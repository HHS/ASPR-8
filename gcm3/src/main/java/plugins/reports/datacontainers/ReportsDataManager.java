package plugins.reports.datacontainers;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ReportId;

/**
 * Mutable data manager that backs the {@linkplain ReportsDataView}. This data
 * manager is for internal use by the {@link ReportsPlugin} and should not be
 * published.
 * 
 * Contains all active report ids.
 * 
 * @author Shawn Hatch
 *
 */
public final class ReportsDataManager {

	private Set<ReportId> reportIds = new LinkedHashSet<>();

	/**
	 * Returns true if and only if the given report id has been added to this
	 * data manager. Null tolerant.
	 */
	public boolean isActiveReport(ReportId reportId) {
		return reportIds.contains(reportId);
	}

	/**
	 * Adds a reportId to the reportIds stored in this manager.
	 */
	public void addReport(ReportId reportId) {
		reportIds.add(reportId);
	}

}
