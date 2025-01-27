package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.core.ProtegeApplication;
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

import java.awt.*;
import java.io.File;

public class SWRLTab extends OWLWorkspaceViewsTab
{
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(SWRLTab.class);

  private static final File pluginsFolder = new File(System.getProperty(ProtegeApplication.BUNDLE_DIR_PROP));

  private SWRLRuleEngineModel swrlRuleEngineModel;
  private SWRLRulesView rulesView;

  private final SWRLTabListener listener = new SWRLTabListener();

  private boolean updating = false;

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
    super.dispose();

    getOWLModelManager().removeListener(this.listener);

    if (this.swrlRuleEngineModel != null)
      this.swrlRuleEngineModel.unregisterOntologyListener();
  }

  private void update()
  {
    this.updating = true;
    try {
      // Get the active OWL ontology
      OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

      if (activeOntology != null) {
        // Create an IRI resolver using Protege's entity finder and entity renderer
        IRIResolver iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
          getOWLModelManager().getOWLEntityRenderer(), getOWLModelManager().getOWLObjectRenderer());

        // Create a rule engine
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(activeOntology, iriResolver);

        ruleEngine.loadExternalSWRLBuiltInLibraries(pluginsFolder);

        // Create a rule engine model. This is the core plugin model.
        this.swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);

        // Create the rule engine dialog manager
        SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory
          .createSWRLRuleEngineDialogManager(swrlRuleEngineModel);

        if (this.rulesView != null)
          remove(this.rulesView);

        // Create the main SWRLTab plugin view
        this.rulesView = new SWRLRulesView(swrlRuleEngineModel, dialogManager);
        this.rulesView.initialize();
        add(this.rulesView);

        this.swrlRuleEngineModel.registerOntologyListener();

      } else
        log.warn("SWRLTab update failed - no active OWL ontology");
    } catch (RuntimeException e) {
      log.error("Error updating SWRLTab", e);
    }
    this.updating = false;
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
