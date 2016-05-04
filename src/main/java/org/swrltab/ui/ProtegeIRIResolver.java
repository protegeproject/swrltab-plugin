package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.factory.DefaultIRIResolver;

import java.util.Optional;

public class ProtegeIRIResolver extends DefaultIRIResolver
{
  private static final Logger log = LoggerFactory.getLogger(ProtegeIRIResolver.class);

  @NonNull private final OWLEntityFinder entityFinder;
  @NonNull private final OWLModelManagerEntityRenderer entityRender;

  public ProtegeIRIResolver(@NonNull OWLEntityFinder owlEntityFinder,
    @NonNull OWLModelManagerEntityRenderer entityRender)
  {
    super();
    this.entityFinder = owlEntityFinder;
    this.entityRender = entityRender;
  }

  @Override @NonNull public Optional<@NonNull String> iri2PrefixedName(@NonNull IRI iri)
  {
    Optional<@NonNull String> prefixedName = super.iri2PrefixedName(iri);

    if (prefixedName.isPresent()) {
      return Optional.of(prefixedName.get());
    } else {
      return Optional.of(this.entityRender.render(iri));
    }
  }

  @Override @NonNull public Optional<@NonNull String> iri2ShortForm(@NonNull IRI iri)
  {
    return Optional.of(this.entityRender.render(iri));
  }

  @Override public Optional<@NonNull IRI> prefixedName2IRI(@NonNull String prefixedName)
  {
    OWLEntity owlEntity = this.entityFinder.getOWLEntity(prefixedName);

    if (owlEntity != null)
      return Optional.of(owlEntity.getIRI());
    else
      return super.prefixedName2IRI(prefixedName);
  }
}
