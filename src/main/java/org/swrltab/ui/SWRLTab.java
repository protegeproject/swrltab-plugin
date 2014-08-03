package org.swrltab.ui;

import java.awt.BorderLayout;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.drools.core.DroolsFactory;
import org.swrlapi.drools.core.DroolsSWRLRuleEngineCreator;
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.ui.dialog.SWRLAPIApplicationDialogManager;
import org.swrlapi.ui.model.SWRLAPIApplicationModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.rules.SWRLAPIRulesView;

public class SWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final Logger log = Logger.getLogger(SWRLTab.class);
	private static final long serialVersionUID = 1L;

	private final SWRLTabListener listener = new SWRLTabListener();
	private OWLModelManager modelManager;
	private SWRLAPIApplicationModel applicationModel;
	private SWRLAPIApplicationDialogManager applicationDialogManager;
	private SWRLAPIRulesView rulesView;
	private SWRLAPIOWLOntology swrlapiOWLOntology;
	private SWRLRuleEngine ruleEngine;
	private Icon ruleEngineIcon;

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();

		this.modelManager = getOWLModelManager();

		try {
			// Create a SWRLAPI OWL ontology from the active OWL ontology
			this.swrlapiOWLOntology = SWRLAPIFactory.createOntology(this.modelManager.getActiveOntology());

			// Create a Drools-based rule engine
			this.ruleEngine = SWRLAPIFactory.createQueryEngine(swrlapiOWLOntology, new DroolsSWRLRuleEngineCreator());

			// Create the Drools rule engine icon
			this.ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();

			// Create the application model, supplying it with the ontology and rule engine
			this.applicationModel = SWRLAPIFactory.createApplicationModel(swrlapiOWLOntology, ruleEngine);

			// Create the application dialog manager
			this.applicationDialogManager = SWRLAPIFactory.createApplicationDialogManager(applicationModel);

			// Create the main SWRLTab plugin view
			this.rulesView = new SWRLAPIRulesView(applicationModel, applicationDialogManager, ruleEngineIcon);

			setLayout(new BorderLayout());
			add(this.rulesView);

			this.modelManager.addListener(this.listener);

			log.info("SWRLTab initialized");
		} catch (SWRLAPIException e) {
			log.warn("Error initializing SWRLTab", e);
		}
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
