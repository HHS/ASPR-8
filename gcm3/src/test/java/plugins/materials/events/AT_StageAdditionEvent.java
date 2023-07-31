package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageAdditionEvent.class)
public class AT_StageAdditionEvent implements Event {
	

	@Test
	@UnitTestConstructor(args = {StageId.class})
	public void testConstructor() {
		//nothing to test
	}
	
	@Test
	@UnitTestMethod(name="getPrimaryKeyValue",args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(534);
		StageAdditionEvent stageAdditionEvent = new StageAdditionEvent(stageId);
		assertEquals(StageAdditionEvent.class,stageAdditionEvent.getPrimaryKeyValue());
	}


	@Test
	@UnitTestMethod(name="getStageId",args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(534);
		StageAdditionEvent stageAdditionEvent = new StageAdditionEvent(stageId);
		assertEquals(stageId,stageAdditionEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		StageId stageId = new StageId(534);
		StageAdditionEvent stageAdditionEvent = new StageAdditionEvent(stageId);
		assertEquals("StageCreation [stageId=534]",stageAdditionEvent.toString());
	}

}