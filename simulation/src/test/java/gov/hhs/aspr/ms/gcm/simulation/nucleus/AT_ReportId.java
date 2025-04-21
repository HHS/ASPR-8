package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class AT_ReportId {

	@UnitTestConstructor(target = ReportId.class, args = { int.class })
	@Test
	public void testConstructor() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ReportId(i).getValue());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ReportId(i).getValue());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("ReportId [id=" + i + "]", new ReportId(i).toString());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491599465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ReportId reportId1 = getRandomReportId(seed);
			ReportId reportId2 = getRandomReportId(seed);

			assertEquals(reportId1, reportId2);
			assertEquals(reportId1.hashCode(), reportId2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ReportId reportId = getRandomReportId(randomGenerator.nextLong());
			hashCodes.add(reportId.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@UnitTestMethod(target = ReportId.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418344306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			ReportId reportId = getRandomReportId(randomGenerator.nextLong());
			assertFalse(reportId.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ReportId reportId = getRandomReportId(randomGenerator.nextLong());
			assertFalse(reportId.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ReportId reportId = getRandomReportId(randomGenerator.nextLong());
			assertTrue(reportId.equals(reportId));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ReportId reportId1 = getRandomReportId(seed);
			ReportId reportId2 = getRandomReportId(seed);
			assertFalse(reportId1 == reportId2);
			for (int j = 0; j < 10; j++) {
				assertTrue(reportId1.equals(reportId2));
				assertTrue(reportId2.equals(reportId1));
			}
		}

		// different inputs yield unequal reportIds
		Set<ReportId> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ReportId reportId = getRandomReportId(randomGenerator.nextLong());
			set.add(reportId);
		}
		assertEquals(100, set.size());
	}

	private ReportId getRandomReportId(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new ReportId(randomGenerator.nextInt(Integer.MAX_VALUE));
	}
}
