package org.swrltab.core;

import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRendererImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.view.rules.AssertedOWLAxiomsView;
import org.swrltab.fixture.ExampleLibraryFactory;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtegeIRIResolverTest {
  private static final String NS = "urn:swrlapi:render#";
  private static final String SWRLB = "http://www.w3.org/2003/11/swrlb#";
  private static final String SQWRL = "http://sqwrl.stanford.edu/ontologies/built-ins/3.4/sqwrl.owl#";
  private OWLDataFactory factory;
  private OWLOntology ontology;
  private ProtegeIRIResolver resolver;
  private OWLObjectRendererImpl hostRenderer;

  @Before public void setUp() throws Exception {
    var manager = OWLManager.createOWLOntologyManager();
    ontology = manager.createOntology();
    factory = manager.getOWLDataFactory();
    var format = new TurtleDocumentFormat();
    format.setDefaultPrefix(NS);
    manager.setOntologyFormat(ontology, format);
    // Model the host's prefixed entity display while using its real object renderer.
    OWLModelManagerEntityRenderer entities = (OWLModelManagerEntityRenderer)Proxy.newProxyInstance(
      getClass().getClassLoader(), new Class<?>[]{OWLModelManagerEntityRenderer.class}, (proxy, method, args) -> {
        if (method.getName().equals("render") || method.getName().equals("getShortForm")) {
          IRI iri = args[0] instanceof IRI ? (IRI)args[0] : ((OWLEntity)args[0]).getIRI();
          return iri.getNamespace().equals(NS) ? ":" + iri.getShortForm() : iri.getShortForm();
        }
        throw new AssertionError("Unexpected entity renderer call: " + method);
      });
    OWLModelManager model = (OWLModelManager)Proxy.newProxyInstance(getClass().getClassLoader(),
      new Class<?>[]{OWLModelManager.class}, (proxy, method, args) -> {
        if (method.getName().equals("getOWLEntityRenderer")) return entities;
        if (method.getName().equals("getRendering")) return entities.render((OWLEntity)args[0]);
        throw new AssertionError("Unexpected model manager call: " + method);
      });
    OWLEntityFinder finder = (OWLEntityFinder)Proxy.newProxyInstance(getClass().getClassLoader(),
      new Class<?>[]{OWLEntityFinder.class}, (proxy, method, args) -> {
        throw new AssertionError("Rendering must not look up entities: " + method);
      });
    hostRenderer = new OWLObjectRendererImpl(model);
    resolver = new ProtegeIRIResolver(finder, entities, hostRenderer);
    resolver.updatePrefixes(ontology);
  }

  @Test public void arithmeticQueryKeepsBuiltInPrefixesAndNormalVariables() {
    SWRLVariable sum = factory.getSWRLVariable(IRI.create(NS + "sum"));
    SWRLBuiltInAtom add = factory.getSWRLBuiltInAtom(IRI.create(SWRLB + "add"), Arrays.asList(sum,
      factory.getSWRLLiteralArgument(factory.getOWLLiteral(2)),
      factory.getSWRLLiteralArgument(factory.getOWLLiteral(3))));
    SWRLBuiltInAtom select = factory.getSWRLBuiltInAtom(IRI.create(SQWRL + "select"), Collections.singletonList(sum));
    SWRLRule rule = factory.getSWRLRule(Collections.singleton(add), Collections.singleton(select));
    assertEquals("swrlb:add(?sum, 2, 3) -> sqwrl:select(?sum)", resolver.render(rule));
  }

  @Test public void classNamesKeepTheirDefaultPrefixButVariablesDoNot() {
    SWRLVariable person = factory.getSWRLVariable(IRI.create(NS + "p"));
    SWRLRule rule = factory.getSWRLRule(
      Collections.singleton(factory.getSWRLClassAtom(factory.getOWLClass(IRI.create(NS + "Person")), person)),
      Collections.singleton(factory.getSWRLClassAtom(factory.getOWLClass(IRI.create(NS + "Adult")), person)));
    assertEquals(":Person(?p) -> :Adult(?p)", resolver.render(rule));
  }

  @Test public void explicitVariableNamespaceDoesNotLeakIntoItsName() {
    resolver.setPrefix("vars:", "urn:variables#");
    assertEquals("?sum", resolver.render(factory.getSWRLVariable(IRI.create("urn:variables#sum"))));
  }

  @Test public void customBuiltInsRetainTheirRegisteredNamespace() {
    resolver.setPrefix("custom:", "urn:custom#");
    SWRLBuiltInAtom atom = factory.getSWRLBuiltInAtom(IRI.create("urn:custom#add"),
      Collections.singletonList(factory.getSWRLVariable(IRI.create(NS + "sum"))));
    assertEquals("custom:add(?sum)", resolver.render(atom));
  }

  private SWRLBuiltInLibraryFactoryPlugin contribution() {
    return new SWRLBuiltInLibraryFactoryPlugin(null) {
      @Override public String getId() { return "test.rendering"; }
      @Override public SWRLBuiltInLibraryFactory newInstance() {
        var factory = new ExampleLibraryFactory();
        try { factory.initialise(); } catch (Exception e) { throw new AssertionError(e); }
        return factory;
      }
    };
  }

  @Test public void ruleEngineUsesTheLibraryPrefixWithoutAnOntologyPrefix() throws Exception {
    var iri = IRI.create("urn:swrlapi:contributed#answer");
    // Populate the autogenerated-name cache before registering the library.
    resolver.iri2PrefixedName(iri);
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      owner.createSWRLRuleEngine(ontology, resolver);
      var atom = factory.getSWRLBuiltInAtom(iri,
        Collections.singletonList(factory.getSWRLVariable(IRI.create(NS + "value"))));
      assertEquals("contributed:answer(?value)", resolver.render(atom));
      assertEquals(iri, resolver.prefixedName2IRI("contributed:answer").get());
    }
  }

  @Test public void queryEngineKeepsLibraryNamesAfterLoadingAndRefreshingPrefixes() throws Exception {
    var iri = IRI.create("urn:swrlapi:contributed#answer");
    var atom = factory.getSWRLBuiltInAtom(iri,
      Collections.singletonList(factory.getSWRLLiteralArgument(factory.getOWLLiteral(42))));
    var format = (TurtleDocumentFormat)ontology.getOWLOntologyManager().getOntologyFormat(ontology);
    format.setPrefix("alias:", "urn:swrlapi:contributed#");
    format.setPrefix("contributed:", "urn:unrelated#");
    var bytes = new java.io.ByteArrayOutputStream();
    ontology.getOWLOntologyManager().saveOntology(ontology, bytes);
    var loaded = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(
      new java.io.ByteArrayInputStream(bytes.toByteArray()));
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      owner.createSQWRLQueryEngine(loaded, resolver);
      resolver.reset();
      resolver.updatePrefixes(loaded);
      assertEquals("contributed:answer(42)", resolver.render(atom));
      assertEquals(iri, resolver.prefixedName2IRI("contributed:answer").get());
      // An unregistered predicate still uses ordinary ontology prefix resolution.
      var unknown = factory.getSWRLBuiltInAtom(IRI.create("urn:unrelated#unknown"), atom.getArguments());
      assertEquals("contributed:unknown(42)", resolver.render(unknown));
    }
  }

  @Test public void literalTextIsNotRewrittenAsRuleSyntax() {
    SWRLBuiltInAtom atom = factory.getSWRLBuiltInAtom(IRI.create(SQWRL + "select"),
      Collections.singletonList(factory.getSWRLLiteralArgument(
        factory.getOWLLiteral("add(?:sum) -> select(?:p)", "en"))));
    assertEquals("sqwrl:select(\"add(?:sum) -> select(?:p)\"@en)", resolver.render(atom));
  }

  @Test public void ordinaryOWLAxiomsStillUseTheHostRenderer() {
    OWLClass adult = factory.getOWLClass(IRI.create(NS + "Adult"));
    OWLAxiom declaration = factory.getOWLDeclarationAxiom(adult);
    OWLAxiom assertion = factory.getOWLClassAssertionAxiom(adult,
      factory.getOWLNamedIndividual(IRI.create(NS + "alice")));
    assertTrue(hostRenderer.render(declaration).contains(":Adult"));
    assertEquals(hostRenderer.render(declaration), resolver.render(declaration));
    assertEquals(hostRenderer.render(assertion), resolver.render(assertion));
  }

  @Test public void anonymousClassExpressionsRemainReadableInsideRules() {
    SWRLVariable person = factory.getSWRLVariable(IRI.create(NS + "p"));
    OWLClassExpression restriction = factory.getOWLObjectSomeValuesFrom(
      factory.getOWLObjectProperty(IRI.create(NS + "knows")),
      factory.getOWLClass(IRI.create(NS + "Adult")));
    SWRLClassAtom atom = factory.getSWRLClassAtom(restriction, person);
    String rendering = resolver.render(atom);
    assertTrue(rendering, rendering.contains(":knows some :Adult"));
    assertTrue(rendering, rendering.endsWith("(?p)"));
  }

  @Test public void assertedAxiomTableUsesTheCorrectedRendering() throws Exception {
    SWRLVariable sum = factory.getSWRLVariable(IRI.create(NS + "sum"));
    SWRLBuiltInAtom add = factory.getSWRLBuiltInAtom(IRI.create(SWRLB + "add"), Arrays.asList(sum,
      factory.getSWRLLiteralArgument(factory.getOWLLiteral(2)),
      factory.getSWRLLiteralArgument(factory.getOWLLiteral(3))));
    SWRLBuiltInAtom select = factory.getSWRLBuiltInAtom(IRI.create(SQWRL + "select"), Collections.singletonList(sum));
    ontology.getOWLOntologyManager().addAxiom(ontology,
      factory.getSWRLRule(Collections.singleton(add), Collections.singleton(select)));
    var engine = SWRLAPIFactory.createSWRLRuleEngine(ontology, resolver);
    engine.importAssertedOWLAxioms();
    var model = SWRLAPIFactory.createSWRLRuleEngineModel(engine);
    SwingUtilities.invokeAndWait(() -> {
      var view = new AssertedOWLAxiomsView(model);
      view.initialize();
      JTable table = (JTable)((JScrollPane)view.getComponent(0)).getViewport().getView();
      int matches = 0;
      for (int row = 0; row < table.getRowCount(); row++)
        if ("swrlb:add(?sum, 2, 3) -> sqwrl:select(?sum)".equals(table.getValueAt(row, 0)))
          matches++;
      assertEquals("The displayed query must occur exactly once", 1, matches);
    });
  }
}
