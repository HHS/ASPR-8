package plugins.personproperties.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyValueAssignmentEvent.class)
public final class AT_PersonPropertyValueAssignmentEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Boolean value = false;

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent = new PersonPropertyValueAssignmentEvent(personId, personPropertyId, value);
			assertEquals(personId, personPropertyValueAssignmentEvent.getPersonId());
		}

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonId personId = new PersonId(10);
		Boolean value = false;

		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent = new PersonPropertyValueAssignmentEvent(personId, testPersonPropertyId, value);
			assertEquals(testPersonPropertyId, personPropertyValueAssignmentEvent.getPersonPropertyId());
		}

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyValue", args = {})
	public void testGetPersonPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

		for (int i = 0; i < 10; i++) {
			Integer value = i;
			PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent = new PersonPropertyValueAssignmentEvent(personId, personPropertyId, value);
			assertEquals(value, personPropertyValueAssignmentEvent.getPersonPropertyValue());
		}

	}

}
