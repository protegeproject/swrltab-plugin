package org.swrltab.core;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.factory.DefaultIRIResolver;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ProtegeIRIResolver extends DefaultIRIResolver
{
  private static final Logger log = LoggerFactory.getLogger(ProtegeIRIResolver.class);

  @NonNull private final OWLEntityFinder entityFinder;
  @NonNull private final OWLModelManagerEntityRenderer entityRenderer;
  @NonNull private final OWLObjectRenderer objectRenderer;

  public ProtegeIRIResolver(@NonNull OWLEntityFinder owlEntityFinder,
    @NonNull OWLModelManagerEntityRenderer entityRenderer, @NonNull OWLObjectRenderer objectRenderer)
  {
    super();
    this.entityFinder = owlEntityFinder;
    this.entityRenderer = entityRenderer;
    this.objectRenderer = objectRenderer;
  }

  @Override @NonNull public Optional<@NonNull String> iri2PrefixedName(@NonNull IRI iri)
  {
    Optional<@NonNull String> prefixedName = super.iri2PrefixedName(iri);

    if (prefixedName.isPresent()) {
      return Optional.of(prefixedName.get());
    } else {
      return Optional.of(this.entityRenderer.render(iri));
    }
  }

  @NonNull @Override public Optional<@NonNull String> iri2ShortForm(@NonNull IRI iri)
  {
    return Optional.of(this.entityRenderer.render(iri));
  }

  @NonNull @Override public Optional<@NonNull IRI> prefixedName2IRI(@NonNull String prefixedName)
  {
    OWLEntity owlEntity = this.entityFinder.getOWLEntity(prefixedName);

    if (owlEntity != null)
      return Optional.of(owlEntity.getIRI());
    else
      return super.prefixedName2IRI(prefixedName);
  }

  @NonNull @Override public String render(@Nonnull OWLObject owlObject)
  {
    return this.objectRenderer.render(owlObject);
  }
}
