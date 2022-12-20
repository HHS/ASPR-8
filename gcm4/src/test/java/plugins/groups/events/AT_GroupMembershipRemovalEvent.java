package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupMembershipRemovalEvent.class)
public class AT_GroupMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		PersonId personId = new PersonId(12);
		GroupId groupId = new GroupId(23);
		GroupMembershipRemovalEvent GroupMembershipRemovalEvent = new GroupMembershipRemovalEvent(personId, groupId);
		assertEquals(groupId, GroupMembershipRemovalEvent.groupId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(12);
		GroupId groupId = new GroupId(23);
		GroupMembershipRemovalEvent GroupMembershipRemovalEvent = new GroupMembershipRemovalEvent(personId, groupId);
		assertEquals(personId, GroupMembershipRemovalEvent.personId());
	}
}
