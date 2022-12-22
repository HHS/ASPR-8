package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageOfferUpdateEvent.class)
public class AT_StageOfferUpdateEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		// nothing to test
	}
}
