package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.testsupport.TestGroupPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupPropertyUpdateEvent.class)
public class AT_GroupPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class, GroupPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
