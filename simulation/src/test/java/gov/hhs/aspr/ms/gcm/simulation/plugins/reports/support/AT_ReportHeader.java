package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_ReportHeader {

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ReportHeader.builder());
	}

	@Test
	@UnitTestMethod(target = ReportHeader.Builder.class, name = "add", args = { String.class })
	public void testAdd() {
		/*
		 * Show that when no strings are added, the resulting header is empty
		 */
		List<String> headerStrings = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).build().getHeaderStrings();
		assertNotNull(headerStrings);
		assertTrue(headerStrings.isEmpty());

		/*
		 * Show that the returned header is composed of the inputs in the
		 * correct order
		 */
		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("beta", headerStrings.get(0));
		assertEquals("alpha", headerStrings.get(1));

		/*
		 * Show that repeated values are handled correctly
		 */
		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(3, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));
		assertEquals("alpha", headerStrings.get(2));

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> ReportHeader.builder().add(null));
		assertEquals(ReportError.NULL_REPORT_HEADER_STRING, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ReportHeader.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		SimpleReportLabel reportLabel = new SimpleReportLabel("report");

		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(reportLabel).build();

		assertEquals(reportLabel, reportHeader.getReportLabel());

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class,
				() -> ReportItem.builder().setReportLabel(null));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportItem.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		SimpleReportLabel reportLabel = new SimpleReportLabel("report");

		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(reportLabel).build();

		assertEquals(reportLabel, reportHeader.getReportLabel());
	}

	@Test
	@UnitTestMethod(target = ReportHeader.Builder.class, name = "build", args = {})
	public void testBuild() {
		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).build();
		assertNotNull(reportHeader);

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").build();
		assertNotNull(reportHeader);

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").build();
		assertNotNull(reportHeader);

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class,
				() -> ReportItem.builder().setReportLabel(null));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "getHeaderStrings", args = {})
	public void testGetHeaderStrings() {
		/*
		 * Show that when no strings are added, the resulting header is empty
		 */
		List<String> headerStrings = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).build().getHeaderStrings();
		assertNotNull(headerStrings);
		assertTrue(headerStrings.isEmpty());

		/*
		 * Show that the returned header is composed of the inputs in the
		 * correct order
		 */
		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("beta", headerStrings.get(0));
		assertEquals("alpha", headerStrings.get(1));

		/*
		 * Show that repeated values are handled correctly
		 */
		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(3, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));
		assertEquals("alpha", headerStrings.get(2));
	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "toString", args = {})
	public void testToString() {

		ReportHeader reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).build();
		String expectedValue = "ReportHeader [reportLabel=SimpleReportLabel [value=test], headerStrings=[]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").build();
		expectedValue = "ReportHeader [reportLabel=SimpleReportLabel [value=test], headerStrings=[alpha, beta]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("beta").add("alpha").build();
		expectedValue = "ReportHeader [reportLabel=SimpleReportLabel [value=test], headerStrings=[beta, alpha]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().setReportLabel(new SimpleReportLabel("test")).add("alpha").add("beta").add("alpha").build();
		expectedValue = "ReportHeader [reportLabel=SimpleReportLabel [value=test], headerStrings=[alpha, beta, alpha]]";
		assertEquals(expectedValue, reportHeader.toString());

	}

	private static Character generateRandomCharacter(RandomGenerator randomGenerator) {
		int i = randomGenerator.nextInt(26) + 97;
		return (char) i;
	}

	private static String generateRandomString(RandomGenerator randomGenerator, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(generateRandomCharacter(randomGenerator));
		}
		return sb.toString();
	}

	private ReportHeader getRandomReportHeader(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		ReportHeader.Builder builder = ReportHeader.builder();

		builder.setReportLabel(new SimpleReportLabel(randomGenerator.nextInt()));

		int fieldCount = randomGenerator.nextInt(5) + 1;
		for (int i = 0; i < fieldCount; i++) {
			int stringLength = randomGenerator.nextInt(5) + 1;
			String fieldValue = generateRandomString(randomGenerator, stringLength);
			builder.add(fieldValue);
		}

		return builder.build();
	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2142808365770946523L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ReportHeader reportHeader1 = getRandomReportHeader(seed);
			ReportHeader reportHeader2 = getRandomReportHeader(seed);

			assertEquals(reportHeader1, reportHeader2);
			assertEquals(reportHeader1.hashCode(), reportHeader2.hashCode());

		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ReportHeader reportHeader = getRandomReportHeader(randomGenerator.nextLong());
			hashCodes.add(reportHeader.hashCode());
		}
		
		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821400224306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			ReportHeader reportHeader = getRandomReportHeader(randomGenerator.nextLong());
			assertFalse(reportHeader.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ReportHeader reportHeader = getRandomReportHeader(randomGenerator.nextLong());
			assertFalse(reportHeader.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ReportHeader reportHeader = getRandomReportHeader(randomGenerator.nextLong());
			assertTrue(reportHeader.equals(reportHeader));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ReportHeader reportHeader1 = getRandomReportHeader(seed);
			ReportHeader reportHeader2 = getRandomReportHeader(seed);
			assertFalse(reportHeader1 == reportHeader2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(reportHeader1.equals(reportHeader2));
				assertTrue(reportHeader2.equals(reportHeader1));
			}
		}

		// different inputs yield unequal report headers
		Set<ReportHeader> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ReportHeader reportHeader = getRandomReportHeader(randomGenerator.nextLong());
			set.add(reportHeader);
		}
		assertEquals(100, set.size());
	}

}
