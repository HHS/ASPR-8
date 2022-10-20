package plugins.globalproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GlobalPropertyUpdateEvent.class)

public class AT_GlobalPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { GlobalPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Integer previousValue = 12;
		Integer currentValue = 13;
		GlobalPropertyUpdateEvent globalPropertyUpdateEvent = new GlobalPropertyUpdateEvent(globalPropertyId, previousValue, currentValue);

		assertNotNull(globalPropertyUpdateEvent);

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyId", args = {})
	public void testGetGlobalPropertyId() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Integer previousValue = 12;
		Integer currentValue = 13;
		GlobalPropertyUpdateEvent globalPropertyUpdateEvent = new GlobalPropertyUpdateEvent(globalPropertyId, previousValue, currentValue);

		assertEquals(globalPropertyId, globalPropertyUpdateEvent.getGlobalPropertyId());

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyId", args = { })
	public void testGetPreviousPropertyValue() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Integer previousValue = 12;
		Integer currentValue = 13;
		GlobalPropertyUpdateEvent globalPropertyUpdateEvent = new GlobalPropertyUpdateEvent(globalPropertyId, previousValue, currentValue);

		assertEquals(previousValue, globalPropertyUpdateEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Integer previousValue = 12;
		Integer currentValue = 13;
		GlobalPropertyUpdateEvent globalPropertyUpdateEvent = new GlobalPropertyUpdateEvent(globalPropertyId, previousValue, currentValue);

		assertEquals(currentValue, globalPropertyUpdateEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("id");
		Integer previousValue = 12;
		Integer currentValue = 13;
		GlobalPropertyUpdateEvent globalPropertyUpdateEvent = new GlobalPropertyUpdateEvent(globalPropertyId, previousValue, currentValue);
		String expectedValue = "GlobalPropertyUpdateEvent [globalPropertyId=id, previousPropertyValue=12, currentPropertyValue=13]";
		String actualValue = globalPropertyUpdateEvent.toString();

		assertEquals(expectedValue, actualValue);

	}

}
