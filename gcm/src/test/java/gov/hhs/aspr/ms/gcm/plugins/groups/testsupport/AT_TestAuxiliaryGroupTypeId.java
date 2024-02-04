package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_TestAuxiliaryGroupTypeId {

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupTypeId.class, name = "size", args = {})
	public void testSize() {
		assertEquals(TestAuxiliaryGroupTypeId.values().length, TestAuxiliaryGroupTypeId.size());
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupTypeId.class, name = "next", args = {})
	public void testNext() {
		for (TestAuxiliaryGroupTypeId testGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
			int index = (testGroupTypeId.ordinal() + 1) % TestAuxiliaryGroupTypeId.values().length;
			TestAuxiliaryGroupTypeId expectedNext = TestAuxiliaryGroupTypeId.values()[index];
			assertEquals(expectedNext, testGroupTypeId.next());
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupTypeId.class, name = "getRandomGroupTypeId", args = { RandomGenerator.class })
	public void testGetRandomGroupTypeId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8706009409831897125L);

		/*
		 * Show that randomly generated type ids are non-null and reasonably
		 * distributed
		 */

		Map<TestAuxiliaryGroupTypeId, MutableInteger> counterMap = new LinkedHashMap<>();
		for (TestAuxiliaryGroupTypeId testGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
			counterMap.put(testGroupTypeId, new MutableInteger());
		}
		int sampleSize = 1000;
		for (int i = 0; i < sampleSize; i++) {
			TestAuxiliaryGroupTypeId testGroupTypeId = TestAuxiliaryGroupTypeId.getRandomGroupTypeId(randomGenerator);
			assertNotNull(testGroupTypeId);
			counterMap.get(testGroupTypeId).increment();
		}

		int expectedSize = sampleSize / TestAuxiliaryGroupTypeId.values().length;
		int lowExpectedSize = 4 * expectedSize / 5;
		int highExpectedSize = 6 * expectedSize / 5;
		for (TestAuxiliaryGroupTypeId testGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
			MutableInteger mutableInteger = counterMap.get(testGroupTypeId);
			assertTrue(mutableInteger.getValue() > lowExpectedSize);
			assertTrue(mutableInteger.getValue() < highExpectedSize);
		}
	}

	@Test
	@UnitTestMethod(target = TestAuxiliaryGroupTypeId.class, name = "getUnknownGroupTypeId", args = {})
	public void testGetUnknownGroupTypeId() {
		/*
		 * Shows that a generated unknown group type id is unique, not null and
		 * not a member of the enum
		 */
		Set<TestAuxiliaryGroupTypeId> testGroupTypeIds = EnumSet.allOf(TestAuxiliaryGroupTypeId.class);
		Set<GroupTypeId> unknownGroupTypeIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			GroupTypeId unknownGroupTypeId = TestAuxiliaryGroupTypeId.getUnknownGroupTypeId();
			assertNotNull(unknownGroupTypeId);
			boolean unique = unknownGroupTypeIds.add(unknownGroupTypeId);
			assertTrue(unique);
			assertFalse(testGroupTypeIds.contains(unknownGroupTypeId));
		}
	}

}
