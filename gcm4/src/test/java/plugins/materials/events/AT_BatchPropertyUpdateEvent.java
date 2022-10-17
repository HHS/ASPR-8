package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.testsupport.TestBatchPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchPropertyUpdateEvent.class)
public class AT_BatchPropertyUpdateEvent implements Event {

	@Test
	@UnitTestConstructor(args = { BatchId.class, BatchPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getBatchId", args = {})
	public void testGetBatchId() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyUpdateEvent batchPropertyUpdateEvent = new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(batchId, batchPropertyUpdateEvent.getBatchId());
	}

	@Test
	@UnitTestMethod(name = "getBatchPropertyId", args = {})
	public void testGetBatchPropertyId() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyUpdateEvent batchPropertyUpdateEvent = new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(batchPropertyId, batchPropertyUpdateEvent.getBatchPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyUpdateEvent batchPropertyUpdateEvent = new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(previousPropertyValue, batchPropertyUpdateEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyUpdateEvent batchPropertyUpdateEvent = new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		assertEquals(currentPropertyValue, batchPropertyUpdateEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		BatchId batchId = new BatchId(5348);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
		Object previousPropertyValue = 45;
		Object currentPropertyValue = 643;
		BatchPropertyUpdateEvent batchPropertyUpdateEvent = new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, currentPropertyValue);
		String expectedValue = "BatchPropertyUpdateEvent [batchId=5348, batchPropertyId=BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, previousPropertyValue=45, currentPropertyValue=643]";
		String actualValue = batchPropertyUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

}
