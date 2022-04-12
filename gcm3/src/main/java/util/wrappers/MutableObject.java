package util.wrappers;

public final class MutableObject<T> {
	private T value;

	public MutableObject(T value) {		
		this.value = value;
	}
	
	public MutableObject() {
				
	}
		
	public void setValue(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableObject [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
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
		if (!(obj instanceof MutableObject)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		MutableObject other = (MutableObject) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	
		
}