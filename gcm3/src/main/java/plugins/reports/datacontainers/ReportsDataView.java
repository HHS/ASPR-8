package plugins.reports.datacontainers;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.ReportId;
import plugins.reports.support.ReportError;
import util.ContractException;

public final class ReportsDataView implements DataView {

	private final ReportsDataManager reportsDataManager;
	private final Context context;

	/**
	 * Creates the Reports Data View from the given {@link Context}
	 * and {@link ReportsDataManager}. Not null tolerant.
	 * 
	  * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain ReportError#NULL_REPORT_DATA_MANAGER}
	 *             if compartment data manager is null</li>
	 * 
	 */
	public ReportsDataView(Context context, ReportsDataManager reportsDataManager) {
		
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (reportsDataManager == null) {
			throw new ContractException(ReportError.NULL_REPORT_DATA_MANAGER);
		}
		
		this.context = context;
		this.reportsDataManager = reportsDataManager;
	}

	/**
	 * Returns true if and only if the report is present
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the report id is null
	 */
	public boolean isActiveReport(ReportId reportId) {
		validateReportIdIsNotNull(reportId);
		return reportsDataManager.isActiveReport(reportId);
	}

	private void validateReportIdIsNotNull(ReportId reportId) {
		if (reportId == null) {
			context.throwContractException(ReportError.NULL_REPORT_ID);
		}
	}
}
