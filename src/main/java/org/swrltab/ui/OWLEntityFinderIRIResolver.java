package org.swrltab.ui;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.swrlapi.factory.DefaultIRIResolver;

public class OWLEntityFinderIRIResolver extends DefaultIRIResolver
{
  private final OWLEntityFinder owlEntityFinder;

  public OWLEntityFinderIRIResolver(OWLEntityFinder owlEntityFinder)
  {
    super();
    this.owlEntityFinder = owlEntityFinder;
  }

  @NonNull @Override public IRI prefixedName2IRI(@NonNull String prefixedName)
  {
    OWLEntity owlEntity = this.owlEntityFinder.getOWLEntity(prefixedName);

    if (owlEntity != null)
      return owlEntity.getIRI();
    else
      throw new IllegalArgumentException("could not get IRI for prefixed name " + prefixedName);
  }
}
