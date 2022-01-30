package nucleus;

import java.util.Set;


public interface Plugin {

	public PluginId getPluginId();
	
	public Set<PluginId> getPluginDependencies();
	
	public void init(PluginContext context);
	
	public PluginBuilder getCloneBuilder();
	
}
