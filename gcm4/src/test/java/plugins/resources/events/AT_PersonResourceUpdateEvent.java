package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;


@UnitTest(target = PersonResourceUpdateEvent.class)
public class AT_PersonResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, personResourceUpdateEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, personResourceUpdateEvent.getCurrentResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(personId, personResourceUpdateEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, personResourceUpdateEvent.getPreviousResourceLevel());
	}

}
