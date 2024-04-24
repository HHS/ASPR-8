package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

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
