package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.builtins.SWRLBuiltInLibrary;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SQWRLQueryEngineModel;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;
import org.swrltab.core.ProtegeIRIResolver;
import org.swrltab.core.SWRLBuiltInLibraryProtegePlugin;
import org.swrltab.core.SWRLBuiltInLibraryProtegePluginLoader;

import java.awt.*;
import java.util.Set;

public class SQWRLTab extends OWLWorkspaceViewsTab
{
  private static final Logger log = LoggerFactory.getLogger(SQWRLTab.class);

  private static final long serialVersionUID = 1L;

  private SQWRLQueryEngineModel sqwrlQueryEngineModel;
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

    if (this.sqwrlQueryEngineModel != null)
      this.sqwrlQueryEngineModel.unregisterOntologyListener();
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
          getOWLModelManager().getOWLEntityRenderer(), getOWLModelManager().getOWLObjectRenderer());

        // Create a SQWRL query engine
        SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(activeOntology, iriResolver);

        SWRLBuiltInLibraryProtegePluginLoader loader = new SWRLBuiltInLibraryProtegePluginLoader();
        Set<SWRLBuiltInLibraryProtegePlugin> swrlBuiltInLibraryProtegePlugins = loader.getPlugins();

        for (SWRLBuiltInLibraryProtegePlugin plugin : swrlBuiltInLibraryProtegePlugins) {
          SWRLBuiltInLibrary library = plugin.newInstance().getSWRLBuiltInLibrary();
          // TODO add library to query engine
          log.info("Loading SWRL built-in library " + library.getPrefix());
        }

        // Create a query engine model. This is the core plugin model.
        sqwrlQueryEngineModel = SWRLAPIFactory.createSQWRLQueryEngineModel(queryEngine);

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

        this.sqwrlQueryEngineModel.registerOntologyListener();

      } else
        log.warn("SQWRLTab update failed - no active OWL ontology");
    } catch (RuntimeException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
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
        log.warn("SQWRLTab ignoring ontology change - still processing old change");
    }
  }
}
