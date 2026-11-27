package org.swrltab.fixture;

import org.swrlapi.builtins.SWRLBuiltInLibrary;
import org.swrltab.core.SWRLBuiltInLibraryFactory;

public class ExampleLibraryFactory extends SWRLBuiltInLibraryFactory
{
  protected SWRLBuiltInLibrary createSWRLBuiltInLibrary()
  {
    return new ExampleLibrary();
  }
}
