package org.swrltab.core;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLObject;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.factory.DefaultIRIResolver;
import org.swrlapi.builtins.SWRLBuiltInLibraryManager;

import javax.annotation.Nonnull;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Optional;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

public class ProtegeIRIResolver extends DefaultIRIResolver
{
  private static final Logger log = LoggerFactory.getLogger(ProtegeIRIResolver.class);

  @NonNull private final OWLEntityFinder entityFinder;
  @NonNull private final OWLModelManagerEntityRenderer entityRenderer;
  @NonNull private final OWLObjectRenderer objectRenderer;
  @NonNull private Optional<SWRLBuiltInLibraryManager> builtInLibraryManager = Optional.empty();

  public ProtegeIRIResolver(@NonNull OWLEntityFinder owlEntityFinder,
    @NonNull OWLModelManagerEntityRenderer entityRenderer, @NonNull OWLObjectRenderer objectRenderer)
  {
    super();
    this.entityFinder = owlEntityFinder;
    this.entityRenderer = entityRenderer;
    this.objectRenderer = objectRenderer;
  }

  void setBuiltInLibraryManager(@NonNull SWRLBuiltInLibraryManager manager)
  {
    this.builtInLibraryManager = Optional.of(manager);
  }

  @Override @NonNull public Optional<@NonNull String> iri2PrefixedName(@NonNull IRI iri)
  {
    // Registered library names take precedence over ontology aliases and generated prefixes.
    Optional<String> builtInName = this.builtInLibraryManager.flatMap(manager -> manager.swrlBuiltInIRI2PrefixedName(iri));
    if (builtInName.isPresent())
      return builtInName;
    Optional<@NonNull String> prefixedName = super.iri2PrefixedName(iri);

    if (prefixedName.isPresent()) {
      return Optional.of(prefixedName.get());
    } else {
      return Optional.of(this.entityRenderer.render(iri));
    }
  }

  @NonNull @Override public Optional<@NonNull String> iri2ShortForm(@NonNull IRI iri)
  {
    Optional<@NonNull String> shortForm = super.iri2ShortForm(iri);

    if (shortForm.isPresent())
      return Optional.of(shortForm.get());
     else
      return Optional.of(this.entityRenderer.render(iri));
  }

  @NonNull @Override public Optional<@NonNull IRI> prefixedName2IRI(@NonNull String prefixedName)
  {
    Optional<IRI> builtInIRI = this.builtInLibraryManager.flatMap(manager -> manager.swrlBuiltInPrefixedName2IRI(prefixedName));
    if (builtInIRI.isPresent())
      return builtInIRI;
    OWLEntity owlEntity = this.entityFinder.getOWLEntity(prefixedName);

    if (owlEntity != null)
      return Optional.of(owlEntity.getIRI());
    else
      return super.prefixedName2IRI(prefixedName);
  }

  @NonNull @Override public String render(@Nonnull OWLObject owlObject)
  {
    if (owlObject instanceof SWRLObject) {
      StringWriter writer = new StringWriter();
      owlObject.accept(new SWRLObjectRenderer(writer));
      return writer.toString();
    }
    return this.objectRenderer.render(owlObject);
  }

  /** Keeps rule syntax independent of the host's entity short-name display. */
  private class SWRLObjectRenderer extends ManchesterOWLSyntaxObjectRenderer
  {
    SWRLObjectRenderer(StringWriter writer)
    {
      super(writer, entityRenderer::render);
      setUseWrapping(false);
      setUseTabbing(false);
    }

    @Override public void visit(IRI iri)
    {
      write(entityRenderer.render(iri));
    }

    @Override public void visit(SWRLVariable variable)
    {
      write("?");
      write(iri2VariableName(variable.getIRI()).orElse(variable.getIRI().getShortForm()));
    }

    @Override public void visit(SWRLBuiltInAtom atom)
    {
      write(iri2PrefixedName(atom.getPredicate()).orElse("<" + atom.getPredicate() + ">"));
      write("(");
      Iterator<SWRLDArgument> arguments = asList(atom.arguments()).iterator();
      while (arguments.hasNext()) {
        arguments.next().accept(this);
        if (arguments.hasNext())
          write(", ");
      }
      write(")");
    }
  }
}
