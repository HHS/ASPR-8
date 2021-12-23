package plugins.groups.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.testsupport.TestGroupPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupPropertyValueAssignmentEvent.class)
public final class AT_GroupPropertyValueAssignmentEvent implements Event {

	@Test
	@UnitTestConstructor(args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(234);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = "value";
		
		GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent = new GroupPropertyValueAssignmentEvent(groupId, groupPropertyId, propertyValue);
		
		assertEquals(groupId,groupPropertyValueAssignmentEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyId", args = {})
	public void testGetGroupPropertyId() {
		GroupId groupId = new GroupId(234);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = "value";
		
		GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent = new GroupPropertyValueAssignmentEvent(groupId, groupPropertyId, propertyValue);
		
		assertEquals(groupPropertyId,groupPropertyValueAssignmentEvent.getGroupPropertyId());

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = {})
	public void testGetGroupPropertyValue() {
		GroupId groupId = new GroupId(234);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = "value";
		
		GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent = new GroupPropertyValueAssignmentEvent(groupId, groupPropertyId, propertyValue);
		
		assertEquals(propertyValue,groupPropertyValueAssignmentEvent.getGroupPropertyValue());

	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKey", args = {})
	public void testGetPrimaryKey() {
		GroupId groupId = new GroupId(234);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = "value";
		
		GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent = new GroupPropertyValueAssignmentEvent(groupId, groupPropertyId, propertyValue);
		
		assertEquals(GroupPropertyValueAssignmentEvent.class,groupPropertyValueAssignmentEvent.getPrimaryKeyValue());
	}

}
