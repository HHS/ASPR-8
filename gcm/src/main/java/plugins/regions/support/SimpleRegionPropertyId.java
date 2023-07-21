package plugins.regions.support;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleRegionPropertyId)) {
			return false;
		}
		SimpleRegionPropertyId other = (SimpleRegionPropertyId) obj;
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
