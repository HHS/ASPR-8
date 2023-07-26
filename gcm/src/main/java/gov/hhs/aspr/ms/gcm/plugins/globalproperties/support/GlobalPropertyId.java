package gov.hhs.aspr.ms.gcm.plugins.globalproperties.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for global property identifiers
 * 
 *
 */
@ThreadSafe
public interface GlobalPropertyId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
