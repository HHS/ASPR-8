package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Sets property value for the given materials producer and property.
 */
@UnitTest(target = MaterialsProducerPropertyValueAssignmentEvent.class)
public final class AT_MaterialsProducerPropertyValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK;
		Object value = 5.67;
		MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent = new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId,
				materialsProducerPropertyId, value);
		assertEquals(MaterialsProducerPropertyValueAssignmentEvent.class, materialsProducerPropertyValueAssignmentEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyValue", args = {})
	public void testGetMaterialsProducerPropertyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK;
		Object value = 5.67;
		MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent = new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId,
				materialsProducerPropertyId, value);
		assertEquals(value, materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyValue());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK;
		Object value = 5.67;
		MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent = new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId,
				materialsProducerPropertyId, value);
		assertEquals(materialsProducerId, materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerId());

	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerPropertyId", args = {})
	public void testGetMaterialsProducerPropertyId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_6_DOUBLE_MUTABLE_TRACK;
		Object value = 5.67;
		MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent = new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId,
				materialsProducerPropertyId, value);
		assertEquals(materialsProducerPropertyId, materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyId());

	}

}
