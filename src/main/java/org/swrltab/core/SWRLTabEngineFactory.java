package org.swrltab.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.builtins.SWRLBuiltInLibraryManager;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.factory.DefaultSWRLRuleAndQueryEngineFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Owns contributed library instances for the engines belonging to one tab. */
public class SWRLTabEngineFactory extends DefaultSWRLRuleAndQueryEngineFactory implements AutoCloseable
{
  private static final Logger log = LoggerFactory.getLogger(SWRLTabEngineFactory.class);
  private final List<SWRLBuiltInLibraryFactoryPlugin> plugins;
  private final List<SWRLBuiltInLibraryFactory> factories = new ArrayList<>();
  private boolean closed;

  public SWRLTabEngineFactory()
  {
    this(new SWRLBuiltInLibraryFactoryLoader().getPlugins());
  }

  // Registration uses the initialized parent registry and does not call tab-specific overrides.
  @SuppressWarnings("nullness:method.invocation")
  public SWRLTabEngineFactory(Collection<SWRLBuiltInLibraryFactoryPlugin> plugins)
  {
    this.plugins = new ArrayList<>(plugins);
    this.plugins.sort(java.util.Comparator.comparing(SWRLBuiltInLibraryFactoryPlugin::getId));
    tryToRegisterADefaultSWRLRuleEngine();
  }

  @Override protected void configureBuiltInLibraries(SWRLBuiltInLibraryManager manager, IRIResolver iriResolver)
  {
    if (this.closed)
      throw new IllegalStateException("Built-in library owner is closed");
    try {
      for (SWRLBuiltInLibraryFactoryPlugin plugin : this.plugins) {
        SWRLBuiltInLibraryFactory factory = plugin.newInstance();
        this.factories.add(factory);
        manager.registerSWRLBuiltInLibrary(factory.getSWRLBuiltInLibrary());
        log.info("Loaded SWRL built-in library {} from {}", factory.getSWRLBuiltInLibrary().getPrefix(), plugin.getId());
      }
      if (iriResolver instanceof ProtegeIRIResolver)
        ((ProtegeIRIResolver)iriResolver).setBuiltInLibraryManager(manager);
    } catch (Exception e) {
      close();
      throw new IllegalStateException("Could not register contributed built-in libraries", e);
    }
  }

  @Override public void close()
  {
    this.closed = true;
    for (int i = this.factories.size() - 1; i >= 0; i--) {
      try {
        this.factories.get(i).dispose();
      } catch (Exception e) {
        log.error("Could not dispose contributed built-in library", e);
      }
    }
    this.factories.clear();
  }
}
