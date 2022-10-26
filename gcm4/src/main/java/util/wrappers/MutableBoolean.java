package util.wrappers;

public final class MutableBoolean {
	private boolean value;

	/**
	 * Constructs this MutableBoolean from the given value
	 */	
	public MutableBoolean(boolean value) {
		
		this.value = value;
	}
	/**
	 * Constructs this MutableBoolean defaulted to false
	 */	
	public MutableBoolean() {
				
	}
	/**
	 * Sets the current value
	 */
	public void setValue(boolean value) {
		this.value = value;
	}
	/**
	 * Returns the current value
	 */

	public boolean getValue() {
		return value;
	}

	/**
	 * Returns the string representation of the current value(X) in the form
	 * 
	 * "MutableBoolean [value="+ X+"]"
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableBoolean [value=");
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
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

	
	/**
	 * Two MutableBoolean objects are equal if and only if the current values are equal 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MutableBoolean)) {
			return false;
		}
		MutableBoolean other = (MutableBoolean) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}
	
}