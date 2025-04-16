package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

/**
 * Test class for {@link GroupTypeCountMap}
 * 
 *
 */
public class AT_GroupTypeCountMap {

	/**
	 * Tests {@linkplain GroupTypeCountMap#equals(Object)
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980720418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupTypeCountMap groupTypeCountMap = getRandomGroupTypeCountMap(randomGenerator.nextLong());
			assertFalse(groupTypeCountMap.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupTypeCountMap groupTypeCountMap = getRandomGroupTypeCountMap(randomGenerator.nextLong());
			assertFalse(groupTypeCountMap.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupTypeCountMap groupTypeCountMap = getRandomGroupTypeCountMap(randomGenerator.nextLong());
			assertTrue(groupTypeCountMap.equals(groupTypeCountMap));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupTypeCountMap groupTypeCountMap1 = getRandomGroupTypeCountMap(seed);
			GroupTypeCountMap groupTypeCountMap2 = getRandomGroupTypeCountMap(seed);
			assertFalse(groupTypeCountMap1 == groupTypeCountMap2);
			for (int j = 0; j < 10; j++) {
				assertTrue(groupTypeCountMap1.equals(groupTypeCountMap2));
				assertTrue(groupTypeCountMap2.equals(groupTypeCountMap1));
			}
		}

		// different inputs yield unequal groupTypeCountMaps
		Set<GroupTypeCountMap> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupTypeCountMap groupTypeCountMap = getRandomGroupTypeCountMap(randomGenerator.nextLong());
			set.add(groupTypeCountMap);
		}
		assertEquals(100, set.size());
	}

	/**
	 * Tests {@linkplain GroupTypeCountMap#hashCode()
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653435608465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupTypeCountMap groupTypeCountMap1 = getRandomGroupTypeCountMap(seed);
			GroupTypeCountMap groupTypeCountMap2 = getRandomGroupTypeCountMap(seed);

			assertEquals(groupTypeCountMap1, groupTypeCountMap2);
			assertEquals(groupTypeCountMap1.hashCode(), groupTypeCountMap2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupTypeCountMap groupTypeCountMap = getRandomGroupTypeCountMap(randomGenerator.nextLong());
			hashCodes.add(groupTypeCountMap.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	/**
	 * Tests {@linkplain GroupTypeCountMap#getGroupCount(GroupTypeId)
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "getGroupCount", args = { GroupTypeId.class })
	public void testGetGroupCount() {
		// covered by testBuilder() test method
	}

	/**
	 * Tests {@linkplain GroupTypeCountMap#toString()
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "toString", args = {})
	public void testToString() {
		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

		int count = 1;
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.setCount(testGroupTypeId, count++);
		}
		GroupTypeCountMap groupTypeCountMap = builder.build();

		String expectedValue = "GroupTypeCountMap [GROUP_TYPE_1=1, GROUP_TYPE_2=2, GROUP_TYPE_3=3]";
		String actualValue = groupTypeCountMap.toString();

		assertEquals(expectedValue, actualValue);
	}

	/**
	 * Tests {@linkplain GroupTypeCountMap#builder()
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "builder", args = {})
	public void testBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1353590720789078598L);

		for (int i = 0; i < 20; i++) {
			Map<TestGroupTypeId, Integer> expectedValues = new LinkedHashMap<>();
			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				expectedValues.put(testGroupTypeId, 0);
				if (randomGenerator.nextBoolean()) {
					int count = randomGenerator.nextInt(3);
					builder.setCount(testGroupTypeId, count);
					expectedValues.put(testGroupTypeId, count);
				}
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int expectedValue = expectedValues.get(testGroupTypeId);
				int actualValue = groupTypeCountMap.getGroupCount(testGroupTypeId);
				assertEquals(expectedValue, actualValue);
			}
		}

		// precondition checks
		ContractException contractException = assertThrows(ContractException.class, () -> GroupTypeCountMap.builder().setCount(null, 10));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> GroupTypeCountMap.builder().setCount(TestGroupTypeId.GROUP_TYPE_1, -1));
		assertEquals(GroupError.NEGATIVE_GROUP_COUNT, contractException.getErrorType());

	}

	// public java.util.Set gcm.simulation.GroupTypeCountMap.getGroupTypeIds()
	/**
	 * Tests {@linkplain GroupTypeCountMap#getGroupTypeIds()
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1310699113269703296L);

		for (int i = 0; i < 20; i++) {
			Set<GroupTypeId> expectedGroupTypeIds = new LinkedHashSet<>();
			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				if (randomGenerator.nextBoolean()) {
					expectedGroupTypeIds.add(testGroupTypeId);
					builder.setCount(testGroupTypeId, 1);
				}
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			Set<GroupTypeId> actualGroupTypeIds = groupTypeCountMap.getGroupTypeIds();
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);
		}

	}

	@Test
	@UnitTestMethod(target = GroupTypeCountMap.Builder.class, name = "build", args = {})
	public void testBuild() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1446391997583651047L);

		for (int i = 0; i < 20; i++) {
			Map<TestGroupTypeId, Integer> expectedValues = new LinkedHashMap<>();
			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				expectedValues.put(testGroupTypeId, 0);
				if (randomGenerator.nextBoolean()) {
					int count = randomGenerator.nextInt(3);
					builder.setCount(testGroupTypeId, count);
					expectedValues.put(testGroupTypeId, count);
				}
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			assertNotNull(groupTypeCountMap);

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int expectedValue = expectedValues.get(testGroupTypeId);
				int actualValue = groupTypeCountMap.getGroupCount(testGroupTypeId);
				assertEquals(expectedValue, actualValue);
			}
		}

	}

	@Test
	@UnitTestMethod(target = GroupTypeCountMap.Builder.class, name = "setCount", args = { GroupTypeId.class, int.class })
	public void testSetCount() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1446391997583651047L);

		for (int i = 0; i < 20; i++) {
			Map<TestGroupTypeId, Integer> expectedValues = new LinkedHashMap<>();
			GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				expectedValues.put(testGroupTypeId, 0);
				if (randomGenerator.nextBoolean()) {
					int count = randomGenerator.nextInt(3);
					builder.setCount(testGroupTypeId, count);
					expectedValues.put(testGroupTypeId, count);
				}
			}
			GroupTypeCountMap groupTypeCountMap = builder.build();

			assertNotNull(groupTypeCountMap);

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int expectedValue = expectedValues.get(testGroupTypeId);
				int actualValue = groupTypeCountMap.getGroupCount(testGroupTypeId);
				assertEquals(expectedValue, actualValue);
			}
		}

	}

	private GroupTypeCountMap getRandomGroupTypeCountMap(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();

		List<TestGroupTypeId> testGroupTypeIds = TestGroupTypeId.getShuffledTestGroupTypeIds(randomGenerator);

		int n = randomGenerator.nextInt(testGroupTypeIds.size()) + 1;
		for (int i = 0; i < n; i++) {
			TestGroupTypeId testGroupTypeId = testGroupTypeIds.get(i);
			int randomGroupCount = randomGenerator.nextInt(Integer.MAX_VALUE);
			builder.setCount(testGroupTypeId, randomGroupCount);
		}

		return builder.build();
	}
}
