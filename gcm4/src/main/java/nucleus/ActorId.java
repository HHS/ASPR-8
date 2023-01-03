package nucleus;

/**
 * The unique identifier for actors. Actors are constructed dynamically and ids
 * are distributed contiguously from 0.
 * 
 *
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
	 * Returns string of the form "ActorId[X]" where the value of the actor id
	 * is X
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
	 * Actor Id instances are equal if and only if their values are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActorId other = (ActorId) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
