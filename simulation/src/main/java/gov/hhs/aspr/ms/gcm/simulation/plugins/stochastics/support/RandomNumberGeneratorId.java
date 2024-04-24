package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for random number generator identifiers
 */
@ThreadSafe
public interface RandomNumberGeneratorId {

	/**
	 * Returns the string representation of the generator id
	 */
	@Override
	public String toString();

}
