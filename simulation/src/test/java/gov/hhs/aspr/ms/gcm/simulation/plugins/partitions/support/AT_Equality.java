package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_Equality {

	/**
	 * Tests {@link Equality#isCompatibleComparisonValue(int)}
	 */
	@Test
	@UnitTestMethod(target = Equality.class, name = "isCompatibleComparisonValue", args = { int.class })
	public void testIsCompatibleComparisonValue() {

		assertEquals(6, Equality.values().length);

		/*
		 * Show that the six Equality members return the proper compatibility
		 * with integer comparison values.
		 */

		for (int i = 1; i <= 10; i++) {
			assertFalse(Equality.EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.EQUAL.isCompatibleComparisonValue(0));
			assertFalse(Equality.EQUAL.isCompatibleComparisonValue(i));

			assertTrue(Equality.NOT_EQUAL.isCompatibleComparisonValue(-i));
			assertFalse(Equality.NOT_EQUAL.isCompatibleComparisonValue(0));
			assertTrue(Equality.NOT_EQUAL.isCompatibleComparisonValue(i));

			assertTrue(Equality.LESS_THAN.isCompatibleComparisonValue(-i));
			assertFalse(Equality.LESS_THAN.isCompatibleComparisonValue(0));
			assertFalse(Equality.LESS_THAN.isCompatibleComparisonValue(i));

			assertTrue(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(0));
			assertFalse(Equality.LESS_THAN_EQUAL.isCompatibleComparisonValue(i));

			assertFalse(Equality.GREATER_THAN.isCompatibleComparisonValue(-i));
			assertFalse(Equality.GREATER_THAN.isCompatibleComparisonValue(0));
			assertTrue(Equality.GREATER_THAN.isCompatibleComparisonValue(i));

			assertFalse(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(-i));
			assertTrue(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(0));
			assertTrue(Equality.GREATER_THAN_EQUAL.isCompatibleComparisonValue(i));
		}
	}

	
	@Test
	@UnitTestMethod(target = Equality.class, name = "getNegation", args = { Equality.class })
	public void testGetNegation() {
		assertEquals(6, Equality.values().length);
		assertEquals(Equality.NOT_EQUAL, Equality.getNegation(Equality.EQUAL));
		assertEquals(Equality.EQUAL, Equality.getNegation(Equality.NOT_EQUAL));
		assertEquals(Equality.LESS_THAN_EQUAL, Equality.getNegation(Equality.GREATER_THAN));
		assertEquals(Equality.LESS_THAN, Equality.getNegation(Equality.GREATER_THAN_EQUAL));
		assertEquals(Equality.GREATER_THAN_EQUAL, Equality.getNegation(Equality.LESS_THAN));
		assertEquals(Equality.GREATER_THAN, Equality.getNegation(Equality.LESS_THAN_EQUAL));
	}
	
	
	@Test
	@UnitTestMethod(target = Equality.class, name = "getRandomEquality", args = { RandomGenerator.class })
	public void testGetRandomEquality() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(81893850178162700L);
		Map<Equality,MutableInteger> map = new LinkedHashMap<>();
		for(Equality equality : Equality.values()) {
			map.put(equality, new MutableInteger());
		}
		
		for(int i =0;i<6000;i++) {
			Equality equality = Equality.getRandomEquality(randomGenerator);
			map.get(equality).increment();
		}
		for(Equality equality : Equality.values()) {
			int count = map.get(equality).getValue();			
			assertTrue(count>900);
			assertTrue(count<1100);
		}
		
		
	}

}
