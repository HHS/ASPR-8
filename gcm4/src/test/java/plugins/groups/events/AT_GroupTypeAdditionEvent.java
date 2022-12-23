package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GroupTypeAdditionEvent.class)
public class AT_GroupTypeAdditionEvent {

	@Test
	@UnitTestConstructor(args = { GroupTypeId.class })
	public void testConstructor() {
		// nothing to test
	}

}
