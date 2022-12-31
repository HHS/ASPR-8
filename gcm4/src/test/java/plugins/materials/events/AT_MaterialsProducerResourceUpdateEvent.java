package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.resources.support.ResourceId;
import tools.annotations.UnitTestConstructor;

public class AT_MaterialsProducerResourceUpdateEvent {

	@Test
	@UnitTestConstructor(target = MaterialsProducerResourceUpdateEvent.class, args = { MaterialsProducerId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}
}
