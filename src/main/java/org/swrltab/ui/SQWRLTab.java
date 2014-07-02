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
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.queries.SWRLAPIQueriesView;

public class SQWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final Logger log = Logger.getLogger(SQWRLTab.class);
	private static final long serialVersionUID = 1L;

	private final Icon ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();

	private final SQWRLTabListener listener = new SQWRLTabListener();
	private OWLModelManager modelManager;

	public SQWRLTab()
	{
		setToolTipText("SQWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();

		this.modelManager = getOWLModelManager();
		this.modelManager.addListener(listener);

		log.info("SQWRLTab initialized");

		update();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		this.modelManager.removeListener(listener);
		log.info("SQWRLTab disposed");
	}

	@Override
	public void update()
	{
		// Get the active OWL ontology
		OWLOntology ontology = this.modelManager.getActiveOntology();

		// Create a SWRLAPI OWL ontology from the active OWL ontology
		SWRLAPIOWLOntology swrlapiOWLOntology = SWRLAPIFactory.createOntology(ontology);

		// Create a Drools-based query engine
		SWRLRuleEngine queryEngine = SWRLAPIFactory
				.createQueryEngine(swrlapiOWLOntology, new DroolsSWRLRuleEngineCreator());

		// Create the application model, supplying it with the ontology and rule engine
		SWRLAPIApplicationModel applicationModel = SWRLAPIFactory.createApplicationModel(swrlapiOWLOntology, queryEngine);

		// Create an application controller
		SWRLAPIApplicationController applicationController = SWRLAPIFactory.createApplicationController(applicationModel);

		SWRLAPIQueriesView queriesView = new SWRLAPIQueriesView(applicationController, ruleEngineIcon);

		removeAll();
		setLayout(new BorderLayout());
		add(queriesView);
	}

	private class SQWRLTabListener implements OWLModelManagerListener
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
