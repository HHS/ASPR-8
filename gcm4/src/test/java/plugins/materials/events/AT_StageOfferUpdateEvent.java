package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageOfferUpdateEvent.class)
public class AT_StageOfferUpdateEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(stageId, stageOfferUpdateEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "isPreviousOfferState", args = {})
	public void testIsPreviousOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(previousOfferState, stageOfferUpdateEvent.isPreviousOfferState());
	}

	@Test
	@UnitTestMethod(name = "isCurrentOfferState", args = {})
	public void testIsCurrentOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(currentOfferState, stageOfferUpdateEvent.isCurrentOfferState());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals("StageOfferUpdateEvent [stageId=543, previousOfferState=true, currentOfferState=false]", stageOfferUpdateEvent.toString());
	}

}
