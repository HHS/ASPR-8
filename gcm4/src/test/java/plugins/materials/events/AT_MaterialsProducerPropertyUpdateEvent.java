package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MaterialsProducerPropertyUpdateEvent.class)
public class AT_MaterialsProducerPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		//nothing to test
	}
}
