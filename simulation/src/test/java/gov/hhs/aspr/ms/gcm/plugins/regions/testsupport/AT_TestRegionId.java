package gov.hhs.aspr.ms.gcm.plugins.regions.testsupport;

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

import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_TestRegionId {

	/**
	 * Shows that generated random test regions are members of the TestRegionId
	 * enum and are reasonably random
	 */
	@Test
	@UnitTestMethod(target = TestRegionId.class, name = "getRandomRegionId", args = { RandomGenerator.class })
	public void testGetRandomRegionId() {

		Map<TestRegionId, MutableInteger> countMap = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			countMap.put(testRegionId, new MutableInteger());
		}
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8925394721065931243L);
		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			countMap.get(randomRegionId).increment();
			assertNotNull(randomRegionId);
		}

		int minCount = sampleCount / TestRegionId.size();
		minCount *= 4;
		minCount /= 5;
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertTrue(countMap.get(testRegionId).getValue() > minCount);
		}
	}

	/**
	 * Shows that a generated unknown region is not null and not a member of the
	 * enum
	 */
	@Test
	@UnitTestMethod(target = TestRegionId.class, name = "getUnknownRegionId", args = {})
	public void testGetUnknownRegionId() {
		Set<RegionId> unknownRegionIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
			assertNotNull(unknownRegionId);
			boolean unique = unknownRegionIds.add(unknownRegionId);
			assertTrue(unique);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertNotEquals(testRegionId, unknownRegionId);
			}
		}
	}

	/**
	 * Shows that size() returns the number of members in the TestRegionId enum
	 */
	@Test
	@UnitTestMethod(target = TestRegionId.class, name = "size", args = {})
	public void testSize() {
		assertEquals(TestRegionId.values().length, TestRegionId.size());
	}

	/**
	 * Shows the next value of each member matches expectations
	 */
	@Test
	@UnitTestMethod(target = TestRegionId.class, name = "next", args = {})
	public void test() {
		assertEquals(6, TestRegionId.values().length);

		assertEquals(TestRegionId.REGION_2, TestRegionId.REGION_1.next());
		assertEquals(TestRegionId.REGION_3, TestRegionId.REGION_2.next());
		assertEquals(TestRegionId.REGION_4, TestRegionId.REGION_3.next());
		assertEquals(TestRegionId.REGION_5, TestRegionId.REGION_4.next());
		assertEquals(TestRegionId.REGION_6, TestRegionId.REGION_5.next());
		assertEquals(TestRegionId.REGION_1, TestRegionId.REGION_6.next());

	}

}
