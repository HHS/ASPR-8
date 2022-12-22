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
}
