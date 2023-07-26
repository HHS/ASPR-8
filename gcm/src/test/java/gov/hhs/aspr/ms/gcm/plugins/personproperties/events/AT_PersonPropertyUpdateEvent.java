package gov.hhs.aspr.ms.gcm.plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonPropertyUpdateEvent.class, args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "personPropertyId", args = {})
	public void testPersonPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}

	
	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousValue() {

		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(345);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK;
			Integer previousPropertyValue = i;
			Integer currentPropertyValue = 45;
			
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousPropertyValue, currentPropertyValue);
			Integer actualValue = personPropertyUpdateEvent.getPreviousPropertyValue();
			assertEquals(previousPropertyValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyUpdateEvent.class, name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentValue() {
		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(345);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK;
			Integer previousPropertyValue = 37;
			Integer currentPropertyValue = i;
			
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousPropertyValue, currentPropertyValue);
			Integer actualValue = personPropertyUpdateEvent.getCurrentPropertyValue();
			assertEquals(currentPropertyValue, actualValue);
		}
	}
}
