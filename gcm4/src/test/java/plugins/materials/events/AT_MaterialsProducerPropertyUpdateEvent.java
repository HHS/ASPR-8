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

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerId, materialsProducerPropertyUpdateEvent.materialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyId", args = {})
	public void testGetMaterialsProducerPropertyId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyUpdateEvent.materialsProducerPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(previousPropertyValue, materialsProducerPropertyUpdateEvent.previousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(currentPropertyValue, materialsProducerPropertyUpdateEvent.currentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 896.5;
		Object currentPropertyValue = 3762.87;
		MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent = new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
				materialsProducerPropertyId, previousPropertyValue, currentPropertyValue);
		
		String expectedValue = "MaterialsProducerPropertyUpdateEvent [materialsProducerId=MATERIALS_PRODUCER_3, materialsProducerPropertyId=MATERIALS_PRODUCER_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, previousPropertyValue=896.5, currentPropertyValue=3762.87]";
		String actualValue = materialsProducerPropertyUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

}
