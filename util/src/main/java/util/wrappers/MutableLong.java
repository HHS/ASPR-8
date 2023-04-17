package util.wrappers;

public final class MutableLong {
	private long value;

	/**
	 * Constructs this MutableLong from the given value
	 */	
	public MutableLong(long value) {
		
		this.value = value;
	}
	/**
	 * Constructs this MutableLong defaulted to zero
	 */	
	public MutableLong() {
				
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

	public void increment(long value){
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

	public void decrement(long value){
		this.value -= value;
	}
	/**
	 * Sets the current value
	 */
	public void setValue(long value) {
		this.value = value;
	}
	/**
	 * Returns the current value
	 */

	public long getValue() {
		return value;
	}

	/**
	 * Returns the string representation of the current value(X) in the form
	 * 
	 * "MutableLong [value="+ X+"]"
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableLong [value=");
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
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	/**
	 * Two MutableLong objects are equal if and only if the current values are equal 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MutableLong)) {
			return false;
		}
		MutableLong other = (MutableLong) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}


	
		
}