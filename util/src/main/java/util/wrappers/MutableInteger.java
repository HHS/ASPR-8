package util.wrappers;

public final class MutableInteger {
	private int value;

	/**
	 * Constructs this MutableInteger from the given value
	 */	
	public MutableInteger(int value) {
		
		this.value = value;
	}
	/**
	 * Constructs this MutableInteger defaulted to zero
	 */	
	public MutableInteger() {
				
	}
	/**
	 * Increments the current value by one
	 */

	public void increment(){
		value++;
	}
	/**
	 * Increments the current value by the given value
	 */

	public void increment(int value){
		this.value += value;
	}

	/**
	 * Decrements the current value by one
	 */

	public void decrement(){
		value--;
	}
	/**
	 * Decrements the current value by the given value
	 */

	public void decrement(int value){
		this.value -= value;
	}
	/**
	 * Sets the current value
	 */
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * Returns the current value
	 */

	public int getValue() {
		return value;
	}

	/**
	 * Returns the string representation of the current value(X) in the form
	 * 
	 * "MutableInteger [value="+ X+"]"
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableInteger [value=");
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
		result = prime * result + value;
		return result;
	}

	/**
	 * Two MutableInteger objects are equal if and only if the current values are equal 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MutableInteger)) {
			return false;
		}
		MutableInteger other = (MutableInteger) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}
		
}