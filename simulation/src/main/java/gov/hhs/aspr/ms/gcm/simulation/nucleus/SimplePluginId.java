package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;

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
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	/**
     * Two {@link SimplePluginId} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SimplePluginId other = (SimplePluginId) obj;
		return Objects.equals(value, other.value);
	}
}