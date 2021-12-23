package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Removes a person to the group associated with the given group type and group
 * identifiers.
 *
 */
@Immutable
public final class GroupMembershipRemovalEvent implements Event {

	private final PersonId personId;

	private final GroupId groupId;

	/**
	 * @throws ContractException
	 *             <li>{@link ErrorType#} if the group id is null
	 *             <li>{@link ErrorType#} if the group id is unknown(group does
	 *             not exist)
	 *             <li>{@link ErrorType#} if the person id is null
	 *             <li>{@link ErrorType#} if the person id is unknown
	 *             <li>{@link ErrorType#} if the person is not a member of the
	 *             group
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is no either a global component, a region
	 *             or a compartment
	 */
	public GroupMembershipRemovalEvent(PersonId personId, GroupId groupId) {
		super();
		this.personId = personId;
		this.groupId = groupId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

}
