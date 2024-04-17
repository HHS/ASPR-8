package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for group type identifiers
 */
@ThreadSafe
public interface GroupTypeId {

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();
}
