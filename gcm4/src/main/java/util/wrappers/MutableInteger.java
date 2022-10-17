package util.wrappers;

public final class MutableInteger {
	private int value;

	public MutableInteger(int value) {
		
		this.value = value;
	}
	
	public MutableInteger() {
				
	}
	
	public void increment(){
		value++;
	}
	
	public void increment(int value){
		this.value += value;
	}

	
	public void decrement(){
		value--;
	}
	
	public void decrement(int value){
		this.value -= value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableInteger [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

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