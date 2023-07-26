package plugins.partitions.testsupport.attributes.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_AttributeUpdateEvent {

	@Test
	@UnitTestConstructor(target = AttributeUpdateEvent.class, args = { PersonId.class, AttributeId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "personId", args = {})
	public void testPersonId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "attributeId", args = {})
	public void testAttributeId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "previousValue", args = {})
	public void testPreviousValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "currentValue", args = {})
	public void testCurrentValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "getPreviousValue", args = {})
	public void testGetPreviousValue() {

		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(345);
			AttributeId attributeId = TestAttributeId.INT_0;
			Integer previousValue = i;
			Integer currentValue = 45;
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, attributeId, previousValue, currentValue);
			Integer actualValue = attributeUpdateEvent.getPreviousValue();
			assertEquals(previousValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(target = AttributeUpdateEvent.class, name = "getCurrentValue", args = {})
	public void testGetCurrentValue() {
		for (int i = 0; i < 20; i++) {
			PersonId personId = new PersonId(345);
			AttributeId attributeId = TestAttributeId.INT_0;
			Integer previousValue = 12;
			Integer currentValue = i;
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, attributeId, previousValue, currentValue);
			Integer actualValue = attributeUpdateEvent.getCurrentValue();
			assertEquals(currentValue, actualValue);
		}
	}
}
