package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ResourcePropertyUpdateEvent.class)

public class AT_ResourcePropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
