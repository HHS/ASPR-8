package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

import java.util.Objects;

public final class SimpleAttributeId implements AttributeId {
	private final Object value;

	/**
	 * Creates an attribute id from the given value
	 * 
	 * @throws RuntimeException if the value is null
	 */
	public SimpleAttributeId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
		}
		this.value = value;
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	/**
     * Two {@link SimpleAttributeId} instances are equal if and only if
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
		SimpleAttributeId other = (SimpleAttributeId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
