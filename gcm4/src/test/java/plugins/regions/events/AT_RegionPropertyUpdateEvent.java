package plugins.regions.events;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = RegionPropertyUpdateEvent.class)
public class AT_RegionPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { RegionId.class, RegionPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// Nothing to test here. All fields covered by other tests.
	}
}
