package plugins.materials.events;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialsError;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = BatchPropertyUpdateEvent.class)
public class AT_BatchPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class, BatchPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;

		// test case: null batch id
		ContractException batchContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(
				null,
				batchPropertyId,
				previousPropertyValue,
				currentPropertyValue));
		assertEquals(MaterialsError.NULL_BATCH_ID, batchContractException.getErrorType());

		// test case: null batch property id
		ContractException propContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(
				batchId,
				null,
				previousPropertyValue,
				currentPropertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, propContractException.getErrorType());

		// test case: null previous property value
		ContractException prevContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(
				batchId,
				batchPropertyId,
				null,
				currentPropertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, prevContractException.getErrorType());

		// test case: null current property value
		ContractException currContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(
				batchId,
				batchPropertyId,
				previousPropertyValue,
				null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, currContractException.getErrorType());

		// test to assert that the builder's output is not null
		assertNotNull(new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue));

	}
}
