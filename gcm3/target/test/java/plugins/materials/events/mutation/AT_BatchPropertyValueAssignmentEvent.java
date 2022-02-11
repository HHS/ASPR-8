package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.testsupport.TestBatchPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BatchPropertyValueAssignmentEvent.class)
public final class AT_BatchPropertyValueAssignmentEvent {

	@Test
	@UnitTestConstructor(args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {  })
	public void testGetBatchId() {
		BatchId batchId = new BatchId(56);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
		Object value = 6457.6;
		BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent = new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value);
		assertEquals(batchId, batchPropertyValueAssignmentEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyId", args = {  })
	public void testGetBatchPropertyId() {
		BatchId batchId = new BatchId(56);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
		Object value = 6457.6;
		BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent = new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value);
		assertEquals(batchPropertyId, batchPropertyValueAssignmentEvent.getBatchPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyValue", args = {  })
	public void testGetBatchPropertyValue() {
		BatchId batchId = new BatchId(56);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
		Object value = 6457.6;
		BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent = new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value);
		assertEquals(value, batchPropertyValueAssignmentEvent.getBatchPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { })
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(56);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK;
		Object value = 6457.6;
		BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent = new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, value);
		assertEquals(BatchPropertyValueAssignmentEvent.class, batchPropertyValueAssignmentEvent.getPrimaryKeyValue());
	}

}
