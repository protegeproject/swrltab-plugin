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
import org.swrlapi.ui.dialog.SWRLAPIApplicationDialogManager;
import org.swrlapi.ui.model.SWRLAPIApplicationModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.queries.SWRLAPIQueriesView;

public class SQWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final Logger log = Logger.getLogger(SQWRLTab.class);

	private static final long serialVersionUID = 1L;

	private OWLModelManager modelManager;
	private SWRLAPIApplicationModel applicationModel;
	private SWRLAPIApplicationDialogManager applicationDialogManager;
	private SWRLAPIQueriesView queriesView;
	private SWRLAPIOWLOntology swrlapiOWLOntology;
	private SWRLRuleEngine queryEngine;
	private Icon ruleEngineIcon;
	private final SQWRLTabListener listener = new SQWRLTabListener();

	private boolean updating = false;

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

		setLayout(new BorderLayout());

		if (this.modelManager.getActiveOntology() != null)
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
		updating = true;
		try {
			// Create a SWRLAPI OWL ontology from the active OWL ontology
			this.swrlapiOWLOntology = SWRLAPIFactory.createSWRLAPIOntology(this.modelManager.getActiveOntology());

			// Create a Drools-based query engine
			this.queryEngine = swrlapiOWLOntology.createSWRLRuleEngine(DroolsFactory.getSWRLRuleEngineCreator());

			// Create the Drools rule engine icon
			this.ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();

			// Create the application model, supplying it with the ontology and rule engine
			this.applicationModel = SWRLAPIFactory.createSWRLAPIApplicationModel(queryEngine);

			// Create the application dialog manager
			this.applicationDialogManager = SWRLAPIFactory.createSWRLAPIApplicationDialogManager(applicationModel);

			if (this.queriesView != null)
				remove(queriesView);

			// Create the primary SQWRLTab view
			this.queriesView = new SWRLAPIQueriesView(applicationModel, applicationDialogManager, ruleEngineIcon);

			add(this.queriesView);

			log.info("SQWRLTab updated");
		} catch (RuntimeException e) {
			log.error("Error updating SQWRLTab", e);
		}
		updating = false;
	}

	private class SQWRLTabListener implements OWLModelManagerListener
	{
		@Override
		public void handleChange(OWLModelManagerChangeEvent event)
		{
			if (!updating) {
				if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
					update();
				}
			} else
				log.info("SQWRLTab Ignoring update");
		}
	}
}
