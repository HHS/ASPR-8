package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTestConstructor;

public class AT_StageMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(target = StageMembershipRemovalEvent.class, args = { BatchId.class, StageId.class })
	public void testConstructor() {
		// nothing to test
	}
}
