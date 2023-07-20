package plugins.materials.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for material identifiers
 * 
 *
 */
@ThreadSafe
public interface MaterialId {
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
