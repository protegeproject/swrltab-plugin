package org.swrltab.ui;

import javax.swing.JDialog;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.swrltab.core.ProtegeIRIResolver;
import org.swrltab.core.SWRLTabEngineFactory;

import java.awt.*;

public class SQWRLTab extends OWLWorkspaceViewsTab
{
  private static final Logger log = LoggerFactory.getLogger(SQWRLTab.class);

  private static final long serialVersionUID = 1L;

  private @Nullable SQWRLQueryEngineModel sqwrlQueryEngineModel;
  private @Nullable SQWRLQueriesView queriesView;

  private final SQWRLTabListener listener = new SQWRLTabListener();

  private boolean updating = false;
  private @Nullable SWRLTabEngineFactory engineFactory;
  private @Nullable JDialog ruleEditorDialog;

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
    releaseEngine();
    if (getOWLModelManager() != null)
      getOWLModelManager().removeListener(this.listener);
    super.dispose();
  }

  private void releaseEngine()
  {
    if (this.ruleEditorDialog != null) {
      this.ruleEditorDialog.dispose();
      this.ruleEditorDialog = null;
    }
    if (this.sqwrlQueryEngineModel != null) {
      this.sqwrlQueryEngineModel.unregisterOntologyListener();
      this.sqwrlQueryEngineModel = null;
    }
    if (this.engineFactory != null) {
      this.engineFactory.close();
      this.engineFactory = null;
    }
    if (this.queriesView != null) {
      remove(this.queriesView);
      this.queriesView = null;
    }
  }

  private void update()
  {
    this.updating = true;
    try {
      releaseEngine();

      // Get the active OWL ontology
      OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

      if (activeOntology != null) {
        // Create an IRI resolver using Protege's entity finder and entity renderer
        IRIResolver iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
          getOWLModelManager().getOWLEntityRenderer(), getOWLModelManager().getOWLObjectRenderer());

        // Create a SQWRL query engine
        this.engineFactory = new SWRLTabEngineFactory();
        SQWRLQueryEngine queryEngine = this.engineFactory.createSQWRLQueryEngine(activeOntology, iriResolver);

        // Create a query engine model. This is the core plugin model.
        SQWRLQueryEngineModel model = SWRLAPIFactory.createSQWRLQueryEngineModel(queryEngine);
        this.sqwrlQueryEngineModel = model;

        // Create the dialog manager
        SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory
          .createSWRLRuleEngineDialogManager(model);
        this.ruleEditorDialog = dialogManager.getSWRLRuleEditorDialog(this);

        // Create the primary SQWRLTab view
        SQWRLQueriesView view = new SQWRLQueriesView(model, dialogManager);
        this.queriesView = view;

        // Initialize the view
        view.initialize();

        // Add it
        add(view);

        model.registerOntologyListener();

      } else
        log.warn("SQWRLTab update failed - no active OWL ontology");
    } catch (RuntimeException e) {
      releaseEngine();
      log.error("Error updating SQWRLTab", e);
    } finally {
      this.updating = false;
      revalidate();
      repaint();
    }
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
        log.warn("SQWRLTab ignoring ontology change - still processing old change");
    }
  }
}
