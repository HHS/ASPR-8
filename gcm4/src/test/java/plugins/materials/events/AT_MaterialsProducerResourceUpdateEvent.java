package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerResourceUpdateEvent.class)
public class AT_MaterialsProducerResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		String expectedValue = "MaterialsProducerResourceUpdateEvent [materialsProducerId=MATERIALS_PRODUCER_3, resourceId=RESOURCE_4, previousResourceLevel=23, currentResourceLevel=346]";
		String actualValue = materialsProducerResourceUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

}
