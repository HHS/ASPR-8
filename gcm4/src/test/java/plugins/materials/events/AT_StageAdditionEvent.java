package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_StageAdditionEvent {

	@Test
	@UnitTestConstructor(target = StageAdditionEvent.class, args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageAdditionEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}

}
