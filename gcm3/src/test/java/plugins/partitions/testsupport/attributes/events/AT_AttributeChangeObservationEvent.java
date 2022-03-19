package plugins.partitions.testsupport.attributes.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.Event;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonId;

@UnitTest(target = AttributeChangeObservationEvent.class)
public class AT_AttributeChangeObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, AttributeId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getCurrentValue", args = {})
	public void testGetCurrentValue() {
		PersonId personId = new PersonId(10);
		AttributeId attributeId = TestAttributeId.INT_0;
		for (int i = 0; i < 10; i++) {
			int currentValue = i * 2 + 3;
			AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(personId, attributeId, 0, currentValue);
			assertEquals(currentValue, attributeChangeObservationEvent.getCurrentValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getAttributeId", args = {})
	public void testGetAttributeId() {
		PersonId personId = new PersonId(10);
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(personId, testAttributeId, false, true);
			assertEquals(testAttributeId, attributeChangeObservationEvent.getAttributeId());
		}

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		AttributeId attributeId = TestAttributeId.BOOLEAN_0;
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(personId, attributeId, false, true);
			assertEquals(personId, attributeChangeObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousValue", args = {})
	public void testGetPreviousValue() {
		PersonId personId = new PersonId(10);
		AttributeId attributeId = TestAttributeId.INT_0;
		for (int i = 0; i < 10; i++) {
			int previousValue = i * 2 + 3;
			AttributeChangeObservationEvent attributeChangeObservationEvent = new AttributeChangeObservationEvent(personId, attributeId, previousValue, 0);
			assertEquals(previousValue, attributeChangeObservationEvent.getPreviousValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId, new AttributeChangeObservationEvent(null, testAttributeId, null, null).getPrimaryKeyValue());
		}
	}
}
