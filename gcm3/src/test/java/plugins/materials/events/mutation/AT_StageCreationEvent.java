package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StageCreationEvent.class)
public final class AT_StageCreationEvent {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		assertEquals(StageCreationEvent.class, new StageCreationEvent().getPrimaryKeyValue());
	}
}
