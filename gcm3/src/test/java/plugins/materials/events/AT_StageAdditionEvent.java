package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageCreationObservationEvent.class)
public class AT_StageCreationObservationEvent implements Event {
	

	@Test
	@UnitTestConstructor(args = {StageId.class})
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name="getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(534);
		StageCreationObservationEvent stageCreationObservationEvent = new StageCreationObservationEvent(stageId);
		assertEquals(StageCreationObservationEvent.class,stageCreationObservationEvent.getPrimaryKeyValue());
	}


	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(534);
		StageCreationObservationEvent stageCreationObservationEvent = new StageCreationObservationEvent(stageId);
		assertEquals(stageId,stageCreationObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		StageId stageId = new StageId(534);
		StageCreationObservationEvent stageCreationObservationEvent = new StageCreationObservationEvent(stageId);
		assertEquals("StageCreation [stageId=534]",stageCreationObservationEvent.toString());
	}

}
