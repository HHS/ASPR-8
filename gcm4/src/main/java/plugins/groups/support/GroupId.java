package plugins.groups.support;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;


/**
 * Identifier for all groups
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GroupId implements Comparable<GroupId>{

	private final int id;

	/**
	 * Constructs the groupId
	 * 
	 * @throws ContractException
	 * <li>{@linkplain GroupError#NEGATIVE_GROUP_ID} </li>
	 */
	public GroupId(int id) {
		if(id<0) {
			throw new ContractException(GroupError.NEGATIVE_GROUP_ID);
		}
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
		if (!(obj instanceof GroupId)) {
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
