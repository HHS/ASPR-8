package plugins.groups.events;


import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GroupAdditionEvent.class)
public class AT_GroupAdditionEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

}
