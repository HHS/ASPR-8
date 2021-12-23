package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StageRemovalRequestEvent.class)
public final class AT_StageRemovalRequestEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, boolean.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { })
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(75);
		boolean destroyBatches = true;		
		StageRemovalRequestEvent stageRemovalRequestEvent = new StageRemovalRequestEvent(stageId, destroyBatches);
		assertEquals(StageRemovalRequestEvent.class,stageRemovalRequestEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(75);
		boolean destroyBatches = true;		
		StageRemovalRequestEvent stageRemovalRequestEvent = new StageRemovalRequestEvent(stageId, destroyBatches);
		assertEquals(stageId,stageRemovalRequestEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "isDestroyBatches", args = {})
	public void testIsDestroyBatches() {
		StageId stageId = new StageId(75);
		boolean destroyBatches = true;		
		StageRemovalRequestEvent stageRemovalRequestEvent = new StageRemovalRequestEvent(stageId, destroyBatches);
		assertEquals(destroyBatches,stageRemovalRequestEvent.isDestroyBatches());
	}

}
