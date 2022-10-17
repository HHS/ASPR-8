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

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		GroupId groupId = new GroupId(30);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = "previous";
		Object currentValue = "current";
		GroupPropertyUpdateEvent groupPropertyUpdateEvent = new GroupPropertyUpdateEvent(groupId, groupPropertyId, previousValue, currentValue);
		assertEquals(currentValue, groupPropertyUpdateEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(30);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = "previous";
		Object currentValue = "current";
		GroupPropertyUpdateEvent groupPropertyUpdateEvent = new GroupPropertyUpdateEvent(groupId, groupPropertyId, previousValue, currentValue);
		assertEquals(groupId, groupPropertyUpdateEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyId", args = {})
	public void testGetGroupPropertyId() {
		GroupId groupId = new GroupId(30);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = "previous";
		Object currentValue = "current";
		GroupPropertyUpdateEvent groupPropertyUpdateEvent = new GroupPropertyUpdateEvent(groupId, groupPropertyId, previousValue, currentValue);
		assertEquals(groupPropertyId, groupPropertyUpdateEvent.getGroupPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		GroupId groupId = new GroupId(30);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = "previous";
		Object currentValue = "current";
		GroupPropertyUpdateEvent groupPropertyUpdateEvent = new GroupPropertyUpdateEvent(groupId, groupPropertyId, previousValue, currentValue);
		assertEquals(previousValue, groupPropertyUpdateEvent.getPreviousPropertyValue());
	}

}
