package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;



public final class SimpleAttributeId implements AttributeId {
	private final Object value;

	/**
	 * Creates an attribute id from the given value
	 * 
	 * @throws RuntimeException
	 *             if the value is null
	 */
	public SimpleAttributeId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
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
	 * Two {@link SimpleAttributeId} instances are equal if and only if their
	 * inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleAttributeId)) {
			return false;
		}
		SimpleAttributeId other = (SimpleAttributeId) obj;
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
