package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimpleRandomNumberGeneratorId {
	@Test
	@UnitTestConstructor(target = SimpleRandomNumberGeneratorId.class, args = { Object.class })
	public void testSimpleRandomNumberGeneratorId() {

		// precondition test: if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> new SimpleRandomNumberGeneratorId(null));
		assertEquals(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980253793557306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId randomId = getRandomSimpleRandomNumberGeneratorId(randomGenerator.nextLong());
			assertFalse(randomId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId randomId = getRandomSimpleRandomNumberGeneratorId(randomGenerator.nextLong());
			assertFalse(randomId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleRandomNumberGeneratorId randomId = getRandomSimpleRandomNumberGeneratorId(randomGenerator.nextLong());
			assertTrue(randomId.equals(randomId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleRandomNumberGeneratorId randomId1 = getRandomSimpleRandomNumberGeneratorId(seed);
			SimpleRandomNumberGeneratorId randomId2 = getRandomSimpleRandomNumberGeneratorId(seed);
			assertFalse(randomId1 == randomId2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(randomId1.equals(randomId2));
				assertTrue(randomId2.equals(randomId1));
			}
		}

		// different inputs yield unequal ids
		Set<SimpleRandomNumberGeneratorId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRandomNumberGeneratorId randomId = getRandomSimpleRandomNumberGeneratorId(randomGenerator.nextLong());
			set.add(randomId);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 30; i++) {
			Integer input = i;
			SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = new SimpleRandomNumberGeneratorId(input);
			assertEquals(input, simpleRandomNumberGeneratorId.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6496930909591275913L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleRandomNumberGeneratorId randomId1 = getRandomSimpleRandomNumberGeneratorId(seed);
			SimpleRandomNumberGeneratorId randomId2 = getRandomSimpleRandomNumberGeneratorId(seed);

			assertEquals(randomId1, randomId2);
			assertEquals(randomId1.hashCode(), randomId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleRandomNumberGeneratorId randomId = getRandomSimpleRandomNumberGeneratorId(randomGenerator.nextLong());
			hashCodes.add(randomId.hashCode());
		}
		
		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = SimpleRandomNumberGeneratorId.class, name = "toString", args = {})
	public void testToString() {
		SimpleRandomNumberGeneratorId simpleRandomNumberGeneratorId = new SimpleRandomNumberGeneratorId("Value");
		
		String actualValue = simpleRandomNumberGeneratorId.toString();
		String expectedValue = "Value";
		
		assertEquals(expectedValue, actualValue);
	}

	private SimpleRandomNumberGeneratorId getRandomSimpleRandomNumberGeneratorId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimpleRandomNumberGeneratorId(randomGenerator.nextInt());
	}
}
