package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_GroupMembershipAdditionEvent {

	@Test
	@UnitTestConstructor(target = GroupMembershipAdditionEvent.class, args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupMembershipAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupMembershipAdditionEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupMembershipAdditionEvent.class, name = "groupId", args = {})
	public void testGroupId() {
		// nothing to test
	}

}
