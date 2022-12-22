package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerPropertyUpdateEvent.class)
public class AT_MaterialsProducerPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		//nothing to test
	}
}
