package gov.hhs.aspr.ms.gcm.plugins.materials.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for materials producer identifiers
 * 
 *
 */
@ThreadSafe
public interface MaterialsProducerId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
