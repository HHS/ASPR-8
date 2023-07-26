package gov.hhs.aspr.ms.gcm.plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonRemovalEvent {

	@Test
	@UnitTestConstructor(target = PersonRemovalEvent.class, args = { PersonId.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonRemovalEvent(null));
		assertEquals(contractException.getErrorType(), PersonError.NULL_PERSON_ID);
	}

	@Test
	@UnitTestMethod(target = PersonRemovalEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRemovalEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonRemovalEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = PersonRemovalEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}
	
	
}