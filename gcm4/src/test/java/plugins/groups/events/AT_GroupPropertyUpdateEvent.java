package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GroupPropertyUpdateEvent.class)
public class AT_GroupPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class, GroupPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
