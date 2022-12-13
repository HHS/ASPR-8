package plugins.partitions.testsupport.attributes.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = AttributeUpdateEvent.class)
public class AT_AttributeUpdateEvent {

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
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, attributeId, 0, currentValue);
			assertEquals(currentValue, attributeUpdateEvent.getCurrentValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getAttributeId", args = {})
	public void testGetAttributeId() {
		PersonId personId = new PersonId(10);
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, testAttributeId, false, true);
			assertEquals(testAttributeId, attributeUpdateEvent.getAttributeId());
		}

	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		AttributeId attributeId = TestAttributeId.BOOLEAN_0;
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, attributeId, false, true);
			assertEquals(personId, attributeUpdateEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousValue", args = {})
	public void testGetPreviousValue() {
		PersonId personId = new PersonId(10);
		AttributeId attributeId = TestAttributeId.INT_0;
		for (int i = 0; i < 10; i++) {
			int previousValue = i * 2 + 3;
			AttributeUpdateEvent attributeUpdateEvent = new AttributeUpdateEvent(personId, attributeId, previousValue, 0);
			assertEquals(previousValue, attributeUpdateEvent.getPreviousValue());
		}
	}

}
