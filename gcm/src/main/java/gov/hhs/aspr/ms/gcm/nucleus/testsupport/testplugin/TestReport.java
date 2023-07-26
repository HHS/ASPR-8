package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import java.util.List;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;

/**
 * Test Support report implementation designed to execute test-defined behaviors
 * from within the report. The report first registers its ReportId with its alias
 * by registering it with the TestPlanDataManager. It then schedules the
 * ReportActionPlans that were stored in the TestPluginData that were associated
 * with its alias.
 * 
 * Alias identification exists for the convenience of the test implementor so
 * that tests can name reports and are not bound to the forced ordering pattern
 * implied by ReportId values.
 * 
 *
 */
public final class TestReport {
	private final Object alias;

	/**
	 * Creates the test actor with its alias
	 * 
	 */
	public TestReport(Object alias) {
		this.alias = alias;
	}

	/**
	 * Associates its ReportId. Schedules the ReprtActionPlans that were stored
	 * in the ActionDataView that were associated with its alias.
	 */
	public void init(ReportContext reportContext) {
		TestPlanDataManager testPlanDataManager = reportContext.getDataManager(TestPlanDataManager.class);

		List<TestReportPlan> testReportPlans = testPlanDataManager.getTestReportPlans(alias);
		for (final TestReportPlan testReportPlan : testReportPlans) {
			reportContext.addPlan(testReportPlan::executeAction, testReportPlan.getScheduledTime());
		}
	}

}
