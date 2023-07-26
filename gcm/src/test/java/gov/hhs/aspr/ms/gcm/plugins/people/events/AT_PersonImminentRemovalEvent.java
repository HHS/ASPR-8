package gov.hhs.aspr.ms.gcm.plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonImminentRemovalEvent {

	@Test
	@UnitTestConstructor(target = PersonImminentRemovalEvent.class, args = { PersonId.class })
	public void testConstructor() {

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonImminentRemovalEvent(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonImminentRemovalEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentRemovalEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentRemovalEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentRemovalEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

}
