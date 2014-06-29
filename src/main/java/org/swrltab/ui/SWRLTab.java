package org.swrltab.ui;

import java.awt.BorderLayout;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

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

		this.modelManager = getOWLModelManager();
		this.modelManager.addListener(listener);

		setLayout(new BorderLayout());

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
