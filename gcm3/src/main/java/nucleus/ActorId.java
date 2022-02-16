package nucleus;

/**
 * The unique identifier for agents. Agents are constructed dynamically and ids
 * are distributed contiguously from 0.
 * 
 * @author Shawn Hatch
 *
 */

public final class AgentId {

	private final int id;

	/**
	 * Returns the int id of this AgentId
	 */
	public final int getValue() {
		return id;
	}

	/**
	 * Creates an AgentId having the value id
	 */
	public AgentId(int id) {
		this.id = id;
	}

	/**
	 * Returns string of the form "AgentId[X]" where the value of the agent id
	 * is X
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AgentId [id=");
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
	 * Agent Id instances are equal if and only if their values are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentId other = (AgentId) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
