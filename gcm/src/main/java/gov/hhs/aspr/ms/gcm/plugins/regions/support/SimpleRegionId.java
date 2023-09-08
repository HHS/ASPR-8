package gov.hhs.aspr.ms.gcm.plugins.regions.support;

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
	 * Two {@link SimpleRegionId} instances are equal if and only if their inputs
	 * are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleRegionId)) {
			return false;
		}
		SimpleRegionId other = (SimpleRegionId) obj;
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
