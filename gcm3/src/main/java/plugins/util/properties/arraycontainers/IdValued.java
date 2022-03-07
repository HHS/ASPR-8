package util.arraycontainers;

import net.jcip.annotations.Immutable;

/**
 * Base class for objects having a non-negative int id.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class IdValued implements Comparable<IdValued> {

	private final int value;

	public IdValued(final int id) {

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
		final IdValued other = (IdValued) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}

	public final int getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public int compareTo(IdValued personId) {
		return Integer.compare(value, personId.value);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

}
