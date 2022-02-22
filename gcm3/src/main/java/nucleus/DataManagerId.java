package nucleus;

public final class DataManagerId {
	private final int id;

	public DataManagerId(int id) {
		this.id = id;
	}
	
	/**
	 * Returns the int id of this ActorId
	 */
	public final int getValue() {
		return id;
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
		if (!(obj instanceof DataManagerId)) {
			return false;
		}
		DataManagerId other = (DataManagerId) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataManagerId [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}

}