package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_MaterialsProducerPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = MaterialsProducerPropertyUpdateEvent.class,args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		//nothing to test
	}
}
