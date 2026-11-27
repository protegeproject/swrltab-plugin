package org.swrltab.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;

public class SWRLBuiltInLibraryFactoryLoader extends AbstractPluginLoader<SWRLBuiltInLibraryFactoryPlugin>
{
  public SWRLBuiltInLibraryFactoryLoader()
  {
    super(SWRLBuiltInLibraryFactoryPlugin.PLUGIN_ID, SWRLBuiltInLibraryFactoryPlugin.ID);
  }

  @Override protected SWRLBuiltInLibraryFactoryPlugin createInstance(IExtension extension)
  {
    return new SWRLBuiltInLibraryFactoryPlugin(extension);
  }
}
