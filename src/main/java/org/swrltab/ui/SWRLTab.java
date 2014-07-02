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
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.ui.controller.SWRLAPIApplicationController;
import org.swrlapi.ui.model.SWRLAPIApplicationModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.rules.SWRLAPIRulesView;

public class SWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final Logger log = Logger.getLogger(SWRLTab.class);
	private static final long serialVersionUID = 1L;

	private final SWRLTabListener listener = new SWRLTabListener();
	private OWLModelManager modelManager;

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();

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

	@Override
	public void update()
	{
		removeAll();

		try {
			// Get the active OWL ontology
			OWLOntology ontology = this.modelManager.getActiveOntology();

			// Create a SWRLAPI OWL ontology from the active OWL ontology
			SWRLAPIOWLOntology swrlapiOWLOntology = SWRLAPIFactory.createOntology(ontology);

			// Create a Drools-based rule engine
			SWRLRuleEngine ruleEngine = SWRLAPIFactory.createQueryEngine(swrlapiOWLOntology,
					new DroolsSWRLRuleEngineCreator());

			// Create the application model, supplying it with the ontology and rule engine
			SWRLAPIApplicationModel applicationModel = SWRLAPIFactory.createApplicationModel(swrlapiOWLOntology, ruleEngine);

			// Create the application controller
			SWRLAPIApplicationController applicationController = SWRLAPIFactory.createApplicationController(applicationModel);

			// Create the rule engine icon
			Icon ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();

			// Create the view
			SWRLAPIRulesView rulesView = new SWRLAPIRulesView(applicationController, ruleEngineIcon);

			setLayout(new BorderLayout());
			add(rulesView);
		} catch (SWRLAPIException e) {
			log.warn("Error updating SWRLTab", e);
		}
	}

	private class SWRLTabListener implements OWLModelManagerListener
	{
		@Override
		public void handleChange(OWLModelManagerChangeEvent event)
		{
			if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
				log.info("The ontology has changed!");
				update();
			}
		}
	}
}
