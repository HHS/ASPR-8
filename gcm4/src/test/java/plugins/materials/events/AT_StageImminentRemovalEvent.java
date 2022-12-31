package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;

public class AT_StageImminentRemovalEvent {

	@Test
	@UnitTestConstructor(target = StageImminentRemovalEvent.class, args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}
}
