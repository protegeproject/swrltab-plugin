package org.swrltab.fixture;

import org.swrlapi.builtins.SWRLBuiltInLibrary;
import org.swrltab.core.SWRLBuiltInLibraryFactory;

public class ExampleLibraryFactory extends SWRLBuiltInLibraryFactory
{
  public static final java.util.List<ExampleLibraryFactory> instances = new java.util.ArrayList<>();
  public static boolean failCreation;

  protected SWRLBuiltInLibrary createSWRLBuiltInLibrary()
  {
    if (failCreation) throw new IllegalStateException("Intentional GUI fixture initialization failure");
    instances.add(this);
    return new ExampleLibrary();
  }
}
