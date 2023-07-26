package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_MaterialsProducerPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = MaterialsProducerPropertyUpdateEvent.class, args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "materialsProducerId", args = {})
	public void testMaterialsProducerId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "materialsProducerPropertyId", args = {})
	public void testMaterialsProducerPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}

}
