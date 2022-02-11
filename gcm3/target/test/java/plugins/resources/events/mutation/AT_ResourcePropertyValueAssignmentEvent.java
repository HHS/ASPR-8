package plugins.resources.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = ResourcePropertyValueAssignmentEvent.class)
public final class AT_ResourcePropertyValueAssignmentEvent implements Event {
	
	@Test
	@UnitTestConstructor(args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testContstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE;
		Object value = 12.6;
		
		ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent = new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, value);
		assertEquals(resourceId, resourcePropertyValueAssignmentEvent.getResourceId());
		
	}
	
	@Test
	@UnitTestMethod(name = "getResourcePropertyId", args = {})
	public void testGetResourcePropertyId() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE;
		Object value = 12.6;
		
		ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent = new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, value);
		assertEquals(resourcePropertyId, resourcePropertyValueAssignmentEvent.getResourcePropertyId());

	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyValue", args = {})
	public void testGetResourcePropertyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE;
		Object value = 12.6;
		
		ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent = new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, value);
		assertEquals(value, resourcePropertyValueAssignmentEvent.getResourcePropertyValue());

	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE;
		Object value = 12.6;
		
		ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent = new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, value);
		assertEquals(ResourcePropertyValueAssignmentEvent.class, resourcePropertyValueAssignmentEvent.getPrimaryKeyValue());

	}

}
