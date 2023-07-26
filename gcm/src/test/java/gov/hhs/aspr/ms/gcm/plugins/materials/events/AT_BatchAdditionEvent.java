package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_BatchAdditionEvent {

	@Test
	@UnitTestConstructor(target = BatchAdditionEvent.class, args = { BatchId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = BatchAdditionEvent.class, name = "batchId", args = {})
	public void testBatchId() {
		// nothing to test
	}
	
}
