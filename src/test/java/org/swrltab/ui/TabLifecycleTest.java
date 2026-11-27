package org.swrltab.ui;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;
import org.swrlapi.ui.view.rules.SWRLRulesView;
import org.swrltab.core.SWRLTabEngineFactory;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TabLifecycleTest
{
  @Test public void ruleTabReleasesItsModelListenerAndView() throws Exception
  {
    verifyRelease(false);
  }

  @Test public void queryTabReleasesItsModelListenerAndView() throws Exception
  {
    verifyRelease(true);
  }

  private void verifyRelease(boolean query) throws Exception
  {
    SwingUtilities.invokeAndWait(() -> {
      try {
        var manager = OWLManager.createOWLOntologyManager();
        var ontology = manager.createOntology();
        var owner = new SWRLTabEngineFactory(Collections.emptySet());
        var engine = owner.createSQWRLQueryEngine(ontology, SWRLAPIFactory.createIRIResolver());
        var model = SWRLAPIFactory.createSQWRLQueryEngineModel(engine);
        var dialogs = (org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager)java.lang.reflect.Proxy.newProxyInstance(
          getClass().getClassLoader(), new Class<?>[]{org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager.class},
          (proxy, method, args) -> { throw new AssertionError("Cleanup must not open a dialog"); });
        JComponent tab = query ? new SQWRLTab() : new SWRLTab();
        JComponent view = query ? new SQWRLQueriesView(model, dialogs) : new SWRLRulesView(model, dialogs);
        set(tab, query ? "sqwrlQueryEngineModel" : "swrlRuleEngineModel", model);
        set(tab, query ? "queriesView" : "rulesView", view);
        set(tab, "engineFactory", owner);
        tab.add(view);
        AtomicInteger independentListener = new AtomicInteger();
        manager.addOntologyChangeListener(changes -> independentListener.incrementAndGet());
        AtomicInteger updates = new AtomicInteger();
        model.getSWRLRulesTableModel().setView(new org.swrlapi.ui.view.SWRLAPIView() {
          @Override public void initialize() {}
          @Override public void update() { updates.incrementAndGet(); }
        });
        model.registerOntologyListener();
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(
          manager.getOWLDataFactory().getOWLClass(IRI.create("urn:test#Before"))));
        assertTrue("Registered model must receive changes", updates.get() > 0);
        int beforeRelease = updates.get();

        var release = tab.getClass().getDeclaredMethod("releaseEngine");
        release.setAccessible(true);
        release.invoke(tab);
        release.invoke(tab);
        assertEquals(0, tab.getComponentCount());
        assertNull(get(tab, "engineFactory"));
        assertNull(get(tab, query ? "sqwrlQueryEngineModel" : "swrlRuleEngineModel"));
        assertNull(get(tab, query ? "queriesView" : "rulesView"));
        manager.addAxiom(ontology, manager.getOWLDataFactory().getOWLDeclarationAxiom(
          manager.getOWLDataFactory().getOWLClass(IRI.create("urn:test#After"))));
        assertEquals("Released model still listens to ontology changes", beforeRelease, updates.get());
        assertEquals(2, independentListener.get());
        var closed = assertThrows(org.swrlapi.exceptions.SWRLRuleEngineException.class,
          () -> owner.createSQWRLQueryEngine(ontology, SWRLAPIFactory.createIRIResolver()));
        assertTrue(closed.getCause() instanceof IllegalStateException);
        assertEquals("Built-in library owner is closed", closed.getCause().getMessage());
      } catch (ReflectiveOperationException | org.semanticweb.owlapi.model.OWLOntologyCreationException e) {
        throw new AssertionError(e);
      }
    });
  }

  private static void set(Object object, String name, Object value) throws ReflectiveOperationException
  {
    var field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    field.set(object, value);
  }

  private static Object get(Object object, String name) throws ReflectiveOperationException
  {
    var field = object.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return field.get(object);
  }
}
