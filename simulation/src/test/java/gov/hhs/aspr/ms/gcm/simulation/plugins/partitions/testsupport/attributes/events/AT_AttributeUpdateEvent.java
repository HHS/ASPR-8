package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_AttributeUpdateEvent {

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
