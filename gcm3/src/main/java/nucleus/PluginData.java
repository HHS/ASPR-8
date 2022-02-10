package nucleus;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface PluginData {	
	
	public PluginDataBuilder getCloneBuilder();
}
