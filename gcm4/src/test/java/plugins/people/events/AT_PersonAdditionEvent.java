package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonAdditionEvent {

	@Test
	@UnitTestConstructor(target = PersonAdditionEvent.class, args = { PersonId.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonAdditionEvent(null));
		assertEquals(contractException.getErrorType(), PersonError.NULL_PERSON_ID);
	}

	@Test
	@UnitTestMethod(target = PersonAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonAdditionEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

}