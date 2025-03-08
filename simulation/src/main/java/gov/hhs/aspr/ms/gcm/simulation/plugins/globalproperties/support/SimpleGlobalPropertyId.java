package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

import java.util.Objects;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A simple implementor of {@link GlobalPropertyId} that wraps a value.
 */
public final class SimpleGlobalPropertyId implements GlobalPropertyId {

	private final Object value;

	/**
	 * Creates a global property id from the given value. The value must implement a
	 * proper equals contract.
	 * 
	 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_VALUE} if
	 *                           the value is null
	 */
	public SimpleGlobalPropertyId(Object value) {
		if (value == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

    /**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	/**
     * Two {@link SimpleGlobalPropertyId} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleGlobalPropertyId other = (SimpleGlobalPropertyId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
