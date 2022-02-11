package plugins.groups.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupConstructionEvent.class)
public class AT_GroupConstructionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { GroupConstructionInfo.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupConstructionInfo", args = {})
	public void testGetGroupConstructionInfo() {
		GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder().setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
																			.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false).build();

		GroupConstructionEvent groupConstructionEvent = new GroupConstructionEvent(groupConstructionInfo);
		assertEquals(groupConstructionInfo, groupConstructionEvent.getGroupConstructionInfo());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKey", args = {})
	public void testGetPrimaryKey() {
		GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder().setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
																			.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false).build();

		GroupConstructionEvent groupConstructionEvent = new GroupConstructionEvent(groupConstructionInfo);
		assertEquals(GroupConstructionEvent.class, groupConstructionEvent.getPrimaryKeyValue());

	}

}
