package gov.hhs.aspr.ms.gcm.nucleus;

import net.jcip.annotations.ThreadSafe;

/**
 * Plugin data objects are thread-safe containers of initialization state for
 * the scenarios of a simulation. Plugin data are contributed to plugins which
 * are in turn added to the experiment. The experiment generates multiple
 * scenarios by having the experiment dimensions alter copies of the plugin
 * datas and then distributing those alternate copies to multiple simulation
 * instances. It is best practice for a plugin data to be properly immutable: 1)
 * its state cannot be altered after construction, 2) all its member fields are
 * declared final and 3) it does not pass any reference to itself during its
 * construction. Plugin data classes require a corresponding PluginDataBuilder
 * that builds the plugin.
 */
/*
 * start code_ref=plugin_data_interface|code_cap=The PluginData interface
 * indicates that its implementors are immutable. Plugin data objects are shared
 * between all simulation instances and thus must be thread safe. It introduces
 * a single method used to copy plugin datas during the experiment process.
 */
@ThreadSafe
public interface PluginData {
	/**
	 * Returns a PluginDataBuilder that can build the plugin data. The returned
	 * builder should be initialized with this plugin data object's internal state
	 * such that invocation of pluginData.getCloneBuilder().build() will generate a
	 * copy of the current plugin.
	 */
	public PluginDataBuilder getCloneBuilder();

	@Override
	public int hashCode();

	/**
	 * Plugin datas are equal if they are implicitly equal. They contain the same
	 * implicit information without regard to order.
	 */
	@Override
	public boolean equals(Object obj);

	/**
	 * A string representation of the plugin data implicit data and reflects the
	 * order of addition of the data. Equal plugin datas have equal strings in terms
	 * of content, but not necessarily order.
	 */
	@Override
	public String toString();
}
/* end */
