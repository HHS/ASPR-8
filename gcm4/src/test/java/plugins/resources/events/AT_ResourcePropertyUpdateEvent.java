package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ResourcePropertyUpdateEvent.class)

public class AT_ResourcePropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyUpdateEvent resourcePropertyUpdateEvent = new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(resourceId, resourcePropertyUpdateEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyId", args = {})
	public void testGetResourcePropertyId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyUpdateEvent resourcePropertyUpdateEvent = new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(resourcePropertyId, resourcePropertyUpdateEvent.getResourcePropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyUpdateEvent resourcePropertyUpdateEvent = new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(previousValue, resourcePropertyUpdateEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyUpdateEvent resourcePropertyUpdateEvent = new ResourcePropertyUpdateEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(currentValue, resourcePropertyUpdateEvent.getCurrentPropertyValue());
	}


}
