package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionResourceUpdateEvent.class)
public class AT_RegionResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}
}
