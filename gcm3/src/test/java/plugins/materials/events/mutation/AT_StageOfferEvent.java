package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = StageOfferEvent.class)
public final class AT_StageOfferEvent {
	
	@Test
	@UnitTestConstructor(args = {StageId.class, boolean.class })	
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue",args = {  })	
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(34545);
		boolean offered = false;
		StageOfferEvent stageOfferEvent = new StageOfferEvent(stageId,offered);
		assertEquals(StageOfferEvent.class,stageOfferEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId",args = { })	
	public void testGetStageId() {
		StageId stageId = new StageId(34545);
		boolean offered = false;
		StageOfferEvent stageOfferEvent = new StageOfferEvent(stageId,offered);
		assertEquals(stageId,stageOfferEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "isOffer",args = { })	
	public void testIsOffer() {
		StageId stageId = new StageId(34545);
		boolean offered = false;
		StageOfferEvent stageOfferEvent = new StageOfferEvent(stageId,offered);
		assertEquals(offered,stageOfferEvent.isOffer());
	}

}
