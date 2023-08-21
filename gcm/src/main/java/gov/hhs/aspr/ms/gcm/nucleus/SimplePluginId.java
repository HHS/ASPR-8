package gov.hhs.aspr.ms.gcm.nucleus;

import util.errors.ContractException;

/**
 * A convenience class for representing PluginId as a wrapped object.
 */
public class SimplePluginId implements PluginId {

	private final Object value;

	/**
	 * Constructs a SimplePluginId from the given value. The value class must
	 * implement a proper equals contract.
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_ID} if the
	 *                           value is null
	 */
	public SimplePluginId(Object value) {
		if (value == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_ID);
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
	 * Simple Plugin Ids are equal if and only if their contained values are equal
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