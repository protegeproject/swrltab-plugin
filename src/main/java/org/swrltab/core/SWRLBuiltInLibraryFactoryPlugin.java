package org.swrltab.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;

public class SWRLBuiltInLibraryFactoryPlugin extends AbstractProtegePlugin<SWRLBuiltInLibraryFactory>
{
  public static final String PLUGIN_ID = "swrltab-plugin";
  public static final String ID = "SWRLBuiltInLibraryFactoryPlugin";

  public SWRLBuiltInLibraryFactoryPlugin(IExtension extension)
  {
    super(extension);
  }

  public String getName()
  {
    return getPluginProperty("name", "SWRL Built-In Library plugin (No Name Supplied)");
  }

  @Override public SWRLBuiltInLibraryFactory newInstance()
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
  {
    SWRLBuiltInLibraryFactory factory = super.newInstance();
    try {
      factory.initialise();
      return factory;
    } catch (Exception e) {
      try {
        factory.dispose();
      } catch (Exception cleanup) {
        e.addSuppressed(cleanup);
      }
      throw new IllegalStateException("Could not initialize built-in library contribution " + getId(), e);
    }
  }
}
