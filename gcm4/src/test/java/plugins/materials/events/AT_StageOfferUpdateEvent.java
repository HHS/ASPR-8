package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;

public class AT_StageOfferUpdateEvent {

	@Test
	@UnitTestConstructor(target = StageOfferUpdateEvent.class, args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		// nothing to test
	}
}
