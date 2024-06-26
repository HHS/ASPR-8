package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_PersonImminentAdditionEvent {

	@Test
	@UnitTestConstructor(target = PersonImminentAdditionEvent.class, args = { PersonId.class, PersonConstructionData.class })
	public void testConstructor() {
		PersonId personId = new PersonId(0);
		PersonConstructionData personConstructionData = PersonConstructionData.builder().build();

		ContractException contractException = assertThrows(ContractException.class, () -> new PersonImminentAdditionEvent(null, personConstructionData));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> new PersonImminentAdditionEvent(personId, null));
		assertEquals(PersonError.NULL_PERSON_CONSTRUCTION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonImminentAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentAdditionEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonImminentAdditionEvent.class, name = "personConstructionData", args = {})
	public void testPersonConstructionData() {
		// nothing to test
	}

}
