package plugins.reports;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ExperimentContext;
import nucleus.util.ContractException;
import plugins.reports.support.LineWriter;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

/**
 * An experiment-level output management utility for writing report items to
 * multiple files.
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOReportItemHandler {

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for NIOReportItemHandlerImpl
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		/**
		 * Add a report by class reference to the NIOReportItemHandler
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the report
		 *             id is null</li>
		 *             <li>{@linkplain ReportError#NULL_REPORT_PATH} if the path
		 *             is null</li>
		 * 
		 * 
		 */
		public Builder addReport(final ReportId reportId, final Path path) {
			if (path == null) {
				throw new ContractException(ReportError.NULL_REPORT_PATH);
			}
			if (reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}
			data.reportMap.put(reportId, path);
			return this;
		}

		private void validate() {
			/*
			 * Ensure that each path is associated with exactly one report id
			 */
			final Map<Path, ReportId> pathMap = new LinkedHashMap<>();
			for (final ReportId reportId : data.reportMap.keySet()) {
				final Path path = data.reportMap.get(reportId);
				if (pathMap.containsKey(path)) {
					throw new ContractException(ReportError.PATH_COLLISION, path);
				}
				pathMap.put(path, reportId);
			}

		}

		/**
		 * Builds the NIOReportItemHandlerImpl from the information gathered and
		 * resets the internal state of this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#PATH_COLLISION} if multiple
		 *             reports are assigned the same path</li>
		 * 
		 */
		public NIOReportItemHandler build() {
			try {
				validate();
				return new NIOReportItemHandler(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the display of experiment columns in all reports. Default value
		 * is true.
		 */
		public Builder setDisplayExperimentColumnsInReports(final boolean displayExperimentColumnsInReports) {
			data.displayExperimentColumnsInReports = displayExperimentColumnsInReports;
			return this;
		}
	}

	private static class Data {
		private final Map<ReportId, Path> reportMap = new LinkedHashMap<>();
		private boolean displayExperimentColumnsInReports = DEFAULT_DISPLAY_EXPERIMENT_COLUMNS;
	}

	private final static boolean DEFAULT_DISPLAY_EXPERIMENT_COLUMNS = true;

	private final Map<Object, LineWriter> lineWriterMap = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Map<ReportId, Path> reportMap;

	private final boolean displayExperimentColumnsInReports;

	private NIOReportItemHandler(final Data data) {

		reportMap = data.reportMap;
		displayExperimentColumnsInReports = data.displayExperimentColumnsInReports;
	}

	private void closeExperiment(ExperimentContext experimentContext) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.close();
			}
		}
	}

	private void closeSimulation(ExperimentContext experimentContext, final Integer scenarioId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.flush();
			}
		}
	}

	private void handleOuput(ExperimentContext experimentContext, Integer scenarioId, ReportItem reportItem) {
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportId());
		if (lineWriter != null) {
			lineWriter.write(experimentContext, scenarioId, reportItem);
		}
	}

	private void openExperiment(ExperimentContext experimentContext) {
		synchronized (lineWriterMap) {
			for (final ReportId reportId : reportMap.keySet()) {
				final Path path = reportMap.get(reportId);
				final LineWriter lineWriter = new LineWriter(experimentContext, path, displayExperimentColumnsInReports);
				lineWriterMap.put(reportId, lineWriter);
			}
		}
	}

	/**
	 * Initializes this report item handler. It subscribes to the following
	 * experiment level events:
	 * <ul>
	 * <li>Experiment Open : reads and initializes all report files</li>
	 * <li>Simulation Output : directs report items to the appropriate file
	 * writer</li>
	 * <li>Simulation Close : ensures all files are flushed so that the content
	 * of each file is complete for each closed scenario</li>
	 * <li>Experiment Close : closes all file writers</li>
	 * </ul>
	 */
	public void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this::openExperiment);
		experimentContext.subscribeToExperimentClose(this::closeExperiment);
		experimentContext.subscribeToSimulationClose(this::closeSimulation);
		experimentContext.subscribeToOutput(ReportItem.class, this::handleOuput);
	}

}
