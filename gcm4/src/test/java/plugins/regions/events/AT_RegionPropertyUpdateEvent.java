package plugins.regions.events;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_RegionPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = RegionPropertyUpdateEvent.class, args = { RegionId.class, RegionPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
