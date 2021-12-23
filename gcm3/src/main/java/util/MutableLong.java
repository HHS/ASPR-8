package util;

public final class MutableLong {
	private long value;

	public MutableLong(long value) {
		
		this.value = value;
	}
	
	public MutableLong() {
				
	}
	
	public void increment(){
		value++;
	}
	
	public void increment(long value){
		this.value += value;
	}

	
	public void decrement(){
		value--;
	}
	
	public void decrement(long value){
		this.value -= value;
	}
	
	public void setValue(long value) {
		this.value = value;
	}
	
	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableLong [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

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