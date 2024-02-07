package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_MaterialsProducerResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = MaterialsProducerResourceUpdateEvent.class, args = { MaterialsProducerId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "materialsProducerId", args = {})
	public void testMaterialsProducerId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "previousResourceLevel", args = {})
	public void testPreviousResourceLevel() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceUpdateEvent.class, name = "currentResourceLevel", args = {})
	public void testCurrentResourceLevel() {
		// nothing to test
	}

}
