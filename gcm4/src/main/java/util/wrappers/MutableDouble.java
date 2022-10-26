package util.wrappers;
/**
 * 
 * @author Shawn Hatch
 *
 */
public final class MutableDouble {
	private double value;

	/**
	 * Constructs this MutableDouble from the given value
	 */	
	public MutableDouble(double value) {		
		this.value = value;
	}
	
	
	/**
	 * Constructs this MutableDouble defaulted to zero
	 */	
	public MutableDouble() {
				
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

	public void increment(double value){
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

	public void decrement(double value){
		this.value -= value;
	}
	
	/**
	 * Sets the current value
	 */

	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Returns the current value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Returns the string representation of the current value(X) in the form
	 * 
	 * "MutableDouble [value="+ X+"]"
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableDouble [value=");
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
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Two MutableDouble objects are equal if and only if the current values are equal 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MutableDouble)) {
			return false;
		}
		MutableDouble other = (MutableDouble) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
			return false;
		}
		return true;
	}
		
}