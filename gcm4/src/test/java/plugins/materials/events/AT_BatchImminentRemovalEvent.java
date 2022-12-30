package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTestConstructor;

public class AT_BatchImminentRemovalEvent {

	@Test
	@UnitTestConstructor(target = BatchImminentRemovalEvent.class, args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}
}
