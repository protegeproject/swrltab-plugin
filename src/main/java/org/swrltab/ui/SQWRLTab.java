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
import org.swrlapi.drools.core.DroolsFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLAPIDialogManager;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.queries.SWRLAPIQueriesView;

public class SQWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
  private static final Logger log = Logger.getLogger(SQWRLTab.class);

  private static final long serialVersionUID = 1L;

  private OWLModelManager modelManager;
  private SQWRLQueryEngineModel sqwrlQueryEngineModel;
  private SWRLAPIDialogManager dialogManager;
  private SWRLAPIQueriesView queriesView;
  private OWLOntology ontology;
  private SQWRLQueryEngine queryEngine;
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
    this.modelManager.addListener(this.listener);

    setLayout(new BorderLayout());

    if (this.modelManager.getActiveOntology() != null)
      update();
  }

  @Override
  public void dispose()
  {
    super.dispose();
    this.modelManager.removeListener(this.listener);
    log.info("SQWRLTab disposed");
  }

  @Override
  public void update()
  {
    this.updating = true;
    try {
      // Get the active OWL ontology
      this.ontology = this.modelManager.getActiveOntology();

      // Create a Drools-based query engine
      this.queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(this.ontology);

      // Get the rule engine icon
      this.ruleEngineIcon = this.queryEngine.getSWRLRuleEngineIcon();

      // Create the query engine model, supplying it with the ontology and query engine
      this.sqwrlQueryEngineModel = this.queryEngine.createSQWRLQueryEngineModel();

      // Create the dialog manager
      this.dialogManager = SWRLAPIFactory.createSWRLAPIDialogManager(this.sqwrlQueryEngineModel);

      if (this.queriesView != null)
        remove(this.queriesView);

      // Create the primary SQWRLTab view
      this.queriesView = new SWRLAPIQueriesView(this.sqwrlQueryEngineModel, this.dialogManager, this.ruleEngineIcon);

      add(this.queriesView);

      log.info("SQWRLTab updated");
    } catch (RuntimeException e) {
      log.error("Error updating SQWRLTab", e);
    }
    this.updating = false;
  }

  private class SQWRLTabListener implements OWLModelManagerListener
  {
    @Override
    public void handleChange(OWLModelManagerChangeEvent event)
    {
      if (!SQWRLTab.this.updating) {
        if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
          update();
        }
      } else
        log.info("SQWRLTab Ignoring update");
    }
  }
}
