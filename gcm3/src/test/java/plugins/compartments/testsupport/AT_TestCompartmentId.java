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
	 * Shows that size() returns the number of members in the TestCompartmentId
	 * enum
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestCompartmentId.values().length, TestCompartmentId.size());
	}

	

	

	

	/**
	 * Shows the next value of each member matches expectations
	 */
	@Test
	@UnitTestMethod(name = "next", args = {})
	public void testNext() {
		assertEquals(5, TestCompartmentId.values().length);
		
		assertEquals(TestCompartmentId.COMPARTMENT_2, TestCompartmentId.COMPARTMENT_1.next());
		assertEquals(TestCompartmentId.COMPARTMENT_3, TestCompartmentId.COMPARTMENT_2.next());
		assertEquals(TestCompartmentId.COMPARTMENT_4, TestCompartmentId.COMPARTMENT_3.next());
		assertEquals(TestCompartmentId.COMPARTMENT_5, TestCompartmentId.COMPARTMENT_4.next());
		assertEquals(TestCompartmentId.COMPARTMENT_1, TestCompartmentId.COMPARTMENT_5.next());
		
	}

}
