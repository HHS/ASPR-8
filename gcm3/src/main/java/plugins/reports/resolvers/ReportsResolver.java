package plugins.reports.resolvers;

import java.util.function.Consumer;

import nucleus.ReportContext;
import nucleus.ReportId;
import nucleus.ResolverContext;
import plugins.reports.datacontainers.ReportsDataManager;
import plugins.reports.datacontainers.ReportsDataView;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportError;
import util.ContractException;

/**
 * <P>
 * Creates and publishes the {@linkplain ReportsDataView} from the
 * {@linkplain ReportsInitialData} instance provided to the plugin.
 * </P>
 * 
 * <P>
 * Creates all reports upon initialization.
 * </P>
 * 
 * <P>
 * Resolves no events.
 * </p>
 * 
 * @author Shawn Hatch
 *
 */
public final class ReportsResolver {
	private ReportsDataManager reportsDataManager;
	private ReportsInitialData reportsInitialData;

	/**
	 * Creates the reports resolver from the given reports initial data
	 * 
	 * @throws ContractException
	 *             <li>if the reports initial data is null</li>
	 * 
	 */
	public ReportsResolver(ReportsInitialData reportsInitialData) {
		if (reportsInitialData == null) {
			throw new ContractException(ReportError.NULL_REPORT_INITIAL_DATA);
		}
		this.reportsInitialData = reportsInitialData;
	}

	/**
	 * Initial behavior of this resolver.
	 * 
	 * <ul>
	 * <li>Adds reports from the {@linkplain ReportsInitialData}</li>
	 * 
	 * <li>Publishes the {@linkplain ReportsDataView}</li>
	 * </ul>
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ReportError#NULL_REPORT_CONTEXT} if the
	 *             report context is null</li>
	 * 
	 */
	public void init(ResolverContext resolverContext) {

		if (resolverContext == null) {
			throw new ContractException(ReportError.NULL_REPORT_CONTEXT);
		}

		reportsDataManager = new ReportsDataManager();
		resolverContext.publishDataView(new ReportsDataView(resolverContext.getSafeContext(), reportsDataManager));

		for (ReportId reportId : reportsInitialData.getReportIds()) {
			reportsDataManager.addReport(reportId);
			Consumer<ReportContext> reportInitialBehavior = reportsInitialData.getReportInitialBehavior(reportId);
			resolverContext.addReport(reportId, reportInitialBehavior);
		}
		reportsInitialData = null;
	}

}
