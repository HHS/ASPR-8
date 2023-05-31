package plugins.partitions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.filters.FalseFilter;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.support.filters.TrueFilter;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PartitionsPluginData {

	private static class LocalAttributeLabeler extends AttributeLabeler {
		private final AttributeId attributeId;

		public LocalAttributeLabeler(AttributeId attributeId) {
			super(attributeId);
			this.attributeId = attributeId;
		}

		@Override
		protected Object getLabelFromValue(Object value) {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((attributeId == null) ? 0 : attributeId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalAttributeLabeler)) {
				return false;
			}
			LocalAttributeLabeler other = (LocalAttributeLabeler) obj;
			if (attributeId == null) {
				if (other.attributeId != null) {
					return false;
				}
			} else if (!attributeId.equals(other.attributeId)) {
				return false;
			}
			return true;
		}

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getPartition", args = { Object.class })
	public void testGetPartition() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6203336618738232358L);
		
		Object key1 = "key1";
		Partition partition1 = getRandomPartition(randomGenerator);

		Object key2 = "key2";
		Partition partition2 = getRandomPartition(randomGenerator);

		Object key3 = "key3";
		Partition partition3 = getRandomPartition(randomGenerator);

		Object key4 = "key4";
		Partition partition4 = getRandomPartition(randomGenerator);

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()//
																		.addPartition(key1, partition4)//
																		.addPartition(key1, partition1)//
																		.addPartition(key2, partition2)//
																		.addPartition(key3, partition3)//
																		.addPartition(key4, partition4)//
																		.build();

		assertEquals(partition1, partitionsPluginData.getPartition(key1));
		assertEquals(partition2, partitionsPluginData.getPartition(key2));
		assertEquals(partition3, partitionsPluginData.getPartition(key3));
		assertEquals(partition4, partitionsPluginData.getPartition(key4));

		// precondition test: if the key is null
		ContractException contractException = assertThrows(ContractException.class, () -> partitionsPluginData.getPartition(null));
		assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

		// precondition test: if the key is unknown
		Object key5 = "key5";
		contractException = assertThrows(ContractException.class, () -> partitionsPluginData.getPartition(key5));
		assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getPartitionKeys", args = {})
	public void testGetPartitionKeys() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1792263467430537703L);
		
		Object key1 = "key1";
		Partition partition1 = getRandomPartition(randomGenerator);

		Object key2 = "key2";
		Partition partition2 = getRandomPartition(randomGenerator);

		Object key3 = "key3";
		Partition partition3 = getRandomPartition(randomGenerator);

		Object key4 = "key4";
		Partition partition4 = getRandomPartition(randomGenerator);

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()//
																		.addPartition(key1, partition4)//
																		.addPartition(key1, partition1)//
																		.addPartition(key2, partition2)//
																		.addPartition(key3, partition3)//
																		.addPartition(key4, partition4)//
																		.build();

		Set<Object> expectedKeys = new LinkedHashSet<>();
		expectedKeys.add(key1);
		expectedKeys.add(key2);
		expectedKeys.add(key3);
		expectedKeys.add(key4);

		Set<Object> actualKeys = partitionsPluginData.getPartitionKeys();

		assertEquals(expectedKeys, actualKeys);
	}

	private Labeler getRandomLabeler(RandomGenerator randomGenerator) {
		return new LocalAttributeLabeler(TestAttributeId.getRandomAttributeId(randomGenerator));
	}

	private Filter getRandomFilter(RandomGenerator randomGenerator, int level) {

		if (level > 2 || randomGenerator.nextDouble() < 0.5) {
			int n = randomGenerator.nextInt(3);
			switch (n) {
			case 0:
				AttributeId attributeId = TestAttributeId.getRandomAttributeId(randomGenerator);
				Equality equality = Equality.getRandomEquality(randomGenerator);
				AttributeFilter attributeFilter = new AttributeFilter(attributeId, equality, randomGenerator.nextDouble());
				return attributeFilter;
			case 1:
				return new TrueFilter();
			case 2:
				return new FalseFilter();
			default:
				throw new RuntimeException("unhandled case " + n);
			}
		} else {
			int subLevel = level - 1;
			int n = randomGenerator.nextInt(3);
			switch (n) {
			case 0:
				return getRandomFilter(randomGenerator, subLevel).and(getRandomFilter(randomGenerator, subLevel));
			case 1:
				return getRandomFilter(randomGenerator, subLevel).or(getRandomFilter(randomGenerator, subLevel));
			case 2:
				return getRandomFilter(randomGenerator, subLevel).not();
			default:
				throw new RuntimeException("unhandled case " + n);
			}
		}
	}

	private Partition getRandomPartition(RandomGenerator randomGenerator) {
		Partition.Builder partitionBuilder = Partition.builder();
		partitionBuilder.setRetainPersonKeys(randomGenerator.nextBoolean());
		if (randomGenerator.nextDouble() < 0.8) {
			partitionBuilder.setFilter(getRandomFilter(randomGenerator, 0));
		}
		int n = randomGenerator.nextInt(3);
		for (int i = 0; i < n; i++) {
			partitionBuilder.addLabeler(getRandomLabeler(randomGenerator));
		}
		return partitionBuilder.build();
	}

	private PartitionsPluginData getRandomPartitionsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		PartitionsPluginData.Builder partitionsPluginDataBuilder = PartitionsPluginData.builder();
		int n = randomGenerator.nextInt(20);
		for (int i = 0; i < n; i++) {
			partitionsPluginDataBuilder.addPartition(randomGenerator.nextInt(), getRandomPartition(randomGenerator));
		}
		return partitionsPluginDataBuilder.build();
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(369266573804576742L);
		int n = 10;
		for (int i = 0; i < n; i++) {
			PartitionsPluginData expectedPluginData = getRandomPartitionsPluginData(randomGenerator.nextLong());
			PluginDataBuilder cloneBuilder = expectedPluginData.getCloneBuilder();
			assertNotNull(cloneBuilder);
			PluginData actualPluginData = cloneBuilder.build();
			assertEquals(expectedPluginData, actualPluginData);
		}
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3212481795053201695L);
		// equal objects have equal hash codes
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData1 = getRandomPartitionsPluginData(seed);
			PartitionsPluginData partitionsPluginData2 = getRandomPartitionsPluginData(seed);
			assertEquals(partitionsPluginData1, partitionsPluginData2);
			assertEquals(partitionsPluginData1.hashCode(), partitionsPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData = getRandomPartitionsPluginData(seed);
			hashCodes.add(partitionsPluginData.hashCode());
		}

		assertTrue(hashCodes.size() > 90);

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4949842187503776364L);
		// no object equals null
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData = getRandomPartitionsPluginData(seed);
			assertFalse(partitionsPluginData.equals(null));
		}
		// reflexivity
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData = getRandomPartitionsPluginData(seed);
			assertTrue(partitionsPluginData.equals(partitionsPluginData));
		}
		// symmetry
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData1 = getRandomPartitionsPluginData(seed);
			PartitionsPluginData partitionsPluginData2 = getRandomPartitionsPluginData(seed);
			assertTrue(partitionsPluginData1.equals(partitionsPluginData2));
			assertTrue(partitionsPluginData2.equals(partitionsPluginData1));
		}
		// transitivity -- effectively covered above

	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2213256764714274380L);
		// we use the getRandomPartitionsPluginData to invoke the build() method
		for (int i = 0; i < 10; i++) {
			long seed = randomGenerator.nextLong();
			PartitionsPluginData partitionsPluginData = getRandomPartitionsPluginData(seed);
			assertNotNull(partitionsPluginData);
		}

		// there are no precondition tests
	}

	@Test
	@UnitTestMethod(target = PartitionsPluginData.Builder.class, name = "setPartition", args = { Object.class, Partition.class })
	public void testSetPartition() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(252451097846467840L);
		
		Object key1 = "key1";
		Partition partition1 = getRandomPartition(randomGenerator);

		Object key2 = "key2";
		Partition partition2 = getRandomPartition(randomGenerator);

		Object key3 = "key3";
		Partition partition3 = getRandomPartition(randomGenerator);

		Object key4 = "key4";
		Partition partition4 = getRandomPartition(randomGenerator);

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()//
																		.addPartition(key1, partition4)//
																		.addPartition(key1, partition1)//
																		.addPartition(key2, partition2)//
																		.addPartition(key3, partition3)//
																		.addPartition(key4, partition4)//
																		.build();

		assertEquals(partition1, partitionsPluginData.getPartition(key1));
		assertEquals(partition2, partitionsPluginData.getPartition(key2));
		assertEquals(partition3, partitionsPluginData.getPartition(key3));
		assertEquals(partition4, partitionsPluginData.getPartition(key4));

		// precondition test: if the key is null
		ContractException contractException = assertThrows(ContractException.class, () -> PartitionsPluginData.builder().addPartition(null,getRandomPartition(randomGenerator)));
		assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

		// precondition test: if the partition null
		Object key5 = "key5";
		contractException = assertThrows(ContractException.class, () -> PartitionsPluginData.builder().addPartition(key5,null));
		assertEquals(PartitionError.NULL_PARTITION, contractException.getErrorType());

		
	}

}
