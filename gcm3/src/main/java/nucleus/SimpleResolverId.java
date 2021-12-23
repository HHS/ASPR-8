package nucleus;

/**
 * A convenience class for representing a class reference as a ResolverId
 * 
 * @author Shawn Hatch
 *
 *
 */

public class SimpleResolverId implements ResolverId {

	private final Object value;

	/**
	 * Constructs a SimpleResolverId from the given value
	 * 
	 * @throws RuntimeException
	 *             <li>if the value is null
	 * 
	 * 
	 */
	public SimpleResolverId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
		}
		this.value = value;
	}

	/**
	 * Returns a string of the form SimpleResolverId [value=x] where x is string
	 * representation of the contained value.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleResolverId [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
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
	 * Simple Resolver Ids are equal if and only if their contained values are
	 * equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleResolverId)) {
			return false;
		}
		SimpleResolverId other = (SimpleResolverId) obj;
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