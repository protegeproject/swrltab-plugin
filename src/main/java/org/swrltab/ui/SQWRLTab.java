package org.swrltab.ui;

import checkers.nullness.quals.NonNull;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLAPIDialogManager;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.queries.SWRLAPIQueriesView;

import javax.swing.*;
import java.awt.*;

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

      // Create a SQWRL query engine
      this.queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(this.ontology);

      // Get the rule engine icon
      this.ruleEngineIcon = this.queryEngine.getRuleEngineIcon();

      // Create the query engine model, supplying it with the query engine
      this.sqwrlQueryEngineModel = SWRLAPIFactory.createSQWRLQueryEngineModel(this.queryEngine);

      // Create the dialog manager
      this.dialogManager = SWRLAPIFactory.createDialogManager(this.sqwrlQueryEngineModel);

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
    public void handleChange(@NonNull OWLModelManagerChangeEvent event)
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
