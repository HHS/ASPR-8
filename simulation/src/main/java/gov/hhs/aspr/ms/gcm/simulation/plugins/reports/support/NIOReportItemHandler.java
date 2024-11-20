package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An experiment-level output management utility for writing report items to
 * multiple files.
 */
public final class NIOReportItemHandler implements Consumer<ExperimentContext> {

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for NIOReportItemHandler
	 */
	public static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		/**
		 * Add a report by class reference to the NIOReportItemHandler
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the report label is null</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PATH} if
		 *                           the path is null</li>
		 *                           </ul>
		 */
		public Builder addReport(final ReportLabel reportLabel, final Path path) {
			if (path == null) {
				throw new ContractException(ReportError.NULL_REPORT_PATH);
			}
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportMap.put(reportLabel, path);
			return this;
		}

		public Builder addExperimentReport(final Path path) {
			if (path == null) {
				throw new ContractException(ReportError.NULL_REPORT_PATH);
			}
			data.experimentReportPath = path;
			return this;
		}

		private void validate() {
			/*
			 * Ensure that each path is associated with exactly one report label
			 */
			final Map<Path, ReportLabel> pathMap = new LinkedHashMap<>();
			for (final ReportLabel reportLabel : data.reportMap.keySet()) {
				final Path path = data.reportMap.get(reportLabel);
				if (pathMap.containsKey(path)) {
					throw new ContractException(ReportError.PATH_COLLISION, path);
				}
				pathMap.put(path, reportLabel);
			}
		}

		/**
		 * Builds the NIOReportItemHandlerImpl from the information gathered and resets
		 * the internal state of this builder.
		 * 
		 * @throws ContractException {@linkplain ReportError#PATH_COLLISION} if multiple
		 *                           reports are assigned the same path
		 */
		public NIOReportItemHandler build() {
			validate();
			return new NIOReportItemHandler(new Data(data));
		}

		/**
		 * Sets the display of experiment columns in all reports. Default value is true.
		 */
		public Builder setDisplayExperimentColumnsInReports(final boolean displayExperimentColumnsInReports) {
			data.displayExperimentColumnsInReports = displayExperimentColumnsInReports;
			return this;
		}

		/**
		 * Sets the delimiter for an experiment.
		 */
		public Builder setDelimiter(String delimiter) {
			data.delimiter = delimiter;
			return this;
		}
	}

	private static class Data {
		private String delimiter = "\t";
		private Path experimentReportPath;
		private final Map<ReportLabel, Path> reportMap = new LinkedHashMap<>();
		private boolean displayExperimentColumnsInReports = DEFAULT_DISPLAY_EXPERIMENT_COLUMNS;

		public Data() {
		}

		public Data(Data data) {
			delimiter = data.delimiter;
			reportMap.putAll(data.reportMap);
			experimentReportPath = data.experimentReportPath;
			displayExperimentColumnsInReports = data.displayExperimentColumnsInReports;
		}

	}

	private final static boolean DEFAULT_DISPLAY_EXPERIMENT_COLUMNS = true;

	private final Map<Object, LineWriter> lineWriterMap = Collections.synchronizedMap(new LinkedHashMap<>());

	private LineWriter experimentLineWriter;

	private final Map<ReportLabel, Path> reportMap;

	private final String delimiter;

	private final boolean displayExperimentColumnsInReports;

	private final Path experimentReportPath;

	private NIOReportItemHandler(final Data data) {
		delimiter = data.delimiter;
		reportMap = data.reportMap;
		experimentReportPath = data.experimentReportPath;
		displayExperimentColumnsInReports = data.displayExperimentColumnsInReports;
	}

	private void closeExperiment(ExperimentContext experimentContext) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.close();
			}
			if (experimentLineWriter != null) {
				experimentLineWriter.close();
			}
		}
	}

	private void closeSimulation(ExperimentContext experimentContext, final Integer scenarioId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.flush();
			}
			if (experimentLineWriter != null) {
				experimentLineWriter.writeScenarioMetaData(experimentContext, scenarioId);
				experimentLineWriter.flush();
			}
		}
	}

	private void handleReportItem(ExperimentContext experimentContext, Integer scenarioId, ReportItem reportItem) {
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportLabel());
		if (lineWriter != null) {
			lineWriter.writeReportItem(experimentContext, scenarioId, reportItem);
		}
	}

	private void handleReportHeader(ExperimentContext experimentContext, Integer scenarioId,
			ReportHeader reportHeader) {
		final ReportLabel reportLabel = reportHeader.getReportLabel();

		if (reportMap.get(reportLabel) != null) {
			synchronized (lineWriterMap) {
				LineWriter lineWriterUnsafe = lineWriterMap.get(reportLabel);
				if (lineWriterUnsafe == null) {
					lineWriterUnsafe = new LineWriter(experimentContext, reportMap.get(reportLabel),
							displayExperimentColumnsInReports, delimiter);
					lineWriterMap.put(reportLabel, lineWriterUnsafe);
				}
			}
			final LineWriter lineWriter = lineWriterMap.get(reportLabel);
			lineWriter.writeReportHeader(experimentContext, reportHeader);
		}
	}

	private void openExperiment(ExperimentContext experimentContext) {
		if (experimentReportPath != null) {
			this.experimentLineWriter = new LineWriter(experimentContext, experimentReportPath, true, delimiter);
			this.experimentLineWriter.writeExperimentReportHeader(experimentContext);
		}
	}

	/**
	 * Initializes this report item handler. It subscribes to the following
	 * experiment level events:
	 * <ul>
	 * <li>Experiment Open : Reads and initializes all report files. All content
	 * that doesn't correspond to a previously fully executed scenario is
	 * removed.</li>
	 * <li>Simulation Output : directs report items to the appropriate file
	 * writer</li>
	 * <li>Simulation Close : ensures all files are flushed so that the content of
	 * each file is complete for each closed scenario</li>
	 * <li>Experiment Close : closes all file writers</li>
	 * </ul>
	 *
	 * @throws RuntimeException
	 *                          <ul>
	 *                          <li>if an {@link IOException} is thrown during file
	 *                          initialization</li>
	 *                          <li>if the simulation run is continuing from a
	 *                          progress log and the path is not a regular file
	 *                          (path does not exist) during file
	 *                          initialization</li>
	 *                          </ul>
	 */
	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this::openExperiment);
		experimentContext.subscribeToExperimentClose(this::closeExperiment);
		experimentContext.subscribeToSimulationClose(this::closeSimulation);
		experimentContext.subscribeToOutput(ReportItem.class, this::handleReportItem);
		experimentContext.subscribeToOutput(ReportHeader.class, this::handleReportHeader);
	}

}
