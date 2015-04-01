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
import org.swrlapi.ui.view.rules.SWRLAPIRulesView;

public class SWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(SWRLTab.class);

	private OWLModelManager modelManager;
	private SWRLAPIOWLOntology swrlapiOWLOntology;
	private SWRLRuleEngine ruleEngine;
	private SWRLAPIApplicationModel applicationModel;
	private SWRLAPIApplicationDialogManager applicationDialogManager;
	private SWRLAPIRulesView rulesView;
	private Icon ruleEngineIcon;
	private final SWRLTabListener listener = new SWRLTabListener();

	private boolean updating = false;

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();

		this.modelManager = getOWLModelManager();
		this.modelManager.addListener(this.listener);

		setLayout(new BorderLayout());

		if (this.modelManager.getActiveOntology() != null)
			update();
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
		updating = true;
		try {
			// Create a SWRLAPI OWL ontology from the active OWL ontology
			this.swrlapiOWLOntology = SWRLAPIFactory.createSWRLAPIOntology(this.modelManager.getActiveOntology());

			// Create a Drools-based rule engine
			this.ruleEngine = this.swrlapiOWLOntology.createSWRLRuleEngine(DroolsFactory.getSWRLRuleEngineCreator());

			// Create the Drools rule engine icon
			this.ruleEngineIcon = DroolsFactory.getSWRLRuleEngineIcon();

			// Create the application model, supplying it with the ontology and rule engine
			this.applicationModel = SWRLAPIFactory.createSWRLAPIApplicationModel(swrlapiOWLOntology, ruleEngine);

			// Create the application dialog manager
			this.applicationDialogManager = SWRLAPIFactory.createSWRLAPIApplicationDialogManager(applicationModel);

			if (this.rulesView != null)
				remove(this.rulesView);

			// Create the main SWRLTab plugin view
			this.rulesView = new SWRLAPIRulesView(applicationModel, applicationDialogManager, ruleEngineIcon);
			add(this.rulesView);

			log.info("SWRLTab updated");
		} catch (RuntimeException e) {
			log.error("Error updating SWRLTab", e);
		}
		updating = false;
	}

	private class SWRLTabListener implements OWLModelManagerListener
	{
		@Override
		public void handleChange(OWLModelManagerChangeEvent event)
		{
			if (!updating) {
				if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
					update();
				}
			} else
				log.info("SWRLTab Ignoring update");
		}
	}
}
