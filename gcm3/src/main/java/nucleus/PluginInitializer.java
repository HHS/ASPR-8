package nucleus;

public interface Plugin {

	public PluginId getPluginId();
	
	public void init(PluginContext context);
	
	public PluginBuilder getCloneBuilder();
	
}
