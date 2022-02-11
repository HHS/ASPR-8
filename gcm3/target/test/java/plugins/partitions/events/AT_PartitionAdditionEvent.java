package plugins.partitions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.partitions.support.Partition;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionAdditionEvent.class)
public final class AT_PartitionAdditionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { Partition.class, Object.class })
	public void testConstructor() {	
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPartition", args = {})
	public void testGetPartition() {
		Partition expectedPartition = Partition.builder().build();
		Object key = new Object();
		PartitionAdditionEvent partitionAdditionEvent = new PartitionAdditionEvent(expectedPartition, key);
		Partition actualPartition = partitionAdditionEvent.getPartition();
		assertEquals(expectedPartition, actualPartition);
	}

	@Test
	@UnitTestMethod(name = "getKey", args = {})
	public void testGetKey() {
		Partition partition = Partition.builder().build();
		Object expectedKey = new Object();
		PartitionAdditionEvent partitionAdditionEvent = new PartitionAdditionEvent(partition, expectedKey);
		Object actualKey = partitionAdditionEvent.getKey();
		assertEquals(expectedKey, actualKey);
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		Partition partition = Partition.builder().build();
		Object key = new Object();
		PartitionAdditionEvent partitionAdditionEvent = new PartitionAdditionEvent(partition, key);		
		assertEquals(PartitionAdditionEvent.class, partitionAdditionEvent.getPrimaryKeyValue());
		
	}
}
