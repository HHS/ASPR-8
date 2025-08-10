package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_ResourceInitialization {

	@Test
	@UnitTestConstructor(target = ResourceInitialization.class, args = { ResourceId.class, Long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceInitialization.class, name = "getResourceId", args = {})
	public void testGetResourceId() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			ResourceInitialization resourceInitialization = new ResourceInitialization(testResourceId, 123L);
			assertEquals(testResourceId, resourceInitialization.getResourceId());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceInitialization.class, name = "getAmount", args = {})
	public void testGetAmount() {
		for (long value = 0; value < 10; value++) {
			ResourceInitialization resourceInitialization = new ResourceInitialization(TestResourceId.RESOURCE_3,
					value);
			assertEquals(value, resourceInitialization.getAmount().longValue());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceInitialization.class, name = "toString", args = {})
	public void testToString() {
		ResourceInitialization resourceInitialization = new ResourceInitialization(TestResourceId.RESOURCE_3, 15L);
		assertEquals("ResourceAssignment [resourceId=RESOURCE_3, amount=15]", resourceInitialization.toString());
	}

	private ResourceInitialization getRandomResourceInitialization(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new ResourceInitialization(TestResourceId.getRandomResourceId(randomGenerator),
				(long) randomGenerator.nextInt(100000));
	}

	@Test
	@UnitTestMethod(target = ResourceInitialization.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8342493324811391268L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			ResourceInitialization resourceInitialization = getRandomResourceInitialization(randomGenerator.nextLong());
			assertFalse(resourceInitialization.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ResourceInitialization resourceInitialization = getRandomResourceInitialization(randomGenerator.nextLong());
			assertFalse(resourceInitialization.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ResourceInitialization resourceInitialization = getRandomResourceInitialization(randomGenerator.nextLong());
			assertTrue(resourceInitialization.equals(resourceInitialization));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceInitialization resourceInitialization1 = getRandomResourceInitialization(seed);
			ResourceInitialization resourceInitialization2 = getRandomResourceInitialization(seed);
			assertFalse(resourceInitialization1 == resourceInitialization2);
			for (int j = 0; j < 10; j++) {
				assertTrue(resourceInitialization1.equals(resourceInitialization2));
				assertTrue(resourceInitialization1.equals(resourceInitialization2));
			}
		}

		// Different inputs yield unequal objects. There is a low probability with
		// 500,000 possible values
		Set<ResourceInitialization> resourceInitializations = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ResourceInitialization resourceInitialization = getRandomResourceInitialization(randomGenerator.nextLong());
			resourceInitializations.add(resourceInitialization);

		}

		assertEquals(100, resourceInitializations.size());

	}

	@Test
	@UnitTestMethod(target = ResourceInitialization.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5406419164101868160L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceInitialization resourceInitialization1 = getRandomResourceInitialization(seed);
			ResourceInitialization resourceInitialization2 = getRandomResourceInitialization(seed);
			assertEquals(resourceInitialization1,resourceInitialization2);
			assertEquals(resourceInitialization1.hashCode(),resourceInitialization2.hashCode());
		}
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ResourceInitialization resourceInitialization = getRandomResourceInitialization(randomGenerator.nextLong());
			hashCodes.add(resourceInitialization.hashCode());

		}

		assertEquals(100, hashCodes.size());		
	}

}
