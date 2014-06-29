package org.swrltab.ui;

import java.awt.BorderLayout;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.drools.DroolsFactory;
import org.swrlapi.drools.DroolsSWRLRuleEngineCreator;
import org.swrlapi.ui.controller.SWRLAPIApplicationController;
import org.swrlapi.ui.model.SWRLAPIApplicationModel;
import org.swrlapi.ui.view.rules.SWRLAPIRulesView;

public class SWRLTab extends OWLWorkspaceViewsTab
{
	private static final Logger log = Logger.getLogger(SWRLTab.class);
	private static final long serialVersionUID = 1L;

	private OWLModelManager modelManager;
	private final SWRLTabListener listener = new SWRLTabListener();

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();

		// Get the active OWL ontology
		OWLOntology ontology = this.modelManager.getActiveOntology();

		// Create a SWRLAPI OWL ontology from the active OWL ontology
		SWRLAPIOWLOntology swrlapiOWLOntology = SWRLAPIFactory.createSWRLAPIOWLOntology(ontology);

		// Create a Drools-based query engine
		SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSQWRLQueryEngine(swrlapiOWLOntology,
				new DroolsSWRLRuleEngineCreator());

		// Create the application model, supplying it with the ontology and rule engine
		SWRLAPIApplicationModel applicationModel = SWRLAPIFactory.createSWRLAPIApplicationModel(swrlapiOWLOntology,
				ruleEngine);

		// Create the application controller
		SWRLAPIApplicationController applicationController = SWRLAPIFactory
				.createSWRLAPIApplicationController(applicationModel);

		Icon ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();
		SWRLAPIRulesView rulesView = new SWRLAPIRulesView(applicationController, ruleEngineIcon);

		setLayout(new BorderLayout());
		add(rulesView);

		this.modelManager = getOWLModelManager();
		this.modelManager.addListener(listener);

		log.info("SWRLTab initialized");
	}

	@Override
	public void dispose()
	{
		super.dispose();
		this.modelManager.removeListener(listener);
		log.info("SWRLTab disposed");
	}

	private class SWRLTabListener implements OWLModelManagerListener
	{
		@Override
		public void handleChange(OWLModelManagerChangeEvent event)
		{
			if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
				log.info("The ontology has changed!");
			}
		}
	}
}
