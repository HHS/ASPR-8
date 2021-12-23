package plugins.compartments.support;

public final class SimpleCompartmentPropertyId implements CompartmentPropertyId {
	private final Object value;
	
	/**
	 * Creates a compartment property id from the given value
	 * 
	 * @throws RuntimeException if the value is null
	 */
	public SimpleCompartmentPropertyId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
		}
		this.value = value;
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
		if (!(obj instanceof SimpleCompartmentPropertyId)) {
			return false;
		}
		SimpleCompartmentPropertyId other = (SimpleCompartmentPropertyId) obj;
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
