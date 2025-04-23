package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all groups
 */
@Immutable
public final class GroupId implements Comparable<GroupId> {

	private final int id;

	/**
	 * Constructs the groupId
	 * 
	 * @throws ContractException {@linkplain GroupError#NEGATIVE_GROUP_ID}
	 */
	public GroupId(int id) {
		if (id < 0) {
			throw new ContractException(GroupError.NEGATIVE_GROUP_ID);
		}
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(GroupId groupId) {
		return Integer.compare(id, groupId.id);
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
     * Two {@link GroupId} instances are equal if and only if
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
		GroupId other = (GroupId) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

}
