package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for plugin identification. Plugins added to an experiment
 * should have unique ids.
 */
@ThreadSafe
public interface PluginId {
	/**
	 * Implementationn consistent with equals()
	 */
	@Override
	public int hashCode();

	/**
	 * Two plugin ids are equal if and only if they represent the same plugin.
	 * Plugin ids are generally implemented as static instances.
	 */
	@Override
	public boolean equals(Object obj);
}
