package plugins.resources.events;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = RegionResourceUpdateEvent.class)
public class AT_RegionResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}
}
