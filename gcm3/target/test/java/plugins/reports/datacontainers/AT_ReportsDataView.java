package plugins.reports.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.DataManagerContext;
import nucleus.ResolverId;
import nucleus.SimpleReportId;
import nucleus.testsupport.MockSimulationContext;
import plugins.reports.ReportId;
import plugins.reports.support.ReportError;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportsDataView.class)
public class AT_ReportsDataView implements DataView {

	@Test
	@UnitTestConstructor(args = { DataManagerContext.class, ReportsDataManager.class })
	public void testConstructor() {
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		ReportsDataManager reportsDataManager = new ReportsDataManager();
		
		ContractException contractException = assertThrows(ContractException.class, ()->new ReportsDataView(null, reportsDataManager));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
		
		contractException = assertThrows(ContractException.class, ()->new ReportsDataView(mockSimulationContext, null));
		assertEquals(ReportError.NULL_REPORT_DATA_MANAGER, contractException.getErrorType());

		
	}

	@Test
	@UnitTestMethod(name = "isActiveReport", args = { ResolverId.class })
	public void testIsActiveReport() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		ReportsDataManager reportsDataManager = new ReportsDataManager();
		
		ReportsDataView reportsDataView = new ReportsDataView(mockSimulationContext, reportsDataManager);
		
		ReportId reportId = new SimpleReportId("report");
		
		assertFalse(reportsDataView.isActiveReport(reportId));
		reportsDataManager.addReport(reportId);
		assertTrue(reportsDataView.isActiveReport(reportId));
		
	}

}
