package plugins.resources.events;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTestConstructor;

public class AT_RegionResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = RegionResourceUpdateEvent.class, args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}
}
