package plugins.personproperties.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for person property identifiers
 * 
 *
 */
@ThreadSafe
public interface PersonPropertyId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
