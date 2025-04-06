package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

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
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_SimpleReportLabel {

	@Test
	@UnitTestConstructor(target = SimpleReportLabel.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleReportLabel(5));

		// show that a null report label is not thrown
		ContractException contractException = assertThrows(ContractException.class, () -> new SimpleReportLabel(null));
		assertEquals(contractException.getErrorType(), ReportError.NULL_REPORT_LABEL);

	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "toString", args = {})
	public void testToString() {
		Object value = 325;
		SimpleReportLabel simpleReportLabel = new SimpleReportLabel(value);
		String expectedString = "SimpleReportLabel [value=" + value + "]";
		String actualString = simpleReportLabel.toString();

		assertEquals(expectedString, actualString);
	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6496930019639555913L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleReportLabel simpleReportLabel1 = getRandomSimpleReportLabel(seed);
			SimpleReportLabel simpleReportLabel2 = getRandomSimpleReportLabel(seed);

			assertEquals(simpleReportLabel1, simpleReportLabel2);
			assertEquals(simpleReportLabel1.hashCode(), simpleReportLabel2.hashCode());

		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleReportLabel simpleReportLabel = getRandomSimpleReportLabel(randomGenerator.nextLong());
			hashCodes.add(simpleReportLabel.hashCode());
		}
		
		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6623921493557306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			SimpleReportLabel simpleReportLabel = getRandomSimpleReportLabel(randomGenerator.nextLong());
			assertFalse(simpleReportLabel.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			SimpleReportLabel simpleReportLabel = getRandomSimpleReportLabel(randomGenerator.nextLong());
			assertFalse(simpleReportLabel.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			SimpleReportLabel simpleReportLabel = getRandomSimpleReportLabel(randomGenerator.nextLong());
			assertTrue(simpleReportLabel.equals(simpleReportLabel));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			SimpleReportLabel simpleReportLabel1 = getRandomSimpleReportLabel(seed);
			SimpleReportLabel simpleReportLabel2 = getRandomSimpleReportLabel(seed);
			assertFalse(simpleReportLabel1 == simpleReportLabel2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(simpleReportLabel1.equals(simpleReportLabel2));
				assertTrue(simpleReportLabel2.equals(simpleReportLabel1));
			}
		}

		// different inputs yield unequal simpleReportLabels
		Set<SimpleReportLabel> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			SimpleReportLabel simpleReportLabel = getRandomSimpleReportLabel(randomGenerator.nextLong());
			set.add(simpleReportLabel);
		}
		assertEquals(100, set.size());

	}
	
	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "getValue", args = {})
	public void testGetValue() {
		Object value = "some value";
		SimpleReportLabel simpleReportLabel = new SimpleReportLabel(value);
		assertEquals(value, simpleReportLabel.getValue());
		
		value = 678;
		simpleReportLabel = new SimpleReportLabel(value);
		assertEquals(value, simpleReportLabel.getValue());
		
		
		value = false;
		simpleReportLabel = new SimpleReportLabel(value);
		assertEquals(value, simpleReportLabel.getValue());
		
		value = 2.98;
		simpleReportLabel = new SimpleReportLabel(value);
		assertEquals(value, simpleReportLabel.getValue());

	}

	private SimpleReportLabel getRandomSimpleReportLabel(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new SimpleReportLabel(randomGenerator.nextInt());
	}
}