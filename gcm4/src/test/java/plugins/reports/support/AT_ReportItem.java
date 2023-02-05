package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public final class AT_ReportItem {

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ReportItem.builder());
	}

	@Test
	@UnitTestMethod(target = ReportItem.Builder.class, name = "addValue", args = { Object.class })
	public void testAddValue() {

		for (int i = 0; i < 10; i++) {
			ReportItem reportItem = ReportItem	.builder()//
												.setReportId(new SimpleReportId("report"))//
												.setReportHeader(ReportHeader.builder().build())//
												.addValue(i)//
												.addValue(i - 1)//
												.build();//

			assertEquals(Integer.toString(i), reportItem.getValue(0));
			assertEquals(Integer.toString(i - 1), reportItem.getValue(1));
		}

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> ReportItem.builder().addValue(null));
		assertEquals(ReportError.NULL_REPORT_ITEM_ENTRY, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportItem.Builder.class, name = "build", args = {})
	public void testBuild() {

		// precondition tests

		ContractException contractException = assertThrows(ContractException.class, () -> {
			ReportItem.builder().setReportId(new SimpleReportId("report")).build();
		});
		assertEquals(ReportError.NULL_REPORT_HEADER, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			ReportItem.builder().setReportHeader(ReportHeader.builder().build()).build();
		});
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportItem.Builder.class, name = "setReportHeader", args = { ReportHeader.class })
	public void testSetReportHeader() {
		ReportHeader reportHeader = ReportHeader.builder().add("A").add("B").build();

		ReportItem reportItem = ReportItem.builder().setReportHeader(reportHeader).setReportId(new SimpleReportId("report")).build();

		assertEquals(reportHeader, reportItem.getReportHeader());

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> ReportItem.builder().setReportHeader(null));
		assertEquals(ReportError.NULL_REPORT_HEADER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportItem.Builder.class, name = "setReportId", args = { ReportLabel.class })
	public void testSetReportId() {

		SimpleReportId reportId = new SimpleReportId("report");

		ReportItem reportItem = ReportItem.builder().setReportHeader(ReportHeader.builder().build()).setReportId(reportId).build();

		assertEquals(reportId, reportItem.getReportId());

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> ReportItem.builder().setReportId(null));
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "getReportId", args = {})
	public void testGetReportId() {
		SimpleReportId reportId = new SimpleReportId("report");

		ReportItem reportItem = ReportItem.builder().setReportHeader(ReportHeader.builder().build()).setReportId(reportId).build();

		assertEquals(reportId, reportItem.getReportId());
	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "getReportHeader", args = {})

	public void testGetReportHeader() {
		ReportHeader reportHeader = ReportHeader.builder().add("A").add("B").build();

		ReportItem reportItem = ReportItem.builder().setReportHeader(reportHeader).setReportId(new SimpleReportId("report")).build();

		assertEquals(reportHeader, reportItem.getReportHeader());
	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "getValue", args = { int.class })

	public void testGetValue() {
		ReportHeader reportHeader = ReportHeader.builder().add("A").add("B").add("C").add("D").build();
		ReportItem reportItem = ReportItem	.builder()//
											.setReportHeader(reportHeader)//
											.setReportId(new SimpleReportId("report"))//
											.addValue("alpha")//
											.addValue(12)//
											.addValue(false)//
											.addValue(123.42)//
											.build();//

		assertEquals("alpha", reportItem.getValue(0));
		assertEquals(Integer.toString(12), reportItem.getValue(1));
		assertEquals(Boolean.toString(false), reportItem.getValue(2));
		assertEquals(Double.toString(123.42), reportItem.getValue(3));

	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "size", args = {})

	public void testSize() {
		ReportHeader reportHeader = ReportHeader.builder().build();
		SimpleReportId reportId = new SimpleReportId("report");

		for (int i = 0; i < 10; i++) {
			ReportItem.Builder builder = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId);
			for (int j = 0; j < i; j++) {
				builder.addValue(j);
			}
			ReportItem reportItem = builder.build();
			assertEquals(i, reportItem.size());
		}

	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "toString", args = {})
	public void testToString() {
		ReportHeader reportHeader = ReportHeader.builder().build();
		SimpleReportId reportId = new SimpleReportId("report");

		ReportItem reportItem = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId).addValue("A").addValue("B").build();

		String expectedValue = "ReportItem [reportId=SimpleReportId [value=report], reportHeader=ReportHeader [headerStrings=[]], values=[A, B]]";
		String actualValue = reportItem.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = ReportItem.class,name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7481311225319288863L);
		/*
		 * Show equal report items have equal hash codes. We will focus on the
		 * values part since other tests should cover the report id and header.
		 */

		ReportHeader reportHeader = ReportHeader.builder().build();
		SimpleReportId reportId = new SimpleReportId("report");

		ReportItem reportItem1 = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId).build();
		ReportItem reportItem2 = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId).build();
		assertEquals(reportItem1.hashCode(), reportItem2.hashCode());

		reportItem1 = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId).addValue("A").addValue("B").build();
		reportItem2 = ReportItem.builder().setReportHeader(reportHeader).setReportId(reportId).addValue("A").addValue("B").build();
		assertEquals(reportItem1.hashCode(), reportItem2.hashCode());

		/*
		 * Show that the hash codes are reasonable dispersed
		 * 
		 */
		Set<Integer> hashCodes = new LinkedHashSet<>();
		ReportItem.Builder builder = ReportItem.builder();

		int sampleCount = 1000;
		for (int i = 0; i < sampleCount; i++) {
			builder.setReportHeader(reportHeader).setReportId(reportId);
			int fieldCount = randomGenerator.nextInt(3) + 1;
			for(int j = 0;j<fieldCount;j++) {
				int stringLength = randomGenerator.nextInt(5)+1;
				builder.addValue(generateRandomString(randomGenerator, stringLength));
			}
			ReportItem reportItem = builder.build();
			hashCodes.add(reportItem.hashCode());
		}
		assertTrue(hashCodes.size()>4*sampleCount/5);

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
	@UnitTestMethod(target = ReportItem.class,name = "equals", args = { Object.class })

	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7530977954336798039L);

		ReportHeader reportHeader = ReportHeader.builder().build();
		SimpleReportId reportId = new SimpleReportId("report");


		/*
		 * Show that equal objects are equal
		 * 
		 */		
		ReportItem.Builder builder1 = ReportItem.builder();
		ReportItem.Builder builder2 = ReportItem.builder();

		int sampleCount = 100;
		for (int i = 0; i < sampleCount; i++) {
			builder1.setReportHeader(reportHeader).setReportId(reportId);
			builder2.setReportHeader(reportHeader).setReportId(reportId);
			int fieldCount = randomGenerator.nextInt(3) + 1;
			for(int j = 0;j<fieldCount;j++) {
				int stringLength = randomGenerator.nextInt(5)+1;
				String value1 = generateRandomString(randomGenerator, stringLength);
				builder1.addValue(value1);
				String value2 = new String(value1);
				builder2.addValue(value2);
			}
			ReportItem reportItem1= builder1.build();
			ReportItem reportItem2= builder2.build();
			assertEquals(reportItem1, reportItem2);	
			
		}
		
		
		//show that non-equal report items are not equal
		for (int i = 0; i < sampleCount; i++) {
			builder1.setReportHeader(reportHeader).setReportId(reportId);
			builder2.setReportHeader(reportHeader).setReportId(reportId);
			int fieldCount = randomGenerator.nextInt(3) + 1;
			for(int j = 0;j<fieldCount;j++) {
				int stringLength = randomGenerator.nextInt(5)+1;
				String value1 = generateRandomString(randomGenerator, stringLength);
				builder1.addValue(value1);
				String value2 = new String(value1)+"x";
				builder2.addValue(value2);
			}
			ReportItem reportItem1= builder1.build();
			ReportItem reportItem2= builder2.build();
			assertNotEquals(reportItem1, reportItem2);	
			
		}
		
	}

}
