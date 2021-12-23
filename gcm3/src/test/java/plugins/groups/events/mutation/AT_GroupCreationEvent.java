package plugins.groups.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupTypeId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupCreationEvent.class)
public final class AT_GroupCreationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { GroupTypeId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeId", args = {})
	public void testGetGroupTypeId() {
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			GroupCreationEvent groupCreationEvent = new GroupCreationEvent(testGroupTypeId);			
			assertEquals(testGroupTypeId, groupCreationEvent.getGroupTypeId());
		}
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKey", args = {})
	public void testGetPrimaryKey() {
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			GroupCreationEvent groupCreationEvent = new GroupCreationEvent(testGroupTypeId);			
			assertEquals(GroupCreationEvent.class, groupCreationEvent.getPrimaryKeyValue());
		}
	}


}
