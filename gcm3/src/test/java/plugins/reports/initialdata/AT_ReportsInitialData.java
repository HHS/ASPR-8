package plugins.reports.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.SimpleReportId;
import plugins.reports.ReportId;
import plugins.reports.support.ReportError;
import util.ContractException;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportsInitialData.class)
public class AT_ReportsInitialData implements DataView {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ReportsInitialData.builder());
	}

	@Test
	@UnitTestMethod(target = ReportsInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(ReportsInitialData.builder().build());
	}

	@Test
	@UnitTestMethod(target = ReportsInitialData.Builder.class, name = "addReport", args = { ReportId.class, Supplier.class })
	public void testAddReport() {
		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		ReportId reportId_1 = new SimpleReportId("report 1");
		expectedReportIds.add(reportId_1);
		ReportId reportId_2 = new SimpleReportId("report 2");
		expectedReportIds.add(reportId_2);

		ReportsInitialData.Builder builder = ReportsInitialData.builder();
		builder.addReport(reportId_1, () -> (c) -> {
		});
		builder.addReport(reportId_2, () -> (c) -> {
		});
		ReportsInitialData reportsInitialData = builder.build();

		Set<ReportId> actualReportIds = reportsInitialData.getReportIds();
		assertEquals(expectedReportIds, actualReportIds);

		for (ReportId reportId : expectedReportIds) {
			assertNotNull(reportsInitialData.getReportInitialBehavior(reportId));
		}

		ContractException contractException = assertThrows(ContractException.class, () -> builder.addReport(null, () -> (c) -> {
		}));
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

		ReportId reportId_3 = new SimpleReportId("report 3");
		contractException = assertThrows(ContractException.class, () -> builder.addReport(reportId_3, null));
		assertEquals(ReportError.NULL_SUPPLIER, contractException.getErrorType());

		builder.addReport(reportId_1, () -> (c) -> {
		});
		contractException = assertThrows(ContractException.class, () -> builder.addReport(reportId_1, () -> (c) -> {
		}));
		assertEquals(ReportError.DUPLICATE_REPORT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getReportIds", args = { ReportId.class, Supplier.class })
	public void testGetReportIds() {

		ReportsInitialData.Builder builder = ReportsInitialData.builder();

		Set<ReportId> expectedReportIds = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			ReportId reportId = new SimpleReportId(i);
			expectedReportIds.add(reportId);
			builder.addReport(reportId, () -> (c) -> {
			});
		}
		ReportsInitialData reportsInitialData = builder.build();

		Set<ReportId> actualReportIds = reportsInitialData.getReportIds();
		assertEquals(expectedReportIds, actualReportIds);

	}

	@Test
	@UnitTestMethod(name = "getReportInitialBehavior", args = { ReportId.class })
	public void testGetReportInitialBehavior() {

		ReportsInitialData.Builder builder = ReportsInitialData.builder();

		/*
		 * Create a container to hold unique mutable integers that will show
		 * that each supplier can be stimulated to supply a ReportContext
		 * consumer.
		 */
		Map<ReportId, MutableInteger> values = new LinkedHashMap<>();

		// Create a container for the expected report ids
		Set<ReportId> expectedReportIds = new LinkedHashSet<>();

		// Create a few report ids and have each one associated with its own
		// supplier.
		for (int i = 0; i < 10; i++) {
			ReportId reportId = new SimpleReportId(i);
			expectedReportIds.add(reportId);
			builder.addReport(reportId, () -> (c) -> {
				values.get(reportId).increment();
			});
			values.put(reportId, new MutableInteger());
		}

		ReportsInitialData reportsInitialData = builder.build();

		// show that the report ids contained in the initial data agree with
		// expectation
		assertEquals(expectedReportIds, reportsInitialData.getReportIds());

		// Show that the values hold only zero valued mutable integers
		for (ReportId reportId : expectedReportIds) {
			assertEquals(0, values.get(reportId).getValue());
		}

		/*
		 * For each report id, get the Consumer of ReportContext associate with
		 * the report id and stimulate that consumer.
		 */
		for (ReportId reportId : expectedReportIds) {
			reportsInitialData.getReportInitialBehavior(reportId).accept(null);
		}

		/*
		 * Show that the values are now all set to one implying that the
		 * suppliers are being properly mapped to the report ids and properly
		 * used to generate the consumers.
		 */
		for (ReportId reportId : expectedReportIds) {
			assertEquals(1, values.get(reportId).getValue());
		}

	}

}
