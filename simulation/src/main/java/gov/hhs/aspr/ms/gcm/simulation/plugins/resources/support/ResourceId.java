package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for resource identifiers
 */
@ThreadSafe
public interface ResourceId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
