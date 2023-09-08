package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_BatchAmountUpdateEvent {

	@Test
	@UnitTestConstructor(target = BatchAmountUpdateEvent.class, args = { BatchId.class, double.class, double.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "batchId", args = {})
	public void testBatchId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "previousAmount", args = {})
	public void testPreviousAmount() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAmountUpdateEvent.class, name = "currentAmount", args = {})
	public void testCurrentAmount() {
		// nothing to test
	}
}
