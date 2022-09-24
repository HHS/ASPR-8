package plugins.stochastics.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Marker interface for random number generator identifiers
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public interface RandomNumberGeneratorId {

	/**
	 * Returns a non-null, non-empty string that will be used to generate the
	 * initial seed state of a corresponding random number generator from the
	 * string's hash code.
	 */
	@Override
	public String toString();

}
