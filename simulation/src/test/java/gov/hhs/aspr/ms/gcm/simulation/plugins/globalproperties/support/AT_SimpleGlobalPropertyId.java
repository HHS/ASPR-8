package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimpleGlobalPropertyId {

    @Test
    @UnitTestConstructor(target = SimpleGlobalPropertyId.class, args = { Object.class })
    public void testConstructor() {
        assertNotNull(new SimpleGlobalPropertyId(5));

        assertThrows(RuntimeException.class, () -> new SimpleGlobalPropertyId(null));
    }

    @Test
    @UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "getValue", args = {})
    public void testGetValue() {
        for (int i = 0; i < 10; i++) {
            Object value = i * 10 % 5;

            SimpleGlobalPropertyId simpleGlobalPropertyId = new SimpleGlobalPropertyId(value);

            assertEquals(value, simpleGlobalPropertyId.getValue());
        }
    }

    @Test
    @UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "toString", args = {})
    public void testToString() {
        /*
         * Show that the toString of the SimpleGlobalPropertyId equals its
         * input's toString
         */

        assertEquals(Integer.toString(5), new SimpleGlobalPropertyId(5).toString());
        assertEquals("table", new SimpleGlobalPropertyId("table").toString());
        assertEquals(Double.toString(2345.5345), new SimpleGlobalPropertyId(2345.5345).toString());

    }

    @Test
    @UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "equals", args = { Object.class })
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821493557306496L);
        
        // never equal to another type
		for (int i = 0; i < 30; i++) {
            SimpleGlobalPropertyId simpleGlobalPropertyId = getRandomSimpleGlobalPropertyId(randomGenerator.nextLong());
            assertFalse(simpleGlobalPropertyId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleGlobalPropertyId simpleGlobalPropertyId = getRandomSimpleGlobalPropertyId(randomGenerator.nextLong());
			assertFalse(simpleGlobalPropertyId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleGlobalPropertyId simpleGlobalPropertyId = getRandomSimpleGlobalPropertyId(randomGenerator.nextLong());
			assertTrue(simpleGlobalPropertyId.equals(simpleGlobalPropertyId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleGlobalPropertyId simpleGlobalPropertyId1 = getRandomSimpleGlobalPropertyId(seed);
			SimpleGlobalPropertyId simpleGlobalPropertyId2 = getRandomSimpleGlobalPropertyId(seed);
			assertFalse(simpleGlobalPropertyId1 == simpleGlobalPropertyId2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(simpleGlobalPropertyId1.equals(simpleGlobalPropertyId2));
				assertTrue(simpleGlobalPropertyId2.equals(simpleGlobalPropertyId1));
			}
		}

		// different inputs yield unequal plugin SimpleGlobalPropertyIds
		Set<SimpleGlobalPropertyId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleGlobalPropertyId simpleGlobalPropertyId = getRandomSimpleGlobalPropertyId(randomGenerator.nextLong());
			set.add(simpleGlobalPropertyId);
		}
		assertEquals(100, set.size());
    }

    @Test
    @UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6496930019926275913L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
			SimpleGlobalPropertyId simpleGlobalPropertyId1 = getRandomSimpleGlobalPropertyId(seed);
			SimpleGlobalPropertyId simpleGlobalPropertyId2 = getRandomSimpleGlobalPropertyId(seed);

            assertEquals(simpleGlobalPropertyId1, simpleGlobalPropertyId2);
            assertEquals(simpleGlobalPropertyId1.hashCode(), simpleGlobalPropertyId2.hashCode());
        }

        // hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            SimpleGlobalPropertyId simpleGlobalPropertyId = getRandomSimpleGlobalPropertyId(randomGenerator.nextLong());
            hashCodes.add(simpleGlobalPropertyId.hashCode());
        }

        assertEquals(100, hashCodes.size());
    }

    private SimpleGlobalPropertyId getRandomSimpleGlobalPropertyId(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        return new SimpleGlobalPropertyId(randomGenerator.nextInt());
    }
}
