package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Adds a person to the group associated with the given group type and group
 * identifiers.
 * 
 */
@Immutable
public final class GroupMembershipAdditionEvent implements Event {

	private final PersonId personId;

	private final GroupId groupId;

	/**
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#DUPLICATE_GROUP_MEMBERSHIP} if the
	 *             person is already a member of the group
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component
	 */
	public GroupMembershipAdditionEvent(PersonId personId, GroupId groupId) {
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
