package gov.hhs.aspr.ms.gcm.plugins.globalproperties.support;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A simple implementor of {@link GlobalPropertyId} that wraps a value.
 */
public final class SimpleGlobalPropertyId implements GlobalPropertyId {

	private final Object value;

	/**
	 * Creates a global property id from the given value. The value must implement a
	 * proper equals contract.
	 * 
	 * @throws util.errors.ContractException {@linkplain PropertyError#NULL_PROPERTY_VALUE} if
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
	 * Standard implementation
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Two {@link SimpleGlobalPropertyId} instances are equal if and only if their
	 * inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleGlobalPropertyId)) {
			return false;
		}
		SimpleGlobalPropertyId other = (SimpleGlobalPropertyId) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
