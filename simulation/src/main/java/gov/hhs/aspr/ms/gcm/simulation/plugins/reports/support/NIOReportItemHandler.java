package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An experiment-level output management utility for writing report items to
 * multiple files.
 */
public final class NIOReportItemHandler implements Consumer<ExperimentContext> {

	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for NIOReportItemHandler
	 */
	public static class Builder {

		private Builder(Data data) {
			this.data = data;
		}

		private Data data;

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
			ensureDataMutability();
			data.reportMap.put(reportLabel, path);
			return this;
		}

		public Builder addExperimentReport(final Path path) {
			if (path == null) {
				throw new ContractException(ReportError.NULL_REPORT_PATH);
			}
			ensureDataMutability();
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
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new NIOReportItemHandler(data);
		}

		/**
		 * Sets the display of experiment columns in all reports. Default value is true.
		 */
		public Builder setDisplayExperimentColumnsInReports(final boolean displayExperimentColumnsInReports) {
			ensureDataMutability();
			data.displayExperimentColumnsInReports = displayExperimentColumnsInReports;
			return this;
		}

		/**
		 * Sets the delimiter for an experiment.
		 */
		public Builder setDelimiter(String delimiter) {
			ensureDataMutability();
			data.delimiter = delimiter;
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}
	}

	private static class Data {
		private String delimiter = "\t";
		private Path experimentReportPath;
		private final Map<ReportLabel, Path> reportMap = new LinkedHashMap<>();
		private boolean displayExperimentColumnsInReports = DEFAULT_DISPLAY_EXPERIMENT_COLUMNS;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			delimiter = data.delimiter;
			reportMap.putAll(data.reportMap);
			experimentReportPath = data.experimentReportPath;
			displayExperimentColumnsInReports = data.displayExperimentColumnsInReports;
			locked = data.locked;
		}

	}

	private final static boolean DEFAULT_DISPLAY_EXPERIMENT_COLUMNS = true;

	private final Map<Object, LineWriter> lineWriterMap = Collections.synchronizedMap(new LinkedHashMap<>());

	// protected by reportHeaderWriteLock
	private final Set<ReportLabel> reportHeadersWritten = new LinkedHashSet<>();

	private LineWriter experimentLineWriter;

	// protects additions to the reportHeadersWritten set
	private Object reportHeaderWriteLock = new Object(){};

	private final Data data;

	private NIOReportItemHandler(final Data data) {
		this.data = data;
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
			return;
		}

		throw new ContractException(ReportError.NO_REPORT_HEADER);
	}

	private void handleReportHeader(ExperimentContext experimentContext, Integer scenarioId,
			ReportHeader reportHeader) {
		final ReportLabel reportLabel = reportHeader.getReportLabel();
		synchronized(reportHeaderWriteLock) {
			if (data.reportMap.get(reportLabel) != null) {
				boolean added = this.reportHeadersWritten.add(reportLabel);

				if (added) {
					LineWriter lineWriterUnsafe = lineWriterMap.get(reportLabel);
					if (lineWriterUnsafe == null) {
						lineWriterUnsafe = new LineWriter(experimentContext, data.reportMap.get(reportLabel),
								data.displayExperimentColumnsInReports, data.delimiter);
						lineWriterMap.put(reportLabel, lineWriterUnsafe);
					}

					final LineWriter lineWriter = lineWriterMap.get(reportLabel);
					lineWriter.writeReportHeader(experimentContext, reportHeader);
				}
			}
		}
	}

	private void openExperiment(ExperimentContext experimentContext) {
		if (data.experimentReportPath != null) {
			this.experimentLineWriter = new LineWriter(experimentContext, data.experimentReportPath, true, data.delimiter);
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

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}
}
