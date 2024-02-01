package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestBatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_BatchPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = BatchPropertyUpdateEvent.class, args = { BatchId.class, BatchPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;

		// test case: null batch id
		ContractException batchContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(null, batchPropertyId, previousPropertyValue, currentPropertyValue));
		assertEquals(MaterialsError.NULL_BATCH_ID, batchContractException.getErrorType());

		// test case: null batch property id
		ContractException propContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(batchId, null, previousPropertyValue, currentPropertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_ID, propContractException.getErrorType());

		// test case: null previous property value
		ContractException prevContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(batchId, batchPropertyId, null, currentPropertyValue));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, prevContractException.getErrorType());

		// test case: null current property value
		ContractException currContractException = assertThrows(ContractException.class, () -> new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, currContractException.getErrorType());

		// test to assert that the builder's output is not null
		assertNotNull(new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue));

	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "batchId", args = {})
	public void testBatchId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "batchPropertyId", args = {})
	public void testBatchPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "previousPropertyValue", args = {})
	public void testPreviousPropertyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchPropertyUpdateEvent.class, name = "currentPropertyValue", args = {})
	public void testCurrentPropertyValue() {
		// nothing to test
	}
}
