package plugins.partitions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionRemovalEvent.class)
public final class AT_PartitionRemovalEvent implements Event {
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getKey", args = {})
	public void testGetKey() {
		Object expectedKey = new Object();
		PartitionRemovalEvent partitionRemovalEvent = new PartitionRemovalEvent(expectedKey);
		Object actualKey = partitionRemovalEvent.getKey();
		assertEquals(expectedKey, actualKey);
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		PartitionRemovalEvent partitionRemovalEvent = new PartitionRemovalEvent(new Object());
		assertEquals(PartitionRemovalEvent.class,partitionRemovalEvent.getPrimaryKeyValue());
	}
}
