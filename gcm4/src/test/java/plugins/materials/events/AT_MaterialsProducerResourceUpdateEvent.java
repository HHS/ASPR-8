package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MaterialsProducerResourceUpdateEvent.class)
public class AT_MaterialsProducerResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		//nothing to test
	}
}
