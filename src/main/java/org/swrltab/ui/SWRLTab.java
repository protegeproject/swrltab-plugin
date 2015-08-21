package org.swrltab.ui;

import checkers.nullness.quals.NonNull;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.rules.SWRLRulesView;

import javax.swing.*;
import java.awt.*;

public class SWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(SWRLTab.class);

	private OWLModelManager modelManager;
	private OWLOntology ontology;
	private SWRLRuleEngine ruleEngine;
	private SWRLRuleEngineModel swrlRuleEngineModel;
	private SWRLRuleEngineDialogManager dialogManager;
	private SWRLRulesView rulesView;
	private Icon ruleEngineIcon;
	private final SWRLTabListener listener = new SWRLTabListener();

	private boolean updating = false;

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override public void initialise()
	{
		super.initialise();

		this.modelManager = getOWLModelManager();
		this.modelManager.addListener(this.listener);

		setLayout(new BorderLayout());

		if (this.modelManager.getActiveOntology() != null)
			update();
	}

	@Override public void dispose()
	{
		super.dispose();
		this.modelManager.removeListener(this.listener);
		log.info("SWRLTab disposed");
	}

	@Override public void update()
	{
		this.updating = true;
		try {
			// Get the active OWL ontology
			this.ontology = this.modelManager.getActiveOntology();

			// Create a Drools-based rule engine
			this.ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(this.ontology);

			// Create the rule engine model, supplying it with the ontology and rule engine
			this.swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(this.ruleEngine);

			// Create the rule engine dialog manager
			this.dialogManager = SWRLAPIFactory.createSWRLRuleEngineDialogManager(this.swrlRuleEngineModel);

			if (this.rulesView != null)
				remove(this.rulesView);

			// Create the main SWRLTab plugin view
			this.rulesView = new SWRLRulesView(this.swrlRuleEngineModel, this.dialogManager);
			add(this.rulesView);

			log.info("SWRLTab updated");
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
