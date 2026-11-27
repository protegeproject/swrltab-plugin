package org.swrltab.core;

import org.junit.Test;
import org.swrlapi.builtins.SWRLBuiltInLibrary;
import org.swrltab.fixture.ExampleLibrary;
import org.swrltab.fixture.ExampleLibraryFactory;

import static org.junit.Assert.*;

public class SWRLBuiltInLibraryFactoryTest
{
  @Test public void accessBeforeInitializationFailsClearly()
  {
    assertThrows(IllegalStateException.class, () -> new ExampleLibraryFactory().getSWRLBuiltInLibrary());
  }

  @Test public void initializationCreatesAndResetsTheContributedLibrary() throws Exception
  {
    var factory = new ExampleLibraryFactory();
    factory.initialise();
    var library = (ExampleLibrary)factory.getSWRLBuiltInLibrary();
    assertEquals("contributed", library.getPrefix());
    assertEquals(1, library.resets);
    factory.initialise();
    assertSame(library, factory.getSWRLBuiltInLibrary());
    assertEquals(1, library.resets);
    factory.dispose();
    assertEquals(2, library.resets);
    assertThrows(IllegalStateException.class, factory::getSWRLBuiltInLibrary);
    factory.dispose();
    assertEquals(2, library.resets);
    factory.initialise();
    assertNotSame(library, factory.getSWRLBuiltInLibrary());
    factory.dispose();
  }

  @Test public void unconfiguredFactoryFailsClearly()
  {
    assertThrows(IllegalStateException.class, () -> new SWRLBuiltInLibraryFactory().initialise());
  }

  @Test public void nullLibraryIsRejectedWithoutPublishingAnInitializedFactory()
  {
    var factory = new SWRLBuiltInLibraryFactory() {
      protected SWRLBuiltInLibrary createSWRLBuiltInLibrary() { return null; }
    };
    assertThrows(IllegalStateException.class, factory::initialise);
    assertThrows(IllegalStateException.class, factory::getSWRLBuiltInLibrary);
  }
}
