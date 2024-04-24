package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_StageImminentRemovalEvent {

	@Test
	@UnitTestConstructor(target = StageImminentRemovalEvent.class, args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageImminentRemovalEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageImminentRemovalEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageImminentRemovalEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageImminentRemovalEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}
}
