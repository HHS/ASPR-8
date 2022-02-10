package nucleus;


import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import plugins.reports.support.ReportItem;


/**
 * Output management utility for writing report items to multiple files
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOReportItemHandler  {

	

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
		 * @throws RuntimeException
		 *             <li>if the path is null
		 *             <li>if the report class is null
		 *             <li>if the initialization data is null
		 *             <li>if the initialization contains a null
		 */
		public Builder addReport(final ReportId reportId,final Path path) {
			if (path == null) {
				throw new RuntimeException("null path");
			}
			if (reportId == null) {
				throw new RuntimeException("null report id");
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
					throw new RuntimeException(path + " is selected for mutiple report id values");
				}
				pathMap.put(path, reportId);
			}

		}

		/**
		 * Builds the NIOReportItemHandlerImpl from the information gathered and
		 * resets the internal state of this builder.
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

	public void closeExperiment(ExperimentContext experimentContext) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.close();
			}
		}
	}

	public void closeSimulation(ExperimentContext experimentContext, final Integer scenarioId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.flush();
			}
		}
	}	
	
	public void handleOuput(ExperimentContext experimentContext, Integer scenarioId, ReportItem reportItem) {
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportId());
		if (lineWriter != null) {
			lineWriter.write(experimentContext,scenarioId, reportItem);
		}
	}
	
	public void openExperiment(ExperimentContext experimentContext) {
		synchronized (lineWriterMap) {
			for (final ReportId reportId : reportMap.keySet()) {				
				final Path path = reportMap.get(reportId);
				final LineWriter lineWriter = new LineWriter(experimentContext,path, displayExperimentColumnsInReports);
				lineWriterMap.put(reportId, lineWriter);
			}
		}
	}	
	
	public void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this::openExperiment);
		experimentContext.subscribeToExperimentClose(this::closeExperiment);
		experimentContext.subscribeToSimulationClose(this::closeSimulation);
		experimentContext.subscribeToOutput(ReportItem.class, this::handleOuput);
	}

}
