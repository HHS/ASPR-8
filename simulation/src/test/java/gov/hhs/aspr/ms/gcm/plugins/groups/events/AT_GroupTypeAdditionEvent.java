package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_GroupTypeAdditionEvent {

	@Test
	@UnitTestConstructor(target = GroupTypeAdditionEvent.class, args = { GroupTypeId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupTypeAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupTypeAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupTypeAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupTypeAdditionEvent.class, name = "groupTypeId", args = {})
	public void testGroupTypeId() {
		// nothing to test
	}
	
}
