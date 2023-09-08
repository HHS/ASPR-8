package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_MaterialsProducerPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = MaterialsProducerPropertyDefinitionEvent.class, args = { MaterialsProducerPropertyId.class })
	public void testConstructor() {

		// precondition: null producer property id
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialsProducerPropertyDefinitionEvent(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyDefinitionEvent.class, name = "materialsProducerPropertyId", args = {})
	public void testMaterialsProducerPropertyId() {
		// nothing to test
	}

}
