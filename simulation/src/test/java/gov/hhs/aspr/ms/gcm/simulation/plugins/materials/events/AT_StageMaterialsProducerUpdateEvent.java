package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_StageMaterialsProducerUpdateEvent {

	@Test
	@UnitTestConstructor(target = StageMaterialsProducerUpdateEvent.class, args = { StageId.class, MaterialsProducerId.class, MaterialsProducerId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "previousMaterialsProducerId", args = {})
	public void testPreviousMaterialsProducerId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMaterialsProducerUpdateEvent.class, name = "currentMaterialsProducerId", args = {})
	public void testCurrentMaterialsProducerId() {
		// nothing to test
	}

}