package org.swrltab.ui;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class SQWRLTab extends OWLWorkspaceViewsTab
{
	private static final Logger log = Logger.getLogger(SQWRLTab.class);
	private static final long serialVersionUID = 1L;

	public SQWRLTab()
	{
		setToolTipText("SQWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();
		log.info("SQWRLTab initialized");
	}

	@Override
	public void dispose()
	{
		super.dispose();
		log.info("SQWRLTab disposed");
	}
}
