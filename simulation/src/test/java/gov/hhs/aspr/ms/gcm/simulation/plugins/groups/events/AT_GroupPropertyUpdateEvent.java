package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_GroupPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = GroupPropertyUpdateEvent.class, args = { GroupId.class, GroupPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "groupId", args = {})
	public void testGroupId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "groupPropertyId", args = {})
	public void testGroupPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}

}
