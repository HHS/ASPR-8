package nucleus.testsupport.actionplugin;

import java.util.Set;

import nucleus.ReportContext;
import nucleus.ReportId;

/**
 * Test Support report implementation designed to execute test-defined behaviors
 * from within the report. It schedules the ActionPlans that were stored in the
 * ActionDataView that were associated with its report id.
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class ActionReport {

	public void init(ReportContext reportContext) {
		ActionDataView actionDataView = reportContext.getDataView(ActionDataView.class).get();

		// retrieve the action plans from the action data view and schedule them
		// with the context
		ReportId reportid = reportContext.getCurrentReportId();
		Set<ReportActionPlan> reportActionPlans = actionDataView.getReportActionPlans(reportid);
		for (final ReportActionPlan reportActionPlan : reportActionPlans) {
			reportContext.addPlan(reportActionPlan::executeAction, reportActionPlan.getScheduledTime());
		}
	}

}
