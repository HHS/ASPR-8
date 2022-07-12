package plugins.util.p2;

public class Property<T> {
	
	private static int masterId;
	private final int id;

    private final T defaultValue;

    public Property(T defaultValue) {
    	this.id = masterId++;
        this.defaultValue = defaultValue;
    }

    public T defaultValue() {
        return defaultValue;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Property)) {
			return false;
		}
		Property<?> other = (Property<?>) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
