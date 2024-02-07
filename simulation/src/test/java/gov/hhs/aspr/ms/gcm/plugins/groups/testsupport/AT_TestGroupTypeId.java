package gov.hhs.aspr.ms.gcm.plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_TestGroupTypeId {

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "getRandomGroupTypeId", args = { RandomGenerator.class })
    public void testGetRandomGroupTypeId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2320032453802629402L);

        /*
         * Show that randomly generated type ids are non-null and reasonably distributed
         */

        Map<TestGroupTypeId, MutableInteger> counterMap = new LinkedHashMap<>();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            counterMap.put(testGroupTypeId, new MutableInteger());
        }
        int sampleSize = 1000;
        for (int i = 0; i < sampleSize; i++) {
            TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            assertNotNull(testGroupTypeId);
            counterMap.get(testGroupTypeId).increment();
        }

        int expectedSize = sampleSize / TestGroupTypeId.values().length;
        int lowExpectedSize = 4 * expectedSize / 5;
        int highExpectedSize = 6 * expectedSize / 5;
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            MutableInteger mutableInteger = counterMap.get(testGroupTypeId);
            assertTrue(mutableInteger.getValue() > lowExpectedSize);
            assertTrue(mutableInteger.getValue() < highExpectedSize);
        }

    }

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "size", args = {})
    public void testSize() {
        assertEquals(TestGroupTypeId.values().length, TestGroupTypeId.size());
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "next", args = {})
    public void testNext() {
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            int index = (testGroupTypeId.ordinal() + 1) % TestGroupTypeId.values().length;
            TestGroupTypeId expectedNext = TestGroupTypeId.values()[index];
            assertEquals(expectedNext, testGroupTypeId.next());
        }
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "getUnknownGroupTypeId", args = {})
    public void testGetUnknownGroupTypeId() {
        /*
         * Shows that a generated unknown group type id is unique, not null and not a
         * member of the enum
         */
        Set<TestGroupTypeId> testGroupTypeIds = EnumSet.allOf(TestGroupTypeId.class);
        Set<GroupTypeId> unknownGroupTypeIds = new LinkedHashSet<>();
        for (int i = 0; i < 30; i++) {
            GroupTypeId unknownGroupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
            assertNotNull(unknownGroupTypeId);
            boolean unique = unknownGroupTypeIds.add(unknownGroupTypeId);
            assertTrue(unique);
            assertFalse(testGroupTypeIds.contains(unknownGroupTypeId));
        }
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "getTestGroupTypeIds", args = {})
    public void testGetTestGroupTypeIds() {
        assertEquals(Arrays.asList(TestGroupTypeId.values()), TestGroupTypeId.getTestGroupTypeIds());
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeId.class, name = "getShuffledTestGroupTypeIds", args = {
            RandomGenerator.class })
    public void testGetShuffledTestGroupTypeIds() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4386761356441288660L);

        List<TestGroupTypeId> groupPropertyIds = TestGroupTypeId.getTestGroupTypeIds();

        Collections.shuffle(groupPropertyIds, new Random(randomGenerator.nextLong()));

        List<TestGroupTypeId> actuaIds = TestGroupTypeId
                .getShuffledTestGroupTypeIds(RandomGeneratorProvider.getRandomGenerator(4386761356441288660L));

        assertEquals(groupPropertyIds, actuaIds);
    }
}
