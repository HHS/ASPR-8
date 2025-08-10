package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Objects;

/**
 * The unique identifier for reports.
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
	 * Returns string of the form "ReportId [X]" where the value of the actor id is
	 * X
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
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
     * Two {@link ReportId} instances are equal if and only if
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
		ReportId other = (ReportId) obj;
		return id == other.id;
	}

}
