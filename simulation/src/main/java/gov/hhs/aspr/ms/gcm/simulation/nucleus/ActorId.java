package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.Objects;

/**
 * The unique identifier for actors. Actors are constructed dynamically and ids
 * are distributed contiguously from 0.
 */
public final class ActorId {

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
	public ActorId(int id) {
		this.id = id;
	}

	/**
	 * Returns string of the form "ActorId[X]" where the value of the actor id is X
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActorId [id=");
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
     * Two {@link ActorId} instances are equal if and only if
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
		ActorId other = (ActorId) obj;
		return id == other.id;
	}
}
