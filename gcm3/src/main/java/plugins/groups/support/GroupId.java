package plugins.groups.support;

import net.jcip.annotations.Immutable;
import plugins.people.support.PersonId;


/**
 * Identifier for all groups
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GroupId implements Comparable<GroupId>{

	private final int id;

	public GroupId(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(GroupId groupId) {
		return Integer.compare(id,groupId.id);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonId)) {
			return false;
		}
		GroupId other = (GroupId) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
	
	@Override	
	public String toString() {
		return Integer.toString(id);
	}

}
