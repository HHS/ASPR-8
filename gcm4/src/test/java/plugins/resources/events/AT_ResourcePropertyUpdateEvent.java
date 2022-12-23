package plugins.resources.events;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = ResourcePropertyUpdateEvent.class)

public class AT_ResourcePropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
