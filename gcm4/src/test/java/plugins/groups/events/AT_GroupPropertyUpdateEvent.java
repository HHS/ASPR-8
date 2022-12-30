package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_GroupPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = GroupPropertyUpdateEvent.class,args = { GroupId.class, GroupPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
