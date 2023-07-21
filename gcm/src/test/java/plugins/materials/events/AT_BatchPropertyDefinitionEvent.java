package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_BatchPropertyDefinitionEvent {
	@Test
	@UnitTestConstructor(target = BatchPropertyDefinitionEvent.class, args = { MaterialId.class, BatchPropertyId.class })
	public void testConstructor() {
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;

		// precondition: null material id
		ContractException contractException = assertThrows(ContractException.class, () -> new BatchPropertyDefinitionEvent(null, batchPropertyId));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

		// precondition: null property id
		contractException = assertThrows(ContractException.class, () -> new BatchPropertyDefinitionEvent(materialId, null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionEvent.class, name = "materialId", args = {})
	public void testMaterialId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyDefinitionEvent.class, name = "batchPropertyId", args = {})
	public void testBatchPropertyId() {
		// nothing to test
	}

}
