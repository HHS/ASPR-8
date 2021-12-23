package nucleus;

/**
 * A convenience class for representing a class reference as a PluginId
 * 
 * @author Shawn Hatch
 *
 *
 */

public class SimplePluginId implements PluginId {

	private final Object value;

	/**
	 * Constructs a SimpleResolverId from the given value
	 * 
	 * @throws RuntimeException
	 *             <li>if the value is null
	 * 
	 * 
	 */
	public SimplePluginId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
		}
		this.value = value;
	}

	/**
	 * Returns the toString form of the input
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
	 * Simple Resolver Ids are equal if and only if their contained values are
	 * equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimplePluginId)) {
			return false;
		}
		SimplePluginId other = (SimplePluginId) obj;
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