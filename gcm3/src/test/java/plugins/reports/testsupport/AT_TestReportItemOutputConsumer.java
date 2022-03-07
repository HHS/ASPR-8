package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.ExperimentContext;

@UnitTest(target = TestReportItemOutputConsumer.class)
@Disabled
public class AT_TestReportItemOutputConsumer {

	@Test
	@UnitTestMethod(name = "getReportItems", args = { })
	public void testGetReportItems() {
		fail();
	}
		

	@Test
	@UnitTestMethod(name = "init", args = { ExperimentContext.class })
	public void testInit() {
		fail();
	}

//	private static final ReportHeader REPORT_HEADER_1 = ReportHeader.builder().add("alpha").add("beta").build();
//
//	private static final ReportHeader REPORT_HEADER_2 = ReportHeader.builder().add("gamma").add("delta").build();

	// creates a report item from the given inputs
//	private static ReportItem getReportItem(int id, ReportHeader reportHeader, int... values) {
//		Builder builder = ReportItem.builder();
//		builder.setReportId(new SimpleReportId(id)).setReportHeader(reportHeader);
//		for (int value : values) {
//			builder.addValue(value);
//		}
//		return builder.build();
//
//	}

	@Test
	@UnitTestMethod(name = "toString", args = { Object.class })
	public void testToString() {
		
		fail();
		
//		//add some report items to a consumer		
//		TestReportItemOutputConsumer consumer = new TestReportItemOutputConsumer();		
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,1,30));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,2,29));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,3,28));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,5,26));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,6,25));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,6,25));
//		consumer.accept(getReportItem(13,REPORT_HEADER_1,7,24));
//		
//		//produce the expected string representation of the consumer 
//		String lineSeparator = System.getProperty("line.separator");
//		
//		StringBuilder sb = new StringBuilder();
//		
//		sb.append("TestReportItemOutputConsumer [reportItems=");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[1, 30]]	count = 1");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[2, 29]]	count = 1");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[3, 28]]	count = 1");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[4, 27]]	count = 3");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[5, 26]]	count = 1");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[6, 25]]	count = 2");
//		sb.append(lineSeparator);
//		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[7, 24]]	count = 1");
//		sb.append(lineSeparator);
//		sb.append("]");
//
//		
//		assertEquals(sb.toString(), consumer.toString());
		
	}

}
