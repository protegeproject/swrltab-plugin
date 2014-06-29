package org.swrltab.ui;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class SWRLTab extends OWLWorkspaceViewsTab
{
	private static final Logger log = Logger.getLogger(SWRLTab.class);
	private static final long serialVersionUID = 1L;

	public SWRLTab()
	{
		setToolTipText("SWRLTab");
	}

	@Override
	public void initialise()
	{
		super.initialise();
		log.info("SWRLTab initialized");
	}

	@Override
	public void dispose()
	{
		super.dispose();
		log.info("SWRLTab disposed");
	}
}
