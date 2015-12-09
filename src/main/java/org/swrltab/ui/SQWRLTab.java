package org.swrltab.ui;

import checkers.nullness.quals.NonNull;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;

import java.awt.*;

public class SQWRLTab extends OWLWorkspaceViewsTab implements SWRLAPIView
{
  private static final Logger log = Logger.getLogger(SQWRLTab.class);

  private static final long serialVersionUID = 1L;

  private OWLModelManager modelManager;
  private SQWRLQueriesView queriesView;

  private final SQWRLTabListener listener = new SQWRLTabListener();

  private boolean updating = false;

  @Override public void initialize()
  {
    super.initialise();

    setToolTipText("SQWRLTab");

    this.queriesView.initialize();

    this.modelManager = getOWLModelManager();
    this.modelManager.addListener(this.listener);

    setLayout(new BorderLayout());

    if (this.modelManager.getActiveOntology() != null)
      update();
  }

  @Override public void dispose()
  {
    super.dispose();
    this.modelManager.removeListener(this.listener);
    log.info("SQWRLTab disposed");
  }

  @Override public void update()
  {
    this.updating = true;
    try {
      // Get the active OWL ontology
      OWLOntology ontology = this.modelManager.getActiveOntology();

      DefaultPrefixManager prefixManager = null; // TODO Where to get prefix manager in plugin?

      // Create a SQWRL query engine
      SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology, prefixManager);

      // Create a query engine model. This is the core plugin model.
      SQWRLQueryEngineModel sqwrlQueryEngineModel = SWRLAPIFactory.createSQWRLQueryEngineModel(queryEngine);

      // Create the dialog manager
      SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory
        .createSWRLRuleEngineDialogManager(sqwrlQueryEngineModel);

      if (this.queriesView != null)
        remove(this.queriesView);

      // Create the primary SQWRLTab view
      this.queriesView = new SQWRLQueriesView(sqwrlQueryEngineModel, dialogManager);

      // Initialize the view
      this.queriesView.initialize();

      // Add it
      add(this.queriesView);

      log.info("SQWRLTab updated");
    } catch (RuntimeException e) {
      log.error("Error updating SQWRLTab", e);
    }
    this.updating = false;
  }

  private class SQWRLTabListener implements OWLModelManagerListener
  {
    @Override public void handleChange(@NonNull OWLModelManagerChangeEvent event)
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
