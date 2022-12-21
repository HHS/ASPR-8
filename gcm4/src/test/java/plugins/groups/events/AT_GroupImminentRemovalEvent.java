package plugins.groups.events;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GroupImminentRemovalEvent.class)
public class AT_GroupImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}
	
}
