package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PersonPropertyUpdateEvent {

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
