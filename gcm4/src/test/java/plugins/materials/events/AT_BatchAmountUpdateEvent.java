package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTestConstructor;

public class AT_BatchAmountUpdateEvent {

	@Test
	@UnitTestConstructor(target = BatchAmountUpdateEvent.class, args = { BatchId.class, double.class, double.class })
	public void testConstructor() {
		// nothing to test
	}
}
