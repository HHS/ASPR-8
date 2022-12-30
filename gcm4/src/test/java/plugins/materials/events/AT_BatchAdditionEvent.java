package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTestConstructor;

public class AT_BatchAdditionEvent {

	@Test
	@UnitTestConstructor(target = BatchAdditionEvent.class, args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}
}
