package gov.hhs.aspr.ms.gcm.simulation.nucleus;

/**
 * The unique identifier for data managers. DataManagerId values are constructed
 * dynamically and are distributed contiguously from 0.
 */
public final class DataManagerId implements Comparable<DataManagerId> {
	private final int id;

	/**
	 * Creates a DataManagerId having the value id
	 */
	public DataManagerId(int id) {
		this.id = id;
	}

	/**
	 * Returns the int id of this DataManagerId
	 */
	public final int getValue() {
		return id;
	}

	/**
	 * Standard hash code implementation
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/**
	 * DataManagerId instances are equal if and only if their values are equal
	 */
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

	/**
	 * Returns string of the form "DataManager[X]" where the value of the data
	 * manager id is X
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataManagerId [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(DataManagerId dataManagerId) {
		return Integer.compare(this.id, dataManagerId.id);
	}

}