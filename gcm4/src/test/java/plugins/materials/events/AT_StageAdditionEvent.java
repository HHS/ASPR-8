package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageAdditionEvent.class)
public class AT_StageAdditionEvent {
	

	@Test
	@UnitTestConstructor(args = {StageId.class})
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name="toString",args = {})
	public void testToString() {
		StageId stageId = new StageId(534);
		StageAdditionEvent stageAdditionEvent = new StageAdditionEvent(stageId);
		assertEquals("StageCreation [stageId=534]",stageAdditionEvent.toString());
	}

}
