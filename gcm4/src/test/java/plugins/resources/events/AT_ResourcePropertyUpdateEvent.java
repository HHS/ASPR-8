package plugins.resources.events;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_ResourcePropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = ResourcePropertyUpdateEvent.class, args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
