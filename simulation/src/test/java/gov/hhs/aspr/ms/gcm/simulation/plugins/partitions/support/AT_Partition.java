package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.TrueFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.FunctionalAttributeLabeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_Partition {

	/**
	 * Tests {@linkplain Partition#builder()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "builder", args = {})
	public void testBuilder() {
		Partition partition = Partition.builder().build();
		assertNotNull(partition);
		assertFalse(partition.getFilter().isPresent());
		assertTrue(partition.getLabelers().isEmpty());
		assertTrue(partition.isDegenerate());
	}

	@Test
	@UnitTestMethod(target = Partition.class, name = "getLabelers", args = {})
	public void testGetLabelers() {

		Set<Labeler> expectedLabelers = new LinkedHashSet<>();
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object()));
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> new Object()));
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, (v) -> new Object()));

		Partition.Builder builder = Partition.builder();
		for (Labeler labeler : expectedLabelers) {
			builder.addLabeler(labeler);
		}

		Partition partition = builder.build();

		Set<Labeler> actualLabelers = partition.getLabelers();

		assertEquals(expectedLabelers, actualLabelers);

	}

	/**
	 * Tests {@linkplain Partition#getFilter()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "getFilter", args = {})
	public void testGetFilter() {

		Partition partition = Partition.builder().build();//
		assertFalse(partition.getFilter().isPresent());

		partition = Partition.builder().setFilter(new TrueFilter()).build();//
		assertTrue(partition.getFilter().isPresent());

	}

	/**
	 * Tests {@linkplain Partition#isDegenerate()
	 */
	@Test
	@UnitTestMethod(target = Partition.class, name = "isDegenerate", args = {})
	public void testIsDegenerate() {

		Partition partition = Partition.builder().build();//
		assertTrue(partition.isDegenerate());

		partition = Partition.builder()
				.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object())).build();
		assertFalse(partition.isDegenerate());
	}

	@Test
	@UnitTestMethod(target = Partition.class, name = "retainPersonKeys", args = {})
	public void testRetainPersonKeys() {
		Partition retainKeys = Partition.builder().setRetainPersonKeys(true).build();
		Partition dontRetainKeys = Partition.builder().setRetainPersonKeys(false).build();

		assertTrue(retainKeys.retainPersonKeys());
		assertFalse(dontRetainKeys.retainPersonKeys());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "build", args = {})
	public void testBuild() {
		Partition partition = Partition.builder().build();
		assertNotNull(partition);
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "setFilter", args = { Filter.class })
	public void testSetFilter() {
		Partition.Builder builder = Partition.builder();
		Filter filter = new TrueFilter();
		builder.setFilter(filter);

		Partition partition = builder.build();
		assertNotNull(partition);
		assertEquals(filter, partition.getFilter().get());
		assertTrue(!Partition.builder().build().getFilter().isPresent());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "setRetainPersonKeys", args = { boolean.class })
	public void testSetRetainPersonKeys() {
		Partition retainKeys = Partition.builder().setRetainPersonKeys(true).build();
		Partition dontRetainKeys = Partition.builder().setRetainPersonKeys(false).build();

		assertTrue(retainKeys.retainPersonKeys());
		assertFalse(dontRetainKeys.retainPersonKeys());
	}

	@Test
	@UnitTestMethod(target = Partition.Builder.class, name = "addLabeler", args = { Labeler.class })
	public void testAddlabeler() {
		Set<Labeler> expectedLabelers = new LinkedHashSet<>();
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> new Object()));
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> new Object()));
		expectedLabelers.add(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, (v) -> new Object()));

		Partition.Builder builder = Partition.builder();
		for (Labeler labeler : expectedLabelers) {
			builder.addLabeler(labeler);
		}

		Partition partition = builder.build();

		Set<Labeler> actualLabelers = partition.getLabelers();

		assertEquals(expectedLabelers, actualLabelers);
	}

	private static class LocalLabeler implements Labeler {

		private final int value;

		public LocalLabeler(int value) {
			this.value = value;
		}

		@Override
		public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
			return null;
		}

		@Override
		public Object getCurrentLabel(PartitionsContext partitionsContext, PersonId personId) {
			return null;
		}

		@Override
		public Object getPastLabel(PartitionsContext partitionsContext, Event event) {
			return null;
		}

		@Override
		public Object getId() {
			return value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LocalLabeler other = (LocalLabeler) obj;
			return value == other.value;
		}

	}

	private Partition getRandomPartition(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Partition.Builder builder = Partition.builder();
		builder.setRetainPersonKeys(randomGenerator.nextBoolean());
		Labeler labeler = new LocalLabeler(randomGenerator.nextInt());
		builder.setFilter(new AttributeFilter(TestAttributeId.getRandomAttributeId(randomGenerator),
				Equality.getRandomEquality(randomGenerator), randomGenerator.nextInt()));
		builder.addLabeler(labeler);
		return builder.build();
	}

	@Test
	@UnitTestMethod(target = Partition.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2832165952351188895L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			Partition partition = getRandomPartition(randomGenerator.nextLong());
			assertFalse(partition.equals(new Object()));
		}

		// never equal null
		for (int i = 0; i < 30; i++) {
			Partition partition = getRandomPartition(randomGenerator.nextLong());//
			assertFalse(partition.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			Partition partition = getRandomPartition(randomGenerator.nextLong());//
			assertTrue(partition.equals(partition));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			Partition partition1 = getRandomPartition(seed);//
			Partition partition2 = getRandomPartition(seed);//
			assertFalse(partition1 == partition2);
			for (int j = 0; j < 10; j++) {
				assertTrue(partition1.equals(partition2));
				assertTrue(partition2.equals(partition1));
			}
		}

		// different inputs yield non-equal objects
		Set<Partition> partitions = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {

			Partition partition = getRandomPartition(randomGenerator.nextLong());//
			partitions.add(partition);

		}
		assertEquals(100, partitions.size());

	}

	@Test
	@UnitTestMethod(target = Partition.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2170049186562286346L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			Partition partition1 = getRandomPartition(seed);//
			Partition partition2 = getRandomPartition(seed);//
			assertEquals(partition1, partition2);
			assertEquals(partition1.hashCode(), partition2.hashCode());
		}
		
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {

			Partition partition = getRandomPartition(randomGenerator.nextLong());//
			hashCodes.add(partition.hashCode());

		}
		assertEquals(100, hashCodes.size());
	}

 
	@Test
	@UnitTestMethod(target = Partition.class, name = "toString", args = {})
	public void testToString() {
		Partition randomPartition = getRandomPartition(5250756946904578664L);
		String actualValue = randomPartition.toString();
		
		String expectedValue =	"Partition [data=Data [filter=AttributeFilter [attributeId=BOOLEAN_1, value=2146794287, equality=LESS_THAN, attributesDataManager=null], labelers={1157575879=gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.AT_Partition$LocalLabeler@44ff34e6}, retainPersonKeys=false]]";
		assertEquals(expectedValue, actualValue);
	}
}
