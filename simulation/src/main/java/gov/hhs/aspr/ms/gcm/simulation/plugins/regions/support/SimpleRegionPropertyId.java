package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import java.util.Objects;

public final class SimpleRegionPropertyId implements RegionPropertyId {

	private final Object value;

	/**
	 * Creates a region property id from the given value
	 * 
	 * @throws NullPointerException if the value is null
	 */
	public SimpleRegionPropertyId(Object value) {
		if (value == null) {
			throw new NullPointerException("null value in simple region property id");
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
     * Two {@link SimpleRegionPropertyId} instances are equal if and only if
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
		SimpleRegionPropertyId other = (SimpleRegionPropertyId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
