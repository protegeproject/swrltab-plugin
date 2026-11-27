package org.swrltab.core;

import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.swrlapi.builtins.SWRLBuiltInLibrary;

import java.util.Optional;

/** A contribution supplies a public no-argument subclass that creates its library. */
public class SWRLBuiltInLibraryFactory implements ProtegePluginInstance
{
  private Optional<SWRLBuiltInLibrary> swrlBuiltInLibrary = Optional.empty();

  public SWRLBuiltInLibrary getSWRLBuiltInLibrary()
  {
    return this.swrlBuiltInLibrary.orElseThrow(() -> new IllegalStateException("Built-in library is not initialized"));
  }

  protected SWRLBuiltInLibrary createSWRLBuiltInLibrary() throws Exception
  {
    throw new IllegalStateException("Contributed factories must implement createSWRLBuiltInLibrary()");
  }

  @Override public void initialise() throws Exception
  {
    if (!this.swrlBuiltInLibrary.isPresent()) {
      SWRLBuiltInLibrary library = createSWRLBuiltInLibrary();
      if (library == null)
        throw new IllegalStateException("Contributed factory returned a null built-in library");
      library.reset();
      this.swrlBuiltInLibrary = Optional.of(library);
    }
  }

  @Override public void dispose() throws Exception
  {
    Optional<SWRLBuiltInLibrary> library = this.swrlBuiltInLibrary;
    this.swrlBuiltInLibrary = Optional.empty();
    if (library.isPresent())
      library.get().reset();
  }
}
