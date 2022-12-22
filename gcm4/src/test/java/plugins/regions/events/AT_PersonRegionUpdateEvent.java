package plugins.regions.events;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = PersonRegionUpdateEvent.class)
public class AT_PersonRegionUpdateEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, RegionId.class, RegionId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionUpdateEvent event = new PersonRegionUpdateEvent(personId, previousRegionId, currentRegionId);
		assertNotNull(event);
	}
}
