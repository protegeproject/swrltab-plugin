package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.rules.SWRLRulesView;

import java.awt.*;

public class SWRLTab extends OWLWorkspaceViewsTab
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(SWRLTab.class);

	private OWLModelManager modelManager;
	private SWRLRulesView rulesView;

	private final SWRLTabListener listener = new SWRLTabListener();

	private boolean updating = false;

	@Override public void initialise()
	{
		super.initialise();

    setToolTipText("SWRLTab");

    this.modelManager = getOWLModelManager();

    if (this.modelManager != null) {
      this.modelManager.addListener(this.listener);

      setLayout(new BorderLayout());

      log.info("SWRLTab initialized");

      if (this.modelManager.getActiveOntology() != null)
        update();
    } else
      log.warn("SWRLTab initialization failed - no model manager");
	}

	@Override public void dispose()
	{
		super.dispose();
		this.modelManager.removeListener(this.listener);
		log.info("SWRLTab disposed");
	}

  private void update()
	{
		this.updating = true;
		try {
			// Get the active OWL ontology
			OWLOntology activeOntology = this.modelManager.getActiveOntology();

      if (activeOntology != null) {

        // Create a rule engine
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(activeOntology);

        // Create a rule engine model. This is the core plugin model.
        SWRLRuleEngineModel swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);

        // Create the rule engine dialog manager
        SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory.createSWRLRuleEngineDialogManager(swrlRuleEngineModel);

        if (this.rulesView != null)
          remove(this.rulesView);

        // Create the main SWRLTab plugin view
        this.rulesView = new SWRLRulesView(swrlRuleEngineModel, dialogManager);
        this.rulesView.initialize();
        add(this.rulesView);

        log.info("SWRLTab updated");
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
				log.info("SWRLTab Ignoring update");
		}
	}
}
