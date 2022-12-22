package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchAdditionEvent.class)
public class AT_BatchAdditionEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}
}
