package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import java.util.Objects;

public final class SimpleRegionId implements RegionId {
	private final Object value;

	/**
	 * Creates a region id from the given value
	 * 
	 * @throws NullPointerException if the value is null
	 */
	public SimpleRegionId(Object value) {
		if (value == null) {
			throw new NullPointerException("null value for simple region id");
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
     * Two {@link SimpleRegionId} instances are equal if and only if
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
		SimpleRegionId other = (SimpleRegionId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
