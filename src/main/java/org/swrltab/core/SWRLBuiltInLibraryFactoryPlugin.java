package org.swrltab.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;

//Declare an extension point in your plugin.xml that plugins to the SWRL tab will implement
//  <extension-point id="SWRLBuiltInLibraryFactoryPlugin" name="SWRL Built-in Library Factory"/>

public class SWRLBuiltInLibraryFactoryPlugin extends AbstractProtegePlugin<SWRLBuiltInLibraryFactory>
{
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
    try {
      SWRLBuiltInLibraryFactory swrlBuiltInLibraryFactory = super.newInstance();
      return swrlBuiltInLibraryFactory;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
