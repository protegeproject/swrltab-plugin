package org.swrltab.fixture;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.IRI;
import org.swrlapi.ui.model.SWRLRuleEngineModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Optional fixture that drives real Swing controls inside a disposable Protégé process. */
public class GraphicalPluginProbe implements BundleActivator
{
  private Timer timer;
  private int attempts;
  private final StringBuilder evidence = new StringBuilder();
  private final java.util.Map<WorkspaceTab, JDialog> editors = new java.util.HashMap<>();

  @Override public void start(BundleContext context)
  {
    String output = System.getProperty("swrlapi.guiSmoke");
    if (output == null) return;
    timer = new Timer(500, event -> {
      try {
        if (++attempts > 120) throw new AssertionError("No visible OWL editor within 60 seconds");
        for (var editor : ProtegeManager.getInstance().getEditorKitManager().getEditorKits()) {
          if (editor instanceof OWLEditorKit && editor.getWorkspace().isShowing()) {
            var kit = (OWLEditorKit)editor;
            if (!kit.getOWLModelManager().getActiveOntology().getOntologyID().getOntologyIRI()
              .filter(iri -> iri.toString().equals("urn:swrlapi:smoke")).isPresent()) continue;
            timer.stop();
            run(kit);
            Files.writeString(Path.of(output), evidence + "GRAPHICAL PLUGIN PASS\n");
            return;
          }
        }
      } catch (Throwable failure) {
        timer.stop();
        failure.printStackTrace();
        try { Files.writeString(Path.of(output), evidence + "FAIL: " + failure + "\n"); }
        catch (Exception writeFailure) { failure.addSuppressed(writeFailure); }
      }
    });
    timer.start();
  }

  @Override public void stop(BundleContext context)
  {
    if (timer != null) timer.stop();
  }

  private void run(OWLEditorKit kit) throws Exception
  {
    var workspace = kit.getOWLWorkspace();
    var manager = kit.getOWLModelManager();
    var original = manager.getActiveOntology();
    WorkspaceTab rules = tab(workspace, "SWRLTab");
    WorkspaceTab queries = tab(workspace, "SQWRLTab");
    workspace.setSelectedTab(queries);
    query(queries, "contributedAnswer", "42");
    query(queries, "osgiSmoke", "5");
    edit(queries, "osgiSmoke", "swrlb:add(?sum, 4, 3) -> sqwrl:select(?sum)");
    query(queries, "osgiSmoke", "7");
    edit(queries, "osgiSmoke", "swrlb:add(?sum, 6, 3) -> sqwrl:select(?sum)");
    query(queries, "osgiSmoke", "9");
    workspace.setSelectedTab(rules);
    edit(rules, "personToAdult", ":Person(?p) ^ swrlb:equal(1, 1) -> :Adult(?p)");
    var df = manager.getOWLDataFactory();
    var conclusion = df.getOWLClassAssertionAxiom(df.getOWLClass(IRI.create("urn:swrlapi:smoke#Adult")),
      df.getOWLNamedIndividual(IRI.create("urn:swrlapi:smoke#alice")));
    require(!original.containsAxiom(conclusion), "Inference negative control already present");
    button(rules, "OWL+SWRL->Drools").doClick();
    button(rules, "Run Drools").doClick();
    button(rules, "Drools->OWL").doClick();
    require(original.containsAxiom(conclusion), "Rule controls did not export Adult(alice)");
    evidence.append("PASS: rule editor, import, Run Drools, export and Adult(alice)\n");

    SWRLRuleEngineModel oldRules = model(rules, "swrlRuleEngineModel");
    SWRLRuleEngineModel oldQueries = model(queries, "sqwrlQueryEngineModel");
    var rulesUpdates = observe(oldRules);
    var queryUpdates = observe(oldQueries);
    manager.getOWLOntologyManager().addAxiom(original,
      df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("urn:swrlapi:smoke#BeforeSwitch"))));
    require(rulesUpdates.get() > 0 && queryUpdates.get() > 0, "Registered tabs did not receive ontology changes");
    rulesUpdates.set(0);
    queryUpdates.set(0);
    var other = manager.getOWLOntologyManager().createOntology(IRI.create("urn:swrlapi:other"));
    manager.setActiveOntology(other);
    require(editors.values().stream().noneMatch(Window::isDisplayable), "Ontology switch retained editor windows");
    require(ruleTable(rules).getRowCount() == 0 && ruleTable(queries).getRowCount() == 0,
      "Ontology switch retained old rules or queries");

    manager.getOWLOntologyManager().addAxiom(original,
      df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("urn:swrlapi:smoke#AfterSwitch"))));
    require(rulesUpdates.get() == 0 && queryUpdates.get() == 0, "Old ontology listeners remain registered");
    require(activeFactories() == 2, "Ontology switch leaked contributed factories");
    evidence.append("PASS: ontology switching clears views and releases old listeners/factories\n");

    ExampleLibraryFactory.failCreation = true;
    try { manager.setActiveOntology(original); }
    finally { ExampleLibraryFactory.failCreation = false; }
    require(model(rules, "swrlRuleEngineModel") == null && model(queries, "sqwrlQueryEngineModel") == null,
      "Failed initialization retained a stale model");
    require(activeFactories() == 0, "Failed initialization leaked contributed factories");
    manager.setActiveOntology(other);
    manager.setActiveOntology(original);
    require(ruleTable(queries).getRowCount() > 0, "Tabs did not recover after initialization failure");
    evidence.append("PASS: failed initialization releases resources; next ontology change recovers\n");

    oldRules = model(rules, "swrlRuleEngineModel");
    oldQueries = model(queries, "sqwrlQueryEngineModel");
    rulesUpdates = observe(oldRules);
    queryUpdates = observe(oldQueries);
    workspace.removeTab(rules);
    rules.dispose();
    workspace.removeTab(queries);
    queries.dispose();
    require(activeFactories() == 0, "Closing tabs leaked contributed factories");

    manager.setActiveOntology(other);
    require(activeFactories() == 0, "Closed tabs still listen to active ontology changes");
    require(model(rules, "swrlRuleEngineModel") == null && model(queries, "sqwrlQueryEngineModel") == null,
      "Closed tabs recreated models");
    manager.getOWLOntologyManager().addAxiom(original,
      df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("urn:swrlapi:smoke#AfterClose"))));
    require(rulesUpdates.get() == 0 && queryUpdates.get() == 0, "Closed models still receive changes");
    manager.setActiveOntology(original);
    tab(workspace, "SWRLTab");
    queries = tab(workspace, "SQWRLTab");
    workspace.setSelectedTab(queries);
    require(activeFactories() == 2, "Reopened tabs did not create exactly two contributed libraries");
    query(queries, "contributedAnswer", "42");
    evidence.append("PASS: tab close/reopen and listener lifecycle\n");
  }

  private WorkspaceTab tab(OWLWorkspace workspace, String label)
  {
    for (var tab : workspace.getWorkspaceTabs()) if (label.equals(tab.getLabel())) return tab;
    return workspace.getOrderedPlugins().stream().filter(plugin -> label.equals(plugin.getLabel()))
      .findFirst().map(workspace::addTabForPlugin).orElseThrow(() -> new AssertionError("Missing tab " + label));
  }

  private void query(WorkspaceTab tab, String name, String expected)
  {
    selectRule(tab, name);
    button(tab, "Run").doClick();
    boolean found = false;
    for (var pane : components(tab, JTabbedPane.class)) {
      int index = pane.indexOfTab(name);
      if (index < 0) continue;
      pane.setSelectedIndex(index);
      for (var table : components((Container)pane.getComponentAt(index), JTable.class)) {
        require(table.getRowCount() == 1 && table.getColumnCount() == 1, "Wrong query result dimensions");
        require(expected.equals(String.valueOf(table.getValueAt(0, 0))), "Wrong displayed result: " + table.getValueAt(0, 0));
        found = true;
      }
    }
    require(found, "No graphical result table for " + name);
    evidence.append("PASS: graphical query ").append(name).append(" = ").append(expected).append('\n');
  }

  private void edit(WorkspaceTab tab, String name, String text)
  {
    selectRule(tab, name);
    Throwable[] error = new Throwable[1];
    Timer editTimer = new Timer(200, event -> {
      ((Timer)event.getSource()).stop();
      try {
        for (Window window : Window.getWindows()) {
          if (!(window instanceof JDialog) || !window.isShowing() || !"Edit".equals(((JDialog)window).getTitle())) continue;
          JDialog previous = editors.put(tab, (JDialog)window);
          require(previous == null || previous == window, "Repeated edits leaked an editor dialog");
          JTextArea area = components(window, JTextArea.class).get(0);
          area.setText(text);
          for (var listener : area.getKeyListeners())
            listener.keyReleased(new KeyEvent(area, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' '));
          button(window, "Ok").doClick();
          require(!window.isShowing(), "Editor did not save valid rule");
          return;
        }
        throw new AssertionError("Rule editor did not open");
      } catch (Throwable failure) {
        error[0] = failure;
        for (Window window : Window.getWindows()) if (window instanceof JDialog && window.isShowing()) window.dispose();
      }
    });
    editTimer.setRepeats(false);
    editTimer.start();
    button(tab, "Edit").doClick();
    if (error[0] != null) throw new AssertionError("Editing failed", error[0]);
    require(!editTimer.isRunning(), "Edit action failed to open its modal dialog");
  }

  private static JTable ruleTable(Container root)
  {
    return components(root, JTable.class).stream().filter(table -> table.getColumnCount() > 2)
      .findFirst().orElseThrow(() -> new AssertionError("Missing rule table"));
  }

  private static void selectRule(Container root, String name)
  {
    JTable table = ruleTable(root);
    for (int row = 0; row < table.getRowCount(); row++)
      for (int col = 0; col < table.getColumnCount(); col++) if (name.equals(table.getValueAt(row, col))) {
        table.setRowSelectionInterval(row, row);
        return;
      }
    throw new AssertionError("Missing rule " + name);
  }

  private static JButton button(Container root, String label)
  {
    JButton button = components(root, JButton.class).stream().filter(value -> label.equals(value.getText()))
      .findFirst().orElseThrow(() -> new AssertionError("Missing button " + label));
    require(button.isEnabled(), "Disabled button " + label);
    return button;
  }

  private static <T> List<T> components(Container root, Class<T> type)
  {
    List<T> result = new ArrayList<>();
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) result.add(type.cast(component));
      if (component instanceof Container) result.addAll(components((Container)component, type));
    }
    return result;
  }

  private static SWRLRuleEngineModel model(Object tab, String name) throws ReflectiveOperationException
  {
    var field = tab.getClass().getDeclaredField(name);
    field.setAccessible(true);
    return (SWRLRuleEngineModel)field.get(tab);
  }

  private static java.util.concurrent.atomic.AtomicInteger observe(SWRLRuleEngineModel model)
  {
    var count = new java.util.concurrent.atomic.AtomicInteger();
    model.getSWRLRulesTableModel().addTableModelListener(event -> count.incrementAndGet());
    return count;
  }

  private static long activeFactories()
  {
    return ExampleLibraryFactory.instances.stream().filter(factory -> {
      try { factory.getSWRLBuiltInLibrary(); return true; }
      catch (IllegalStateException disposed) { return false; }
    }).count();
  }

  private static void require(boolean condition, String message)
  {
    if (!condition) throw new AssertionError(message);
  }
}
