package util.wrappers;

public final class MutableDouble {
	private double value;

	public MutableDouble(double value) {
		
		this.value = value;
	}
	
	public MutableDouble() {
				
	}
	
	public void increment(){
		value++;
	}
	
	public void increment(double value){
		this.value += value;
	}

	
	public void decrement(){
		value--;
	}
	
	public void decrement(double value){
		this.value -= value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableDouble [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

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