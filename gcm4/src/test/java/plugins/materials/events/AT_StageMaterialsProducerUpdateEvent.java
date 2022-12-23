package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = StageMaterialsProducerUpdateEvent.class)
public class AT_StageMaterialsProducerUpdateEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, MaterialsProducerId.class, MaterialsProducerId.class })
	public void testConstructor() {
		// nothing to test
	}
}