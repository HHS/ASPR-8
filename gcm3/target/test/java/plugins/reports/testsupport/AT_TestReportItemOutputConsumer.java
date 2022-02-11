package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.SimpleReportId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestReportItemOutputConsumer.class)
public class AT_TestReportItemOutputConsumer {

	@Test
	@UnitTestMethod(name = "accept", args = { Object.class })
	public void testAccept() {
		// test is covered by the testEquals()
	}

	private static final ReportHeader REPORT_HEADER_1 = ReportHeader.builder().add("alpha").add("beta").build();

	private static final ReportHeader REPORT_HEADER_2 = ReportHeader.builder().add("gamma").add("delta").build();

	// creates a report item from the given inputs
	private static ReportItem getReportItem(int id, ReportHeader reportHeader, int... values) {
		Builder builder = ReportItem.builder();
		builder.setReportId(new SimpleReportId(id)).setReportHeader(reportHeader);
		for (int value : values) {
			builder.addValue(value);
		}
		return builder.build();

	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		/*
		 * Show that TestReportItemOutputConsumer instances are equal if and
		 * only if they have equal inputs
		 */
		TestReportItemOutputConsumer consumer1 = new TestReportItemOutputConsumer();
		TestReportItemOutputConsumer consumer2 = new TestReportItemOutputConsumer();

		// same content
		assertEquals(consumer1, consumer2);

		// different content
		consumer1.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		assertNotEquals(consumer1, consumer2);

		// returned to same content
		consumer2.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		assertEquals(consumer1, consumer2);

		// different headers
		consumer1 = new TestReportItemOutputConsumer();
		consumer2 = new TestReportItemOutputConsumer();
		consumer1.accept(getReportItem(0, REPORT_HEADER_1, 2, 5));
		consumer2.accept(getReportItem(0, REPORT_HEADER_2, 2, 5));
		assertNotEquals(consumer1, consumer2);

		// different report ids
		consumer1 = new TestReportItemOutputConsumer();
		consumer2 = new TestReportItemOutputConsumer();
		consumer1.accept(getReportItem(0, REPORT_HEADER_1, 4, 8, 3));
		consumer2.accept(getReportItem(1, REPORT_HEADER_1, 4, 8, 3));
		assertNotEquals(consumer1, consumer2);

		/*
		 * different duplicate counts
		 */
		consumer1 = new TestReportItemOutputConsumer();
		consumer2 = new TestReportItemOutputConsumer();
		ReportItem reportItem = getReportItem(0, REPORT_HEADER_1, 4, 5);
		consumer1.accept(reportItem);
		consumer2.accept(reportItem);
		assertEquals(consumer1, consumer2);

		consumer1.accept(reportItem);
		assertNotEquals(consumer1, consumer2);
	}

	@Test
	@UnitTestMethod(name = "toString", args = { Object.class })
	public void testToString() {
		
		//add some report items to a consumer		
		TestReportItemOutputConsumer consumer = new TestReportItemOutputConsumer();		
		consumer.accept(getReportItem(13,REPORT_HEADER_1,1,30));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,2,29));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,3,28));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,4,27));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,5,26));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,6,25));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,6,25));
		consumer.accept(getReportItem(13,REPORT_HEADER_1,7,24));
		
		//produce the expected string representation of the consumer 
		String lineSeparator = System.getProperty("line.separator");
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("TestReportItemOutputConsumer [reportItems=");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[1, 30]]	count = 1");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[2, 29]]	count = 1");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[3, 28]]	count = 1");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[4, 27]]	count = 3");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[5, 26]]	count = 1");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[6, 25]]	count = 2");
		sb.append(lineSeparator);
		sb.append("ReportItem [reportId=SimpleReportId [value=13], reportHeader=ReportHeader [headerStrings=[alpha, beta]], values=[7, 24]]	count = 1");
		sb.append(lineSeparator);
		sb.append("]");

		
		assertEquals(sb.toString(), consumer.toString());
		
	}
	
	
	@Test
	@UnitTestMethod(name = "hashCode", args = { Object.class })
	public void testHashCode() {
		// show that equal objects have equal hash codes
		TestReportItemOutputConsumer consumer1 = new TestReportItemOutputConsumer();
		TestReportItemOutputConsumer consumer2 = new TestReportItemOutputConsumer();
		assertEquals(consumer1.hashCode(), consumer2.hashCode());

		consumer1.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		consumer2.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		assertEquals(consumer1.hashCode(), consumer2.hashCode());

		consumer1.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		consumer2.accept(getReportItem(0, REPORT_HEADER_1, 4, 5));
		assertEquals(consumer1.hashCode(), consumer2.hashCode());

		consumer1.accept(getReportItem(1, REPORT_HEADER_1, 3, 9));
		consumer2.accept(getReportItem(1, REPORT_HEADER_1, 3, 9));
		assertEquals(consumer1.hashCode(), consumer2.hashCode());

		/*
		 *  Show that hash codes do not trivially collide
		 */
		

		//create some report items -- enough so that
		List<ReportItem> reportItems = new ArrayList<>();
		for (int id = 0; id < 10; id++) {
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 10; j++) {
					ReportItem reportItem = getReportItem(id, REPORT_HEADER_1, i, j);
					reportItems.add(reportItem);
				}
			}
		}

		Random random = new Random();
		
		List<TestReportItemOutputConsumer> consumers = new ArrayList<>();
		
		//build a reasonable number of consumers 
		for (int i = 0; i < 1000; i++) {
			TestReportItemOutputConsumer consumer = new TestReportItemOutputConsumer();						
			for (int j = 0; j < 10; j++) {
				int index = random.nextInt(reportItems.size());
				consumer.accept(reportItems.get(index));
			}	
			consumers.add(consumer);			
		}
		
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for(TestReportItemOutputConsumer consumer : consumers) {
			hashCodes.add(consumer.hashCode());
		}
		
		double collisionDensity = consumers.size();
		collisionDensity/=hashCodes.size();		
		assertTrue(collisionDensity<2);

	}

}
