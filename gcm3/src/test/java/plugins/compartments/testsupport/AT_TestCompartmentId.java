package plugins.compartments.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestCompartmentId.class)
public class AT_TestCompartmentId {

	/**
	 * Shows that generated random test compartments are members of the
	 * TestCompartmentId enum and are reasonably random
	 */
	@Test
	@UnitTestMethod(name = "getRandomCompartmentId", args = { RandomGenerator.class })
	public void testGetRandomCompartmentId() {

		Map<TestCompartmentId, MutableInteger> countMap = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			countMap.put(testCompartmentId, new MutableInteger());
		}
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8925394721065931243L);
		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			TestCompartmentId randomCompartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);
			countMap.get(randomCompartmentId).increment();
			assertNotNull(randomCompartmentId);
		}

		int minCount = sampleCount / TestCompartmentId.size();
		minCount *= 4;
		minCount /= 5;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertTrue(countMap.get(testCompartmentId).getValue() > minCount);
		}
	}

	/**
	 * Shows that a generated unknown compartment is not null and not a member
	 * of the enum
	 */
	@Test
	@UnitTestMethod(name = "getUnknownCompartmentId", args = {})
	public void testGetUnknownCompartmentId() {
		Set<CompartmentId> compartmentIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
			assertNotNull(unknownCompartmentId);
			boolean unique = compartmentIds.add(unknownCompartmentId);
			assertTrue(unique);
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertNotEquals(testCompartmentId, unknownCompartmentId);
			}
		}
	}

	/**
	 * Shows that a generated unknown compartment property id is not null and
	 * not a property id of any member of the enum
	 */
	@Test
	@UnitTestMethod(name = "getUnknownCompartmentPropertyId", args = {})
	public void testGetUnknownCompartmentPropertyId() {
		Set<CompartmentPropertyId> unknownCompartmentPropertyIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentId.getUnknownCompartmentPropertyId();
			assertNotNull(unknownCompartmentPropertyId);
			boolean unique = unknownCompartmentPropertyIds.add(unknownCompartmentPropertyId);
			assertTrue(unique);
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (CompartmentPropertyId compartmentPropertyId : testCompartmentId.getCompartmentPropertyIds()) {
					assertNotEquals(compartmentPropertyId, unknownCompartmentPropertyId);
				}
			}
		}
	}

	/**
	 * Shows that size() returns the number of members in the TestCompartmentId
	 * enum
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestCompartmentId.values().length, TestCompartmentId.size());
	}

	/**
	 * Shows that the property count for each member is equal to the number of
	 * properties contained for that member.
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPropertyCount", args = {})
	public void testGetCompartmentPropertyCount() {
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertEquals(testCompartmentId.getCompartmentPropertyIds().length, testCompartmentId.getCompartmentPropertyCount());
		}
	}

	/**
	 * Shows that the compartment property id returned is equal to the
	 * corresponding property id in the array version.
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPropertyId", args = { int.class })
	public void testGetCompartmentPropertyId() {

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (int i = 0; i < testCompartmentId.getCompartmentPropertyCount(); i++) {
				assertEquals(compartmentPropertyIds[i], testCompartmentId.getCompartmentPropertyId(i));
			}
		}

	}

	/**
	 * Shows that each compartment property id is unique and that the number of
	 * such ids matches the construction arguments of the enum
	 */
	@Test
	@UnitTestMethod(name = "getCompartmentPropertyIds", args = {})
	public void testGetCompartmentPropertyIds() {
		Set<CompartmentPropertyId> allCompartmentPropertyIds = new LinkedHashSet<>();
		Map<TestCompartmentId, Integer> expectedCounts = new LinkedHashMap<>();
		expectedCounts.put(TestCompartmentId.COMPARTMENT_1, 3);
		expectedCounts.put(TestCompartmentId.COMPARTMENT_2, 2);
		expectedCounts.put(TestCompartmentId.COMPARTMENT_3, 4);
		expectedCounts.put(TestCompartmentId.COMPARTMENT_4, 1);
		expectedCounts.put(TestCompartmentId.COMPARTMENT_5, 3);
		
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			int count = testCompartmentId.getCompartmentPropertyCount();
			assertEquals(expectedCounts.get(testCompartmentId).intValue(), count);
			for(int i = 0; i<count;i++) {
				boolean unique = allCompartmentPropertyIds.add(testCompartmentId.getCompartmentPropertyId(i));
				assertTrue(unique);
			}
		}

	}

	/**
	 * Shows the next value of each member matches expectations
	 */
	@Test
	@UnitTestMethod(name = "next", args = {})
	public void test() {
		assertEquals(5, TestCompartmentId.values().length);
		
		assertEquals(TestCompartmentId.COMPARTMENT_2, TestCompartmentId.COMPARTMENT_1.next());
		assertEquals(TestCompartmentId.COMPARTMENT_3, TestCompartmentId.COMPARTMENT_2.next());
		assertEquals(TestCompartmentId.COMPARTMENT_4, TestCompartmentId.COMPARTMENT_3.next());
		assertEquals(TestCompartmentId.COMPARTMENT_5, TestCompartmentId.COMPARTMENT_4.next());
		assertEquals(TestCompartmentId.COMPARTMENT_1, TestCompartmentId.COMPARTMENT_5.next());
		
	}

}
