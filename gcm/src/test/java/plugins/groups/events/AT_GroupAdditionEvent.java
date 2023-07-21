package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_GroupAdditionEvent {

	@Test
	@UnitTestConstructor(target = GroupAdditionEvent.class, args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GroupAdditionEvent.class, name = "groupId", args = {})
	public void testGroupId() {
		// nothing to test
	}
	
	
}
