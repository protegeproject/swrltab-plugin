package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;

import java.awt.*;

public class SQWRLTab extends OWLWorkspaceViewsTab
{
  private static final Logger log = LoggerFactory.getLogger(SQWRLTab.class);

  private static final long serialVersionUID = 1L;

  private SQWRLQueriesView queriesView;

  private final SQWRLTabListener listener = new SQWRLTabListener();

  private boolean updating = false;

  @Override public void initialise()
  {
    super.initialise();

    setToolTipText("SQWRLTab");

    if (getOWLModelManager() != null) {
      getOWLModelManager().addListener(this.listener);

      setLayout(new BorderLayout());

      if (getOWLModelManager().getActiveOntology() != null)
        update();
    } else
      log.warn("SQWRLTab initialization failed - no model manager");

  }

  @Override public void dispose()
  {
    super.dispose();
    getOWLModelManager().removeListener(this.listener);
  }

  private void update()
  {
    this.updating = true;
    try {
      // Get the active OWL ontology
      OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

      if (activeOntology != null) {
        // Create an IRI resolver using Protege's entity finder and entity renderer
        IRIResolver iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
          getOWLModelManager().getOWLEntityRenderer());

        // Create a SQWRL query engine
        SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(activeOntology, iriResolver);

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

      } else
        log.warn("SQWRLTab update failed - no active OWL ontology");
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
        log.warn("SQWRLTab ignoring new update - still processing old update");
    }
  }
}
