package plugins.resources.events;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTestConstructor;

public class AT_PersonResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonResourceUpdateEvent.class, args = { PersonId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}
}
