package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Objects;

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
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
     * Two {@link DataManagerId} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DataManagerId other = (DataManagerId) obj;
		return id == other.id;
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