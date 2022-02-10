package nucleus;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface PluginInitializer {

	public PluginId getPluginId();

	public void init(PluginContext pluginContext);

}
