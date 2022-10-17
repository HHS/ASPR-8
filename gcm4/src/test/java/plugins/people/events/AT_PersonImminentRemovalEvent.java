package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = PersonImminentRemovalEvent.class)
public class AT_PersonImminentRemovalEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class })
	public void testConstructor() {

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonImminentRemovalEvent(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = { PersonId.class })
	public void testGetPersonId() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(personId);
			assertEquals(personId, personImminentRemovalEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonImminentRemovalEvent personImminentRemovalEvent = new PersonImminentRemovalEvent(personId);
			String expectedValue = "PersonImminentRemovalEvent [personId="+i+"]";
			String actualValue =personImminentRemovalEvent.toString();
			assertEquals(expectedValue, actualValue);
		}
		
	}
}
