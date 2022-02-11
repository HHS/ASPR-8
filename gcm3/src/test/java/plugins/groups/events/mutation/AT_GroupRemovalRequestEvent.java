package plugins.groups.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.groups.support.GroupId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = GroupRemovalRequestEvent.class)
public final class AT_GroupRemovalRequestEvent implements Event {
	

	@Test
	@UnitTestConstructor(args = {GroupId.class})
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name ="getGroupId", args = {})
	public void testGetGroupId() {
		for(int i = 9;i<10;i++) {
			GroupId groupId = new GroupId(i);
			GroupRemovalRequestEvent groupRemovalRequestEvent = new GroupRemovalRequestEvent(groupId);
			assertEquals(groupId, groupRemovalRequestEvent.getGroupId());
		}
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKey", args = {})
	public void testGetPrimaryKey() {
		for(int i = 9;i<10;i++) {
			GroupId groupId = new GroupId(i);
			GroupRemovalRequestEvent groupRemovalRequestEvent = new GroupRemovalRequestEvent(groupId);
			assertEquals(GroupRemovalRequestEvent.class, groupRemovalRequestEvent.getPrimaryKeyValue());
		}
	}

}
