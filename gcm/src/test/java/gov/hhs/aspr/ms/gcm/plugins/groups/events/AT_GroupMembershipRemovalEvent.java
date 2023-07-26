package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_GroupMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(target = GroupMembershipRemovalEvent.class, args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipRemovalEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipRemovalEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupMembershipRemovalEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipRemovalEvent.class, name = "groupId", args = {})
	public void testGroupId() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupMembershipRemovalEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

}
