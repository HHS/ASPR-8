package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_StageOfferUpdateEvent {

	@Test
	@UnitTestConstructor(target = StageOfferUpdateEvent.class, args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "stageId", args = {})
	public void testStageId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "previousOfferState", args = {})
	public void testPreviousOfferState() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StageOfferUpdateEvent.class, name = "currentOfferState", args = {})
	public void testCurrentOfferState() {
		// nothing to test
	}
	
}
