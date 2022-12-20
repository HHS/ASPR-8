package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupImminentRemovalEvent.class)
public class AT_GroupImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(35);
		GroupImminentRemovalEvent groupImminentRemovalEvent = new GroupImminentRemovalEvent(groupId);
		assertEquals(groupId, groupImminentRemovalEvent.groupId());
	}
	
}
