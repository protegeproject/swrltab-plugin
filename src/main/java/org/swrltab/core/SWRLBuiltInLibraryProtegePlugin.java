package org.swrltab.core;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractProtegePlugin;

//Declare an extension point in your plugin.xml that plugins to the SWRL tab will implement
//  <extension-point id="SWRLBuiltInLibraryPlugin" name="SWRL Built-in Library Plugin"/>

public class SWRLBuiltInLibraryProtegePlugin extends AbstractProtegePlugin<SWRLBuiltInLibraryProtegePluginInstance>
{
  public static final String ID = "SWRLBuiltInLibraryPlugin";

  public SWRLBuiltInLibraryProtegePlugin(IExtension extension)
  {
    super(extension);
  }

  public String getName()
  {
    return getPluginProperty("name", "SWRL Built-In Library plugin (No Name Supplied)");
  }

  @Override public SWRLBuiltInLibraryProtegePluginInstance newInstance()
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
  {
    try {
      SWRLBuiltInLibraryProtegePluginInstance swrlBuiltInLibraryProtegePluginInstance = super.newInstance();
      swrlBuiltInLibraryProtegePluginInstance.setSWRLBuiltInLibrary(null);
      return swrlBuiltInLibraryProtegePluginInstance;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
