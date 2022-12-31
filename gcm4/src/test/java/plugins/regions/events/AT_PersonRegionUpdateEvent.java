package plugins.regions.events;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import tools.annotations.UnitTestConstructor;

public class AT_PersonRegionUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonRegionUpdateEvent.class, args = { PersonId.class, RegionId.class, RegionId.class })
	public void testConstructor() {
		// nothing to test
	}
}
