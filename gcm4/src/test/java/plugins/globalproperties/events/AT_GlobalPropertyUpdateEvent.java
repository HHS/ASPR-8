package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_GlobalPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = GlobalPropertyUpdateEvent.class, args = { GlobalPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
