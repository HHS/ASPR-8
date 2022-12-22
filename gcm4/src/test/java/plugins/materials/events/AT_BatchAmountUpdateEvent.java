package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;


@UnitTest(target = BatchAmountUpdateEvent.class)
public class AT_BatchAmountUpdateEvent {
	
	@Test
	@UnitTestConstructor(args = {BatchId.class, double.class, double.class})
	public void testConstructor() {
		//nothing to test
	}
}
