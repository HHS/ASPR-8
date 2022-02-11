package plugins.reports.datacontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.ReportId;
import nucleus.SimpleReportId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportsDataManager.class)
public final class AT_ReportsDataManager {

	@Test
	@UnitTestConstructor( args = {ReportId.class})
	public void testConstructor() {
		// this test is covered by the remaining tests
	}
	
	@Test
	@UnitTestMethod(name = "isActiveReport", args = {ReportId.class})
	public void testIsActiveReport() {
		ReportId reportIdA = new SimpleReportId("A");
		ReportId reportIdB = new SimpleReportId("B");
		
		ReportsDataManager reportsDataManager = new ReportsDataManager();
		assertFalse(reportsDataManager.isActiveReport(reportIdA));
		assertFalse(reportsDataManager.isActiveReport(reportIdB));
		assertFalse(reportsDataManager.isActiveReport(null));
		
		reportsDataManager.addReport(reportIdA);
		assertTrue(reportsDataManager.isActiveReport(reportIdA));
		assertFalse(reportsDataManager.isActiveReport(reportIdB));
		assertFalse(reportsDataManager.isActiveReport(null));
		
		reportsDataManager.addReport(null);
		assertTrue(reportsDataManager.isActiveReport(null));
		
		//repeated addition
		reportsDataManager.addReport(reportIdA);
		assertTrue(reportsDataManager.isActiveReport(reportIdA));
		assertFalse(reportsDataManager.isActiveReport(reportIdB));
		
		//adding a new report id
		
		reportsDataManager.addReport(reportIdB);
		assertTrue(reportsDataManager.isActiveReport(reportIdA));
		assertTrue(reportsDataManager.isActiveReport(reportIdB));
				
	}
	
	@Test
	@UnitTestMethod(name = "addReport", args = {ReportId.class})
	public void testAddReport() {
		//covered by testIsActiveReport()
	}

}
