package plugins.regions.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for region identifiers. Each region id is
 * associated with a region agent in the simulation.
 * 
 *
 */
@ThreadSafe
public interface RegionId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
