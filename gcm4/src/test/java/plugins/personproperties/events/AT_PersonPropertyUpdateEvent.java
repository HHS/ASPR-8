package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyUpdateEvent.class)
public class AT_PersonPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		for (int i = 0; i < 10; i++) {
			Object currentValue = i;
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(currentValue, personPropertyUpdateEvent.currentPropertyValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonId personId = new PersonId(10);
		Object previousValue = 0;
		Object currentValue = 1;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, testPersonPropertyId, previousValue, currentValue);
			assertEquals(testPersonPropertyId, personPropertyUpdateEvent.personPropertyId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(personId, personPropertyUpdateEvent.personId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			Object previousValue = i;
			PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(previousValue, personPropertyUpdateEvent.previousPropertyValue());
		}

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, previousValue, currentValue);
		String actualValue = personPropertyUpdateEvent.toString();
		String expectedValue = "PersonPropertyUpdateEvent [personId=10, personPropertyId=PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, previousPropertyValue=0, currentPropertyValue=1]";
		assertEquals(actualValue, expectedValue);
	}


}
