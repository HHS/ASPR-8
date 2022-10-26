package util.wrappers;

public final class MutableObject<T> {
	private T value;

	/**
	 * Constructs this MutableObject from the given value
	 */
	public MutableObject(T value) {
		this.value = value;
	}

	/**
	 * Constructs this MutableObject defaulted to null
	 */
	public MutableObject() {

	}

	/**
	 * Sets the current value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Returns the current value
	 */

	public T getValue() {
		return value;
	}

	/**
	 * Returns the string representation of the current value(X) in the form
	 * 
	 * "MutableObject [value="+ X+"]"
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableObject [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns a hash code based on the current value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Two MutableObjects objects are equal if and only if the current values are equal 
	 */
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