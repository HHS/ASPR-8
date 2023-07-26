package nucleus;

/**
 * The unique identifier for reports.
 * 
 *
 */

public final class ReportId {

	private final int id;

	/**
	 * Returns the int id of this ActorId
	 */
	public final int getValue() {
		return id;
	}

	/**
	 * Creates an ActorId having the value id
	 */
	public ReportId(int id) {
		this.id = id;
	}

	/**
	 * Returns string of the form "ReportId [X]" where the value of the actor id
	 * is X
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportId [id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
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
	 * Report Id instances are equal if and only if their values are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportId other = (ReportId) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
