package plugins.partitions.testsupport.attributes.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributeValueAssignmentEvent.class)
public class AT_AttributeValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, AttributeId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {

		AttributeId attributeId = TestAttributeId.BOOLEAN_0;
		Object value = false;

		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			AttributeValueAssignmentEvent attributeValueAssignmentEvent = new AttributeValueAssignmentEvent(personId, attributeId, value);
			assertEquals(personId, attributeValueAssignmentEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getAttributeId", args = {})
	public void testGetAttributeId() {
		
		Object value = false;
		PersonId personId = new PersonId(10);
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			AttributeValueAssignmentEvent attributeValueAssignmentEvent = new AttributeValueAssignmentEvent(personId, testAttributeId, value);
			assertEquals(testAttributeId, attributeValueAssignmentEvent.getAttributeId());
		}

	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		
		PersonId personId = new PersonId(10);
		AttributeId attributeId = TestAttributeId.INT_0;
		for (int i = 0;i<10;i++) {
			Object value = i*i;
			AttributeValueAssignmentEvent attributeValueAssignmentEvent = new AttributeValueAssignmentEvent(personId, attributeId, value);
			assertEquals(value, attributeValueAssignmentEvent.getValue());
		}
	}
	
	@Test
	@UnitTestMethod(name="getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		assertEquals(AttributeValueAssignmentEvent.class,new AttributeValueAssignmentEvent(null,null,null).getPrimaryKeyValue());
	}

}
