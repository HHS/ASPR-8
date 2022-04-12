package util.dimensiontree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.time.TimeElapser;

/**
 * Test class for {@link DimensionTree}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = DimensionTree.class)

public class AT_DimensionTree {
	
	private static class Record {

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Record [id=");
			builder.append(id);
			builder.append(", position=");
			builder.append(Arrays.toString(position));
			builder.append("]");
			return builder.toString();
		}

		private final int id;
		private final double[] position = new double[2];

		public Record(int id, double x, double y) {
			this.id = id;
			position[0] = x;
			position[1] = y;
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
	 * Tests {@link DimensionTree#getMembersInSphere(double, double[])
	 */
	@Test
	@UnitTestMethod(name = "getMembersInSphere", args = { double.class, double[].class })
	public void testGetMembersInSphere() {


		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4720556754557132042L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position, record);
		}

		double searchRadius = 10;

		for (int i = 0; i < n; i++) {
			double[] position = new double[2];
			position[0] = randomGenerator.nextDouble() * 120 - 10;
			position[1] = randomGenerator.nextDouble() * 120 - 10;

			List<Record> list = new ArrayList<>();

			for (Record record : records) {
				double deltaX = record.position[0] - position[0];
				double deltaY = record.position[1] - position[1];
				double distance = FastMath.sqrt(deltaX * deltaX + deltaY * deltaY);
				if (distance < searchRadius) {
					list.add(record);
				}
			}

			Set<Record> expectedRecords = new LinkedHashSet<>(list);

			list = tree.getMembersInSphere(searchRadius, position);

			Set<Record> actualRecords = new LinkedHashSet<>(list);

			assertEquals(expectedRecords, actualRecords);
		}

	}

	/**
	 * Tests {@link DimensionTree#getNearestMember(double[])
	 */
	@Test
	@UnitTestMethod(name = "getNearestMember", args = { double[].class })
	public void testGetNearestMember() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1704585910834495981L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { -10, -10 })//
								.setUpperBounds(new double[] { -1, -1 })//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position, record);
		}

		for (int i = 0; i < n; i++) {
			double[] position = new double[2];
			position[0] = randomGenerator.nextDouble() * 120 - 10;
			position[1] = randomGenerator.nextDouble() * 120 - 10;

			Record expectedClosestRecord = null;
			double bestDistance = Double.POSITIVE_INFINITY;
			for (Record record : records) {
				double deltaX = record.position[0] - position[0];
				double deltaY = record.position[1] - position[1];
				double distance = FastMath.sqrt(deltaX * deltaX + deltaY * deltaY);
				if (expectedClosestRecord == null || distance < bestDistance) {
					expectedClosestRecord = record;
					bestDistance = distance;
				}
			}

			Optional<Record> actualClosestRecord = tree.getNearestMember(position);
			assertTrue(actualClosestRecord.isPresent());
			assertEquals(expectedClosestRecord, actualClosestRecord.get());

		}

	}

	/**
	 * Tests {@link DimensionTree#getMembersInRectanguloid(double[], double[])
	 */
	@Test
	@UnitTestMethod(name = "getMembersInRectanguloid", args = { double[].class, double[].class })
	public void testGetMembersInRectanguloid() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7067981743992714824L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 1, 1 })//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 1000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
		}

		for (Record record : records) {
			tree.add(record.position, record);
		}

		// StopWatch bruteForceStopWatch = new StopWatch();
		// StopWatch treeStopWatch = new StopWatch();
		for (int i = 0; i < n; i++) {
			double[] upperBounds = new double[2];
			double[] lowerBounds = new double[2];
			lowerBounds[0] = randomGenerator.nextDouble() * 120 - 10;
			lowerBounds[1] = randomGenerator.nextDouble() * 120 - 10;
			upperBounds[0] = lowerBounds[0] + randomGenerator.nextDouble() * 10 + 1;
			upperBounds[1] = lowerBounds[1] + randomGenerator.nextDouble() * 10 + 1;

			Set<Record> expectedRecords = new LinkedHashSet<>();
			// bruteForceStopWatch.start();
			for (Record record : records) {
				boolean reject = false;
				for (int j = 0; j < 2; j++) {
					reject |= record.position[j] > upperBounds[j];
					reject |= record.position[j] < lowerBounds[j];
				}
				if (!reject) {
					expectedRecords.add(record);
				}
			}
			// bruteForceStopWatch.stop();

			// treeStopWatch.start();
			Set<Record> actualRecords = tree.getMembersInRectanguloid(lowerBounds, upperBounds).stream().collect(Collectors.toCollection(LinkedHashSet::new));
			// treeStopWatch.stop();

			assertEquals(expectedRecords, actualRecords);

		}
		// System.out.println("brute force " +
		// bruteForceStopWatch.getElapsedMilliSeconds());
		// System.out.println("tree " + treeStopWatch.getElapsedMilliSeconds());

	}

	/**
	 * Tests {@link DimensionTree#getAll()
	 */
	@Test
	@UnitTestMethod(name = "getAll", args = {})
	public void testGetAll() {
		/*
		 * See test for add()
		 */
	}

	/**
	 * Tests {@link DimensionTree#add(double[], Object)
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double[].class, Object.class })
	public void testAdd() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7699215262915366096L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 1, 1 })//
								.setLeafSize(15).build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
			tree.add(record.position, record);
		}

		Set<Record> expectedRecords = records.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		Set<Record> actualRecords = tree.getAll().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expectedRecords, actualRecords);

		// We add the records again, this should result in each record being in
		// two places in the tree and the tree returning twice as many records.
		// Note that two records are equal if their id values match.
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			tree.add(record.position, record);
		}

		assertEquals(records.size() * 2, tree.getAll().size());

		expectedRecords = records.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		actualRecords = tree.getAll().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expectedRecords, actualRecords);

	}

	/**
	 * Tests {@link DimensionTree#contains(Object)
	 */
	@Test
	@UnitTestMethod(name = "contains", args = { Object.class })
	public void testContains() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7408475895380756180L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 1, 1 })//
								.setLeafSize(15).build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
			if (i < n / 2) {
				tree.add(record.position, record);
			}
		}

		for (int i = 0; i < n; i++) {
			Record record = records.get(i);
			if (i < n / 2) {
				assertTrue(tree.contains(record));
			} else {
				assertFalse(tree.contains(record));
			}
		}

	}

	/**
	 * Tests {@link DimensionTree#remove(Object)
	 */
	@Test
	@UnitTestMethod(name = "remove", args = { Object.class })
	public void testRemove() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7030760224206012399L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.setFastRemovals(true)//
								.setLeafSize(50)//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 100;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
			if (i % 2 == 0) {
				tree.add(record.position, record);
				tree.add(record.position, record);
				tree.add(record.position, record);
				record.position[0] = randomGenerator.nextDouble() * 100;
				record.position[1] = randomGenerator.nextDouble() * 100;
				tree.add(record.position, record);
				tree.add(record.position, record);
			}
		}

		// System.out.println(tree);

		for (int i = 0; i < records.size(); i++) {
			Record record = records.get(i);
			boolean removed = tree.remove(record);
			assertEquals(i % 2 == 0, removed);
			// System.out.println(tree);
		}

	}

	/**
	 * Tests {@link DimensionTree#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		/*
		 * Precondition tests
		 */

		// first show that the following arguments to the builder form a tree.
		DimensionTree<Object> tree = DimensionTree	.builder()//
													.setLowerBounds(new double[] { 0, 0 })//
													.setUpperBounds(new double[] { 100, 100 })//
													.setFastRemovals(true)//
													.setLeafSize(50)//
													.build(); //
		assertNotNull(tree);

		// if the selected leaf size is not positive
		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 0 })//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(-50)//
							.build(); //
		});

		// if the lower bounds were not contributed or were null
		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							// .setLowerBounds(new double[] { 0, 0 })//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(null)//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		// if the upper bounds were not contributed or were null
		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 0 })//
							// .setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 0 })//
							.setUpperBounds(null)//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		// if the lower and upper bounds do not match in length
		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 0 })//
							.setUpperBounds(new double[] { 100, 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 0, 0 })//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		// if any of the lower bounds exceed the corresponding
		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 101, 0 })//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

		assertThrows(RuntimeException.class, () -> {
			DimensionTree	.builder()//
							.setLowerBounds(new double[] { 0, 101 })//
							.setUpperBounds(new double[] { 100, 100 })//
							.setFastRemovals(true)//
							.setLeafSize(50)//
							.build(); //
		});

	}

	// @Test
	public void testPerformance() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3684348766628697684L);

		DimensionTree<Record> tree = //
				DimensionTree	.builder()//
								.setLowerBounds(new double[] { 0, 0 })//
								.setUpperBounds(new double[] { 100, 100 })//
								.setFastRemovals(true)//
								.setLeafSize(15)//
								.build(); //

		List<Record> records = new ArrayList<>();

		int n = 600_000;
		for (int i = 0; i < n; i++) {
			Record record = new Record(i, randomGenerator.nextDouble() * 100, randomGenerator.nextDouble() * 100);
			records.add(record);
		}
		TimeElapser timeElapser = new TimeElapser();

		for (Record record : records) {
			tree.add(record.position, record);
		}
		System.out.println("Add time = " + timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();
		for (Record record : records) {
			tree.remove(record);
		}
		System.out.println("Remove time = " + timeElapser.getElapsedMilliSeconds());
	}

}
