package org.swrltab.core;

import org.protege.editor.core.plugin.ProtegePluginInstance;
import org.swrlapi.builtins.SWRLBuiltInLibrary;

public class SWRLBuiltInLibraryFactory implements ProtegePluginInstance
{
  private SWRLBuiltInLibrary swrlBuiltInLibrary;

  public SWRLBuiltInLibrary getSWRLBuiltInLibrary()
  {
    return this.swrlBuiltInLibrary;
  }

  @Override public void initialise() throws Exception
  {
    this.swrlBuiltInLibrary = null; // TODO create it
    this.swrlBuiltInLibrary.reset();
  }

  @Override public void dispose() throws Exception
  {
  }
}
