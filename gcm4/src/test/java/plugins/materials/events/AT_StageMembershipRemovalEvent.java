package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_StageMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(target = StageMembershipRemovalEvent.class, args = { BatchId.class, StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipRemovalEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipRemovalEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipRemovalEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipRemovalEvent.class, name = "batchId", args = {})
	public void testBatchId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageMembershipRemovalEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}

}
