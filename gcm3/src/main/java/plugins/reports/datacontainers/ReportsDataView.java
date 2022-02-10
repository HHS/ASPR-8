package plugins.reports.datacontainers;

import nucleus.SimulationContext;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.reports.ReportId;
import plugins.reports.support.ReportError;
import util.ContractException;

public final class ReportsDataView implements DataView {

	private final ReportsDataManager reportsDataManager;
	private final SimulationContext simulationContext;

	/**
	 * Creates the Reports Data View from the given {@link SimulationContext}
	 * and {@link ReportsDataManager}. Not null tolerant.
	 * 
	  * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain ReportError#NULL_REPORT_DATA_MANAGER}
	 *             if compartment data manager is null</li>
	 * 
	 */
	public ReportsDataView(SimulationContext simulationContext, ReportsDataManager reportsDataManager) {
		
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (reportsDataManager == null) {
			throw new ContractException(ReportError.NULL_REPORT_DATA_MANAGER);
		}
		
		this.simulationContext = simulationContext;
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
			simulationContext.throwContractException(ReportError.NULL_REPORT_ID);
		}
	}
}
