package util.dimensiontree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.vector.Vector2D;

/**
 * Test class for {@link VolumetricDimensionTree}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = VolumetricDimensionTree.class)

public class AT_VolumetricDimensionTree {
	

	private static class Record {
		private final Vector2D position;
		private final double radius;
		private final int id;

		public Record(int id, Vector2D position, double radius) {
			this.position = position;
			this.radius = radius;
			this.id = id;
			if (radius < 0) {
				throw new RuntimeException("negative radius");
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Record)) {
				return false;
			}
			Record other = (Record) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#getMembersInSphere(double, double[])
	 */
	@Test
	@UnitTestMethod(name = "getMembersInSphere", args = { double.class, double[].class })
	public void testGetMembersInSphere() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6999798233863944694L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position.toArray(), record.radius, record);
		}

		double searchRadius = 10;

		for (int i = 0; i < n; i++) {
			Vector2D searchPosition = new Vector2D(randomGenerator.nextDouble() * 120 - 10, randomGenerator.nextDouble() * 120 - 10);

			Set<Record> expectedRecords = new LinkedHashSet<>();

			for (Record record : records) {
				double distance = record.position.distanceTo(searchPosition);
				if (distance <= searchRadius + record.radius) {
					expectedRecords.add(record);
				}
			}

			Set<Record> actualRecords = tree.getMembersInSphere(searchRadius, searchPosition.toArray()).stream().collect(Collectors.toCollection(LinkedHashSet::new));

			assertEquals(expectedRecords, actualRecords);
		}
	}

	/**
	 * Tests {@link VolumetricDimensionTree#getAll()
	 */
	@Test
	@UnitTestMethod(name = "getAll", args = {})
	public void testGetAll() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7850509241104624831L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position.toArray(), record.radius, record);
		}

		Set<Record> expected = records.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Record> actual = tree.getAll().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link VolumetricDimensionTree#add(double[], double, Object)
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double[].class, double.class, Object.class })
	public void testAdd() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8144068492710937714L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
			tree.add(record.position.toArray(), record.radius, record);
		}

		Set<Record> expectedRecords = records.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Record> actualRecords = tree.getAll().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expectedRecords, actualRecords);

		/*
		 * We add the records again, this should result in each record being in
		 * two places in the tree and the tree returning twice as many records.
		 * Note that two records are equal if their id values match.
		 */
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			tree.add(record.position.toArray(), record.radius, record);
		}

		assertEquals(records.size() * 2, tree.getAll().size());

		expectedRecords = records.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		actualRecords = tree.getAll().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expectedRecords, actualRecords);

	}

	/**
	 * Tests {@link VolumetricDimensionTree#contains(Object)
	 */
	@Test
	@UnitTestMethod(name = "contains", args = { Object.class })
	public void testContains() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7101516806363352895L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			if (i % 2 == 0) {
				tree.add(record.position.toArray(), record.radius, record);
			}
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			boolean contained = tree.contains(record);
			assertEquals(i % 2 == 0, contained);
		}
	}

	/**
	 * Tests {@link VolumetricDimensionTree#remove(Object)
	 * 
	 */
	@Test
	@UnitTestMethod(name = "remove", args = { Object.class })
	public void testRemove_Object() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5635646758825222227L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			if (i % 2 == 0) {
				tree.add(record.position.toArray(), record.radius, record);
			}
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			boolean removed = tree.remove(record);
			assertEquals(i % 2 == 0, removed);
		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#remove(double, Object)
	 */
	@Test
	@UnitTestMethod(name = "remove", args = { double.class, Object.class })
	public void testRemove() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4986556383522453940L);

		VolumetricDimensionTree<Record> tree = //
				VolumetricDimensionTree	.builder()//
										.setLowerBounds(new double[] { 0, 0 })//
										.setUpperBounds(new double[] { 100, 100 })//
										.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Vector2D position = new Vector2D(randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			double radius = randomGenerator.nextDouble() * 10;
			Record record = new Record(i, position, radius);
			records.add(record);
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			if (i % 2 == 0) {
				tree.add(record.position.toArray(), record.radius, record);
			}
		}

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			boolean removed = tree.remove(record.radius, record);
			assertEquals(i % 2 == 0, removed);
		}

	}

	/**
	 * Tests {@link VolumetricDimensionTree#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		/*
		 * Precondition tests
		 */

		// first show that the following arguments to the builder form a tree.
		VolumetricDimensionTree<Object> tree = VolumetricDimensionTree	.builder()//
																		.setLowerBounds(new double[] { 0, 0 })//
																		.setUpperBounds(new double[] { 100, 100 })//
																		.setFastRemovals(true)//
																		.setLeafSize(50)//
																		.build(); //
		assertNotNull(tree);

		// if the selected leaf size is not positive
		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 0 })//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(-50)//
									.build(); //
		});

		// if the lower bounds were not contributed or were null
		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									// .setLowerBounds(new double[] { 0, 0 })//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(null)//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		// if the upper bounds were not contributed or were null
		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 0 })//
									// .setUpperBounds(new double[] { 100, 100
									// })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 0 })//
									.setUpperBounds(null)//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		// if the lower and upper bounds do not match in length
		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 0 })//
									.setUpperBounds(new double[] { 100, 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 0, 0 })//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		// if any of the lower bounds exceed the corresponding
		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 101, 0 })//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			VolumetricDimensionTree	.builder()//
									.setLowerBounds(new double[] { 0, 101 })//
									.setUpperBounds(new double[] { 100, 100 })//
									.setFastRemovals(true)//
									.setLeafSize(50)//
									.build(); //
		});

	}

}
