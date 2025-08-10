package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_IdentifiableFunction {

	@Test
	@UnitTestConstructor(target = IdentifiableFunction.class, args = { Object.class, Function.class })
	public void testConstructor() {
		for (int i = 0; i < 30; i++) {
			int input = i;
			String expectedValue = Integer.toString(input);
			IdentifiableFunction<Integer> f = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
			Object actualValue = f.getFunction().apply(input);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(target = IdentifiableFunction.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// identifiable functions are equal if and only if their internal id
		// values are equal
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980997618377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			IdentifiableFunction<Integer> identifiableFunction = getRandomIdentifiableFunction(randomGenerator.nextLong());
			assertFalse(identifiableFunction.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			IdentifiableFunction<Integer> identifiableFunction = getRandomIdentifiableFunction(randomGenerator.nextLong());
			assertFalse(identifiableFunction.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			IdentifiableFunction<Integer> identifiableFunction = getRandomIdentifiableFunction(randomGenerator.nextLong());
			assertTrue(identifiableFunction.equals(identifiableFunction));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			IdentifiableFunction<Integer> identifiableFunction1 = getRandomIdentifiableFunction(seed);
			IdentifiableFunction<Integer> identifiableFunction2 = getRandomIdentifiableFunction(seed);
			assertFalse(identifiableFunction1 == identifiableFunction2);
			for (int j = 0; j < 10; j++) {
				assertTrue(identifiableFunction1.equals(identifiableFunction2));
				assertTrue(identifiableFunction2.equals(identifiableFunction1));
			}
		}

		// different inputs yield unequal identifiableFunctions
		Set<IdentifiableFunction<Integer>> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			IdentifiableFunction<Integer> identifiableFunction = getRandomIdentifiableFunction(randomGenerator.nextLong());
			set.add(identifiableFunction);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = IdentifiableFunction.class, name = "getFunction", args = {})
	public void testGetFunction() {
		/*
		 * Show that the event function is retrievable by executing that
		 * function against some input
		 */

		for (int i = 0; i < 30; i++) {
			int input = i;
			String expectedValue = Integer.toString(input);
			IdentifiableFunction<Integer> f = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
			Object actualValue = f.getFunction().apply(input);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(target = IdentifiableFunction.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2666881508465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			IdentifiableFunction<Integer> identifiableFunction1 = getRandomIdentifiableFunction(seed);
			IdentifiableFunction<Integer> identifiableFunction2 = getRandomIdentifiableFunction(seed);

			assertEquals(identifiableFunction1, identifiableFunction2);
			assertEquals(identifiableFunction1.hashCode(), identifiableFunction2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			IdentifiableFunction<Integer> identifiableFunction = getRandomIdentifiableFunction(randomGenerator.nextLong());
			hashCodes.add(identifiableFunction.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private IdentifiableFunction<Integer> getRandomIdentifiableFunction(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new IdentifiableFunction<>(randomGenerator.nextInt(), (Integer n) -> Integer.toString(n));
	}
}
