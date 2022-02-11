package plugins.globals.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalPropertyValueAssignmentEvent.class)
public final class AT_GlobalPropertyValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { GlobalPropertyId.class, Object.class })
	public void testConstructor() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Double value = 345.53;
		assertNotNull(new GlobalPropertyValueAssignmentEvent(globalPropertyId, value));
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyId", args = {})
	public void testGetGlobalPropertyId() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Double value = 345.53;
		GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent = new GlobalPropertyValueAssignmentEvent(globalPropertyId, value);

		assertEquals(globalPropertyId, globalPropertyValueAssignmentEvent.getGlobalPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyValue", args = {})
	public void testGetGlobalPropertyValue() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Double value = 345.53;
		GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent = new GlobalPropertyValueAssignmentEvent(globalPropertyId, value);
		
		assertEquals(value, globalPropertyValueAssignmentEvent.getGlobalPropertyValue());

	}

}
