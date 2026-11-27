package org.swrltab.core;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.swrlapi.builtins.SWRLBuiltInLibraryManager;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrltab.fixture.ExampleLibrary;
import org.swrltab.fixture.ExampleLibraryFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class ContributedLibraryTest
{
  private SWRLBuiltInLibraryFactoryPlugin contribution()
  {
    return new SWRLBuiltInLibraryFactoryPlugin(null) {
      @Override public String getId() { return "test.contribution"; }
      @Override public SWRLBuiltInLibraryFactory newInstance() {
        var factory = new ExampleLibraryFactory();
        try { factory.initialise(); } catch (Exception e) { throw new AssertionError(e); }
        return factory;
      }
    };
  }

  @Test public void noContributorsPreservesOrdinaryQueryExecution() throws Exception
  {
    try (var owner = new SWRLTabEngineFactory(Collections.emptySet())) {
      var engine = owner.createSQWRLQueryEngine(OWLManager.createOWLOntologyManager().createOntology(),
        SWRLAPIFactory.createIRIResolver());
      var result = engine.runSQWRLQuery("standard", "swrlb:add(?sum, 2, 3) -> sqwrl:select(?sum)");
      assertEquals(1, result.getNumberOfRows());
      assertTrue(result.next());
      assertEquals(java.math.BigInteger.valueOf(5), result.getLiteral(0).getInteger());
      assertFalse(result.next());
    }
  }

  @Test public void contributedLibraryExecutesAndRejectsNonMatchingValues() throws Exception
  {
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      var engine = owner.createSQWRLQueryEngine(OWLManager.createOWLOntologyManager().createOntology(),
        SWRLAPIFactory.createIRIResolver());
      var result = engine.runSQWRLQuery("answer", "contributed:answer(?value) -> sqwrl:select(?value)");
      assertEquals(1, result.getNumberOfRows());
      assertTrue(result.next());
      assertEquals(java.math.BigInteger.valueOf(42), result.getLiteral(0).getInteger());
      assertFalse(result.next());
      assertEquals(1, engine.runSQWRLQuery("bound", "contributed:answer(42) -> sqwrl:select(42)").getNumberOfRows());
      assertEquals(0, engine.runSQWRLQuery("negative", "contributed:answer(41) -> sqwrl:select(41)").getNumberOfRows());
    }
  }

  @Test public void savedQueriesAreRecognizedDuringInitialImport() throws Exception
  {
    byte[] saved;
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      var ontology = OWLManager.createOWLOntologyManager().createOntology();
      var engine = owner.createSQWRLQueryEngine(ontology, SWRLAPIFactory.createIRIResolver());
      engine.createSQWRLQuery("saved", "contributed:answer(?value) -> sqwrl:select(?value)");
      var bytes = new ByteArrayOutputStream();
      ontology.getOWLOntologyManager().saveOntology(ontology, bytes);
      saved = bytes.toByteArray();
    }
    var loaded = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(saved));
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      var engine = owner.createSQWRLQueryEngine(loaded, SWRLAPIFactory.createIRIResolver());
      var result = engine.runSQWRLQuery("saved");
      assertEquals(1, result.getNumberOfRows());
      assertTrue(result.next());
      assertEquals(java.math.BigInteger.valueOf(42), result.getLiteral(0).getInteger());
    }
  }

  @Test public void ruleEngineAlsoRegistersContributionsBeforeInference() throws Exception
  {
    var manager = OWLManager.createOWLOntologyManager();
    var ontology = manager.createOntology();
    var df = manager.getOWLDataFactory();
    var adult = df.getOWLClass(IRI.create("urn:test#Adult"));
    var alice = df.getOWLNamedIndividual(IRI.create("urn:test#alice"));
    manager.addAxiom(ontology, df.getOWLDeclarationAxiom(adult));
    manager.addAxiom(ontology, df.getOWLDeclarationAxiom(alice));
    var answer = df.getSWRLBuiltInAtom(IRI.create("urn:swrlapi:contributed#answer"),
      Collections.singletonList(df.getSWRLLiteralArgument(df.getOWLLiteral(42))));
    manager.addAxiom(ontology, df.getSWRLRule(Collections.singleton(answer),
      Collections.singleton(df.getSWRLClassAtom(adult, df.getSWRLIndividualArgument(alice)))));
    assertFalse(ontology.containsAxiom(df.getOWLClassAssertionAxiom(adult, alice)));
    try (var owner = new SWRLTabEngineFactory(Collections.singleton(contribution()))) {
      var engine = owner.createSWRLRuleEngine(ontology, SWRLAPIFactory.createIRIResolver());
      engine.infer();
      assertTrue(ontology.containsAxiom(df.getOWLClassAssertionAxiom(adult, alice)));
    }
  }

  @Test public void duplicatePrefixesCannotReplaceAnExistingLibrary()
  {
    var manager = new SWRLBuiltInLibraryManager();
    manager.registerSWRLBuiltInLibrary(new ExampleLibrary());
    assertThrows(IllegalArgumentException.class, () -> manager.registerSWRLBuiltInLibrary(new ExampleLibrary()));
    assertEquals(IRI.create("urn:swrlapi:contributed#answer"),
      manager.swrlBuiltInPrefixedName2IRI("contributed:answer").get());
    assertTrue(manager.isSWRLBuiltIn("swrlb:add"));
  }

  @Test public void invalidMethodsDoNotPartiallyRegisterTheLibrary()
  {
    var manager = new SWRLBuiltInLibraryManager();
    var invalid = new ExampleLibrary() {
      @Override public Set<String> getBuiltInNames() { return Set.of("answer", "missing"); }
    };
    assertThrows(IllegalArgumentException.class, () -> manager.registerSWRLBuiltInLibrary(invalid));
    assertFalse(manager.isSWRLBuiltIn("contributed:answer"));
    assertFalse(manager.isSWRLBuiltInIRI(IRI.create("urn:swrlapi:contributed#missing")));
    manager.registerSWRLBuiltInLibrary(new ExampleLibrary());
    assertTrue(manager.isSWRLBuiltIn("contributed:answer"));
  }

  @Test public void collisionFailureDisposesAlreadyCreatedFactories() throws Exception
  {
    var first = new ExampleLibraryFactory();
    var second = new ExampleLibraryFactory();
    first.initialise();
    second.initialise();
    var one = new SWRLBuiltInLibraryFactoryPlugin(null) {
      @Override public String getId() { return "one"; }
      @Override public SWRLBuiltInLibraryFactory newInstance() { return first; }
    };
    var two = new SWRLBuiltInLibraryFactoryPlugin(null) {
      @Override public String getId() { return "two"; }
      @Override public SWRLBuiltInLibraryFactory newInstance() { return second; }
    };
    try (var owner = new SWRLTabEngineFactory(Set.of(one, two))) {
      var ontology = OWLManager.createOWLOntologyManager().createOntology();
      assertThrows(RuntimeException.class, () -> owner.createSQWRLQueryEngine(ontology, SWRLAPIFactory.createIRIResolver()));
      assertThrows(IllegalStateException.class, first::getSWRLBuiltInLibrary);
      assertThrows(IllegalStateException.class, second::getSWRLBuiltInLibrary);
    }
  }
}
