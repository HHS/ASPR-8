package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageImminentRemovalEvent.class)

public class AT_StageImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}


	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalEvent stageImminentRemovalEvent = new StageImminentRemovalEvent(stageId);
		assertEquals(stageId, stageImminentRemovalEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalEvent stageImminentRemovalEvent = new StageImminentRemovalEvent(stageId);
		assertEquals("StageImminentRemovalEvent [stageId=4534]", stageImminentRemovalEvent.toString());
	}

}
