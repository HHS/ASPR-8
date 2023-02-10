package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

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
		List<String> headerStrings = ReportHeader.builder().build().getHeaderStrings();
		assertNotNull(headerStrings);
		assertTrue(headerStrings.isEmpty());

		/*
		 * Show that the returned header is composed of the inputs in the
		 * correct order
		 */
		ReportHeader reportHeader = ReportHeader.builder().add("alpha").add("beta").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));

		reportHeader = ReportHeader.builder().add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("beta", headerStrings.get(0));
		assertEquals("alpha", headerStrings.get(1));

		/*
		 * Show that repeated values are handled correctly
		 */
		reportHeader = ReportHeader.builder().add("alpha").add("beta").add("alpha").build();
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
	@UnitTestMethod(target = ReportHeader.Builder.class, name = "build", args = {})
	public void testBuild() {
		ReportHeader reportHeader = ReportHeader.builder().build();
		assertNotNull(reportHeader);

		reportHeader = ReportHeader.builder().add("alpha").build();
		assertNotNull(reportHeader);

		reportHeader = ReportHeader.builder().add("alpha").add("beta").build();
		assertNotNull(reportHeader);
	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "getHeaderStrings", args = {})
	public void testGetHeaderStrings() {
		/*
		 * Show that when no strings are added, the resulting header is empty
		 */
		List<String> headerStrings = ReportHeader.builder().build().getHeaderStrings();
		assertNotNull(headerStrings);
		assertTrue(headerStrings.isEmpty());

		/*
		 * Show that the returned header is composed of the inputs in the
		 * correct order
		 */
		ReportHeader reportHeader = ReportHeader.builder().add("alpha").add("beta").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("alpha", headerStrings.get(0));
		assertEquals("beta", headerStrings.get(1));

		reportHeader = ReportHeader.builder().add("beta").add("alpha").build();
		headerStrings = reportHeader.getHeaderStrings();
		assertNotNull(headerStrings);
		assertEquals(2, headerStrings.size());
		assertEquals("beta", headerStrings.get(0));
		assertEquals("alpha", headerStrings.get(1));

		/*
		 * Show that repeated values are handled correctly
		 */
		reportHeader = ReportHeader.builder().add("alpha").add("beta").add("alpha").build();
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

		ReportHeader reportHeader = ReportHeader.builder().build();
		String expectedValue = "ReportHeader [headerStrings=[]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().add("alpha").add("beta").build();
		expectedValue = "ReportHeader [headerStrings=[alpha, beta]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().add("beta").add("alpha").build();
		expectedValue = "ReportHeader [headerStrings=[beta, alpha]]";
		assertEquals(expectedValue, reportHeader.toString());

		reportHeader = ReportHeader.builder().add("alpha").add("beta").add("alpha").build();
		expectedValue = "ReportHeader [headerStrings=[alpha, beta, alpha]]";
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

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2142808365770946523L);

		// show that hash codes are reasonably dispersed
		Set<Integer> hashCodeValues = new LinkedHashSet<>();

		int sampleCount = 1000;

		for (int i = 0; i < sampleCount; i++) {
			ReportHeader.Builder builder = ReportHeader.builder();
			int fieldCount = randomGenerator.nextInt(3) + 1;
			for (int j = 0; j < fieldCount; j++) {
				int length = randomGenerator.nextInt(8) + 3;
				String randomHeaderString = generateRandomString(randomGenerator, length);
				builder.add(randomHeaderString);
			}
			ReportHeader reportHeader = builder.build();
			hashCodeValues.add(reportHeader.hashCode());
		}

		assertTrue(hashCodeValues.size() > 0.8 * sampleCount);

		// show that equal report headers have equal hash codes

		for (int i = 0; i < sampleCount; i++) {
			ReportHeader.Builder builder = ReportHeader.builder();
			ReportHeader.Builder builder2 = ReportHeader.builder();
			int fieldCount = randomGenerator.nextInt(3) + 1;
			for (int j = 0; j < fieldCount; j++) {
				int length = randomGenerator.nextInt(8) + 3;
				String randomHeaderString = generateRandomString(randomGenerator, length);
				builder.add(randomHeaderString);
				builder2.add(new String(randomHeaderString));
			}
			ReportHeader reportHeader = builder.build();
			ReportHeader reportHeader2 = builder2.build();

			assertEquals(reportHeader.hashCode(), reportHeader2.hashCode());

		}

	}

	@Test
	@UnitTestMethod(target = ReportHeader.class, name = "equals", args = { Object.class })
	public void testEquals() {
		ReportHeader.Builder builder = ReportHeader.builder();
		ReportHeader AB1 = builder.add("A").add("B").build();
		ReportHeader BA = builder.add("B").add("A").build();
		ReportHeader ABC1 = builder.add("A").add("B").add("C").build();
		ReportHeader AB2 = builder.add("A").add("B").build();
		ReportHeader ABC2 = builder.add("A").add("B").add("C").build();
		ReportHeader ABC3 = builder.add("A").add("B").add("C").build();

		// Reflexive
		assertEquals(AB1, AB1);
		assertEquals(BA, BA);
		assertEquals(ABC1, ABC1);
		assertEquals(AB2, AB2);
		assertEquals(ABC2, ABC2);

		// Symmetric
		assertEquals(AB1, AB2);
		assertEquals(AB2, AB1);

		// Transitive
		assertEquals(ABC1, ABC2);
		assertEquals(ABC2, ABC1);
		assertEquals(ABC1, ABC3);
		assertEquals(ABC3, ABC1);
		assertEquals(ABC3, ABC2);
		assertEquals(ABC2, ABC3);

		// show that it cannot be equal to null
		assertNotEquals(AB1, null);
		assertNotEquals(BA, null);
		assertNotEquals(ABC1, null);
		assertNotEquals(AB2, null);
		assertNotEquals(ABC2, null);
		assertNotEquals(ABC3, null);

		// show that report headers with different inputs are not equal
		assertNotEquals(AB1, BA);
		assertNotEquals(AB1, ABC1);
		assertNotEquals(AB1, ABC2);
		assertNotEquals(AB1, ABC3);
		assertNotEquals(BA, ABC1);
		assertNotEquals(BA, ABC2);
		assertNotEquals(BA, ABC3);

	}

}
