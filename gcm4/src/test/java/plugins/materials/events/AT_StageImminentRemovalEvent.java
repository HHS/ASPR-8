package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

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
