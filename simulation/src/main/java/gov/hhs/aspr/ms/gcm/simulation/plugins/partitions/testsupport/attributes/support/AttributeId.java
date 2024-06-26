package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for attribute identifiers
 */
@ThreadSafe
public interface AttributeId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
