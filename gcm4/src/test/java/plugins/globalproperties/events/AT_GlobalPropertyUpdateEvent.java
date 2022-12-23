package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.GlobalPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = GlobalPropertyUpdateEvent.class)

public class AT_GlobalPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { GlobalPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
