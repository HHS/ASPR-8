package plugins.groups.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupMembershipAdditionEvent.class)
public final class AT_GroupMembershipAdditionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(5);
		GroupId groupId = new GroupId(10);
		GroupMembershipAdditionEvent groupMembershipAdditionEvent = new GroupMembershipAdditionEvent(personId, groupId);
		assertEquals(personId, groupMembershipAdditionEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		PersonId personId = new PersonId(5);
		GroupId groupId = new GroupId(10);
		GroupMembershipAdditionEvent groupMembershipAdditionEvent = new GroupMembershipAdditionEvent(personId, groupId);
		assertEquals(groupId, groupMembershipAdditionEvent.getGroupId());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKey", args = {})
	public void testGetPrimaryKey() {
		PersonId personId = new PersonId(5);
		GroupId groupId = new GroupId(10);
		GroupMembershipAdditionEvent groupMembershipAdditionEvent = new GroupMembershipAdditionEvent(personId, groupId);
		assertEquals(GroupMembershipAdditionEvent.class, groupMembershipAdditionEvent.getPrimaryKeyValue());
	}

}
