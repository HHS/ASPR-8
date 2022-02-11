package plugins.people.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonRemovalRequestEvent.class)
public final class AT_PersonRemovalRequestEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class })
	public void testConstructor() {
		// nothing to test		
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = { PersonId.class })
	public void testGetPersonId() {
		PersonId personId = new PersonId(45);
		PersonRemovalRequestEvent personRemovalRequestEvent = new PersonRemovalRequestEvent(personId);
		
		assertEquals(personId, personRemovalRequestEvent.getPersonId());
	}

}
