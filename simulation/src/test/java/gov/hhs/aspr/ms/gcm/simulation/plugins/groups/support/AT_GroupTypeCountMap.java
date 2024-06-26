package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
		// 4832988525233013426L
		/*
		 * Show various cases demonstrating that build order and implied zero
		 * values do not influence the equals contract
		 */

		// order should not matter
		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		GroupTypeCountMap groupTypeCountMap1 = builder.build();

		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		GroupTypeCountMap groupTypeCountMap2 = builder.build();

		assertEquals(groupTypeCountMap1, groupTypeCountMap2);

		// implied zero values should not matter
		builder.setCount(TestGroupTypeId.GROUP_TYPE_3, 0);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		groupTypeCountMap1 = builder.build();

		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		groupTypeCountMap2 = builder.build();

		assertEquals(groupTypeCountMap1, groupTypeCountMap2);

		// differences in positive counts matter

		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		groupTypeCountMap1 = builder.build();

		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 3);
		groupTypeCountMap2 = builder.build();

		assertNotEquals(groupTypeCountMap1, groupTypeCountMap2);
	}

	/**
	 * Tests {@linkplain GroupTypeCountMap#hashCode()
	 */
	@Test
	@UnitTestMethod(target = GroupTypeCountMap.class, name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * Equal objects have equal hash codes
		 */

		// order should not matter
		GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		GroupTypeCountMap groupTypeCountMap1 = builder.build();

		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		GroupTypeCountMap groupTypeCountMap2 = builder.build();

		assertEquals(groupTypeCountMap1.hashCode(), groupTypeCountMap2.hashCode());

		// implied zero values should not matter
		builder.setCount(TestGroupTypeId.GROUP_TYPE_3, 0);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		groupTypeCountMap1 = builder.build();

		builder.setCount(TestGroupTypeId.GROUP_TYPE_2, 7);
		builder.setCount(TestGroupTypeId.GROUP_TYPE_1, 5);
		groupTypeCountMap2 = builder.build();

		assertEquals(groupTypeCountMap1.hashCode(), groupTypeCountMap2.hashCode());

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
}
