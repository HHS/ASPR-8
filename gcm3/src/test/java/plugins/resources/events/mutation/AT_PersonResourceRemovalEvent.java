package plugins.resources.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonResourceRemovalEvent.class)
public final class AT_PersonResourceRemovalEvent implements Event {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, PersonId.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		PersonId personId = new PersonId(123);
		long amount = 5234L;
		PersonResourceRemovalEvent personResourceRemovalEvent = new PersonResourceRemovalEvent(resourceId, personId, amount);
		assertEquals(resourceId, personResourceRemovalEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		PersonId personId = new PersonId(123);
		long amount = 5234L;
		PersonResourceRemovalEvent personResourceRemovalEvent = new PersonResourceRemovalEvent(resourceId, personId, amount);
		assertEquals(personId, personResourceRemovalEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		PersonId personId = new PersonId(123);
		long amount = 5234L;
		PersonResourceRemovalEvent personResourceRemovalEvent = new PersonResourceRemovalEvent(resourceId, personId, amount);
		assertEquals(amount, personResourceRemovalEvent.getAmount());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		PersonId personId = new PersonId(123);
		long amount = 5234L;
		PersonResourceRemovalEvent personResourceRemovalEvent = new PersonResourceRemovalEvent(resourceId, personId, amount);
		assertEquals(PersonResourceRemovalEvent.class, personResourceRemovalEvent.getPrimaryKeyValue());
	}

}
