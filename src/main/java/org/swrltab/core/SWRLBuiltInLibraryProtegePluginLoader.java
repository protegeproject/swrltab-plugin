package org.swrltab.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.owl.ProtegeOWL;

public class SWRLBuiltInLibraryProtegePluginLoader extends AbstractPluginLoader<SWRLBuiltInLibraryProtegePlugin>
{
  public SWRLBuiltInLibraryProtegePluginLoader()
  {
    super(ProtegeOWL.ID, SWRLBuiltInLibraryProtegePlugin.ID);
  }

  @Override protected SWRLBuiltInLibraryProtegePlugin createInstance(IExtension extension)
  {
    return new SWRLBuiltInLibraryProtegePlugin(extension);
  }
}
