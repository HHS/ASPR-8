package plugins.globals.support;

import nucleus.util.ContractException;

public final class SimpleGlobalPropertyId implements GlobalPropertyId {
	private final Object value;

	/**
	 * Creates a compartment id from the given value
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_VALUE} if
	 *             the value is null</li>
	 */
	public SimpleGlobalPropertyId(Object value) {
		if (value == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_VALUE);
		}
		this.value = value;
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
	 * Two {@link SimpleGlobalPropertyId} instances are equal if and only if
	 * their inputs are equal.
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
