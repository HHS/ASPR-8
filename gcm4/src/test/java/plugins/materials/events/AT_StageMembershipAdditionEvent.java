package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_StageMembershipAdditionEvent {

	@Test
	@UnitTestConstructor(target = StageMembershipAdditionEvent.class, args = { BatchId.class, StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipAdditionEvent.class, name = "batchId", args = {})
	public void testBatchId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipAdditionEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}

}
