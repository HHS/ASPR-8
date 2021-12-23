package plugins.components.testsupport;

import plugins.components.support.ComponentId;

/**
 * A convenience class for representing an object reference as a ComponentId
 * 
 * @author Shawn Hatch
 *
 *
 */

public class SimpleComponentId implements ComponentId {

	private final Object value;

	/**
	 * Constructs a SimpleComponentId from the given value
	 * 
	 * @throws RuntimeException
	 *             <li>if the value is null
	 * 
	 * 
	 */
	public SimpleComponentId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
		}
		this.value = value;
	}

	/**
	 * Returns the toString form of its input value
	 */
	@Override
	public String toString() {
		return value.toString();
	}

	/**
	 * Standard hash code implementation based on the contained value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Simple Component Ids are equal if and only if their contained values are
	 * equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleComponentId)) {
			return false;
		}
		SimpleComponentId other = (SimpleComponentId) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}