package plugins.partitions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.filters.FalseFilter;
import plugins.partitions.support.filters.TrueFilter;
import plugins.partitions.testsupport.FunctionalAttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PartitionsPluginData {

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getPartition", args = { Object.class })
	public void testGetPartition() {
		Object key1 = "key1";
		Partition partition1 = Partition.builder()//
										.setFilter(new TrueFilter())//
										.build();

		Object key2 = "key2";
		Partition partition2 = Partition.builder()//
										.setFilter(new FalseFilter())//
										.build();

		Object key3 = "key3";
		Partition partition3 = Partition.builder()//
										.setFilter(new FalseFilter().or(new TrueFilter()))//
										.build();

		Object key4 = "key4";
		Partition partition4 = Partition.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, (a) -> 5)).build();

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()//
							.setPartition(key1, partition4)//
							.setPartition(key1, partition1)//
							.setPartition(key2, partition2)//
							.setPartition(key3, partition3)//
							.setPartition(key4, partition4)//
							.build();
		
		assertEquals(partition1,partitionsPluginData.getPartition(key1));
		assertEquals(partition2,partitionsPluginData.getPartition(key2));
		assertEquals(partition3,partitionsPluginData.getPartition(key3));
		assertEquals(partition4,partitionsPluginData.getPartition(key4));
		
	
		
		//precondition test: if the key is null
		ContractException contractException = assertThrows(ContractException.class,()->partitionsPluginData.getPartition(null));
		assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());
		
		//precondition test: if the key is unknown
		Object key5 = "key5";
		contractException = assertThrows(ContractException.class,()->partitionsPluginData.getPartition(key5));
		assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());
		

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getPartitionKeys", args = {})
	public void testGetPartitionKeys() {
		fail();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		fail();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		fail();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		fail();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		fail();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "setPartition", args = { Object.class, Partition.class })
	public void testSetPartition() {
		fail();
	}

}
