package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = StageImminentRemovalEvent.class)

public class AT_StageImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}
}
