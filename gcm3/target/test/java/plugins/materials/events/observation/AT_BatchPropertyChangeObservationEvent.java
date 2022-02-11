package plugins.materials.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.testsupport.TestBatchPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = BatchPropertyChangeObservationEvent.class)
public class AT_BatchPropertyChangeObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { BatchId.class, BatchPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(BatchPropertyChangeObservationEvent.class, batchPropertyChangeObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(batchId, batchPropertyChangeObservationEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyId", args = {})
	public void testGetBatchPropertyId() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(batchPropertyId, batchPropertyChangeObservationEvent.getBatchPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(previousPropertyValue, batchPropertyChangeObservationEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(currentPropertyValue, batchPropertyChangeObservationEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent = new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		String expectedValue = "BatchPropertyChangeObservationEvent [batchId=5348, batchPropertyId=BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, previousPropertyValue=45, currentPropertyValue=643]";
		String actualValue = batchPropertyChangeObservationEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

}
