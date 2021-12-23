package plugins.regions.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonRegionAssignmentEvent.class)
public class AT_PersonRegionAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, RegionId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(6);
		RegionId regionId = TestRegionId.REGION_3;
		assertNotNull(new PersonRegionAssignmentEvent(personId, regionId));
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		PersonId personId = new PersonId(6);
		RegionId expectedRegionId = TestRegionId.REGION_3;
		PersonRegionAssignmentEvent event = new PersonRegionAssignmentEvent(personId, expectedRegionId);
		RegionId actualRegionId = event.getRegionId();
		assertEquals(expectedRegionId, actualRegionId);
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId expectedPersonId = new PersonId(6);
		RegionId regionId = TestRegionId.REGION_3;
		PersonRegionAssignmentEvent event = new PersonRegionAssignmentEvent(expectedPersonId, regionId);
		PersonId actualPersonId = event.getPersonId();
		assertEquals(expectedPersonId, actualPersonId);
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		PersonId personId = new PersonId(6);
		RegionId regionId = TestRegionId.REGION_3;
		PersonRegionAssignmentEvent event = new PersonRegionAssignmentEvent(personId, regionId);
		
		assertEquals(PersonRegionAssignmentEvent.class, event.getPrimaryKeyValue());
		// there are no precondition tests
	}

}
