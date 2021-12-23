package util;

import net.jcip.annotations.Immutable;

/**
 * Base class for int-based identifiers.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class IntId implements Comparable<IntId> {

	private final int value;

	/**
	 * Constructs this IntId from the given value
	 * 
	 */
	public IntId(final int id) {

		this.value = id;
	}

	/**
	 * To be equal, two IntId instances must have the same implementor type and
	 * have the same int value
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IntId other = (IntId) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the value used to construct this IntId
	 */
	public final int getValue() {
		return value;
	}

	/**
	 * Returns the value used to construct this IntId
	 */
	@Override
	public int hashCode() {
		return value;
	}

	/**
	 * Returns the comparison between this and the given IntId values.
	 */
	@Override
	public int compareTo(IntId intId) {
		return Integer.compare(value, intId.value);
	}

	/**
	 * Returns this IntId's value as a string
	 */
	@Override	
	public String toString() {
		return Integer.toString(value);
	}

}
