package util.wrappers;

public final class MutableBoolean {
	private boolean value;

	public MutableBoolean(boolean value) {
		
		this.value = value;
	}
	
	public MutableBoolean() {
				
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableBoolean [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value ? 1231 : 1237);
		return result;
	}

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