package org.swrltab.ui;

import javax.swing.JDialog;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.rules.SWRLRulesView;
import org.swrltab.core.ProtegeIRIResolver;
import org.swrltab.core.SWRLTabEngineFactory;

import java.awt.*;

public class SWRLTab extends OWLWorkspaceViewsTab
{
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(SWRLTab.class);

  private @Nullable SWRLRuleEngineModel swrlRuleEngineModel;
  private @Nullable SWRLRulesView rulesView;

  private final SWRLTabListener listener = new SWRLTabListener();

  private boolean updating = false;
  private @Nullable SWRLTabEngineFactory engineFactory;
  private @Nullable JDialog ruleEditorDialog;

  @Override public void initialise()
  {
    super.initialise();

    setToolTipText("SWRLTab");

    if (getOWLModelManager() != null) {
      getOWLModelManager().addListener(this.listener);

      setLayout(new BorderLayout());

      if (getOWLModelManager().getActiveOntology() != null)
        update();
    } else
      log.warn("SWRLTab initialization failed - no model manager");
  }

  @Override public void dispose()
  {
    releaseEngine();
    if (getOWLModelManager() != null)
      getOWLModelManager().removeListener(this.listener);
    super.dispose();
  }

  private void releaseEngine()
  {
    if (this.ruleEditorDialog != null) {
      this.ruleEditorDialog.dispose();
      this.ruleEditorDialog = null;
    }
    if (this.swrlRuleEngineModel != null) {
      this.swrlRuleEngineModel.unregisterOntologyListener();
      this.swrlRuleEngineModel = null;
    }
    if (this.engineFactory != null) {
      this.engineFactory.close();
      this.engineFactory = null;
    }
    if (this.rulesView != null) {
      remove(this.rulesView);
      this.rulesView = null;
    }
  }

  private void update()
  {
    this.updating = true;
    try {
      releaseEngine();

      // Get the active OWL ontology
      OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

      if (activeOntology != null) {
        // Create an IRI resolver using Protege's entity finder and entity renderer
        IRIResolver iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
          getOWLModelManager().getOWLEntityRenderer(), getOWLModelManager().getOWLObjectRenderer());

        // Create a rule engine
        this.engineFactory = new SWRLTabEngineFactory();
        SWRLRuleEngine ruleEngine = this.engineFactory.createSWRLRuleEngine(activeOntology, iriResolver);

        // Create a rule engine model. This is the core plugin model.
        SWRLRuleEngineModel model = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);
        this.swrlRuleEngineModel = model;

        // Create the rule engine dialog manager
        SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory
          .createSWRLRuleEngineDialogManager(model);
        this.ruleEditorDialog = dialogManager.getSWRLRuleEditorDialog(this);

        // Create the main SWRLTab plugin view
        SWRLRulesView view = new SWRLRulesView(model, dialogManager);
        this.rulesView = view;
        view.initialize();
        add(view);

        model.registerOntologyListener();

      } else
        log.warn("SWRLTab update failed - no active OWL ontology");
    } catch (RuntimeException e) {
      releaseEngine();
      log.error("Error updating SWRLTab", e);
    } finally {
      this.updating = false;
      revalidate();
      repaint();
    }
  }

  private class SWRLTabListener implements OWLModelManagerListener
  {
    @Override public void handleChange(@NonNull OWLModelManagerChangeEvent event)
    {
      if (!SWRLTab.this.updating) {
        if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
          update();
        }
      } else
        log.warn("SWRLTab ignoring ontology change - still processing old change");
    }
  }
}
