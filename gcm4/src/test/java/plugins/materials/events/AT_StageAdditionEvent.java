package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;

public class AT_StageAdditionEvent {

	@Test
	@UnitTestConstructor(target = StageAdditionEvent.class, args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}
}
