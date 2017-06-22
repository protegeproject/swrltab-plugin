package org.swrltab.core;

import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.swrlapi.builtins.SWRLBuiltInLibrary;

public class SWRLBuiltInLibraryProtegePluginInstance implements ProtegePluginInstance
{
  private SWRLBuiltInLibrary swrlBuiltInLibrary;

  public void setSWRLBuiltInLibrary(SWRLBuiltInLibrary swrlBuiltInLibrary)
  {
    this.swrlBuiltInLibrary = swrlBuiltInLibrary;
  }

  public SWRLBuiltInLibrary getSWRLBuiltInLibrary()
  {
    return this.swrlBuiltInLibrary;
  }

  @Override public void initialise() throws Exception
  {
    this.swrlBuiltInLibrary.reset();
  }

  @Override public void dispose() throws Exception
  {
  }
}
