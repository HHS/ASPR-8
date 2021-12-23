package plugins.gcm.experiment.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.ReportId;
import plugins.gcm.experiment.Experiment;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;
import plugins.gcm.experiment.progress.ExperimentProgressLog;
import plugins.reports.support.ReportItem;


/**
 * An implementor of ReportItemHandler that uses java.nio framework. Reports are
 * headered, tab-delimited files.
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOReportItemHandler implements OutputItemHandler {

	private static class LineWriter extends NIOHeaderedOutputItemHandler {

		private final String experimentHeader;
		private final List<String> experimentFields = new ArrayList<>();
		private final Experiment regularExperiment;
		private final boolean displayExperimentColumnsInReports;

		public LineWriter(final Path path, final Experiment regularExperiment, final boolean displayExperimentColumnsInReports, final ExperimentProgressLog experimentProgressLog) {
			super(path);

			this.displayExperimentColumnsInReports = displayExperimentColumnsInReports;

			this.regularExperiment = regularExperiment;

			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					experimentFields.add(regularExperiment.getExperimentFieldName(i));
				}
				final StringBuilder sb = new StringBuilder();
				for (final String experimentField : experimentFields) {
					sb.append("\t");
					sb.append(experimentField);
				}
				experimentHeader = sb.toString();
			} else {
				experimentHeader = "";
			}

		}

		@Override
		public Set<Class<?>> getHandledClasses() {
			final Set<Class<?>> result = new LinkedHashSet<>();
			result.add(ReportItem.class);
			return result;
		}

		@Override
		protected String getHeader(final Object output) {
			final ReportItem reportItem = (ReportItem) output;
			final StringBuilder sb = new StringBuilder();
			sb.append("Scenario");
			sb.append("\t");
			sb.append("Replication");
			sb.append(experimentHeader);
			final List<String> headerStrings = reportItem.getReportHeader().getHeaderStrings();
			for (final String headerString : headerStrings) {
				sb.append("\t");
				sb.append(headerString);
			}
			return sb.toString();
		}

		@Override
		protected String getOutputLine(ScenarioId scenarioId, ReplicationId replicationId, final Object output) {
			final ReportItem reportItem = (ReportItem) output;
			final StringBuilder sb = new StringBuilder();
			sb.append(scenarioId);
			sb.append("\t");
			sb.append(replicationId);

			if (displayExperimentColumnsInReports) {
				for (int i = 0; i < regularExperiment.getExperimentFieldCount(); i++) {
					sb.append("\t");
					final Object experimentFieldValue = regularExperiment.getExperimentFieldValue(scenarioId, i);
					sb.append(experimentFieldValue);
				}
			}

			for (int i = 0; i < reportItem.size(); i++) {
				sb.append("\t");
				sb.append(reportItem.getValue(i));
			}
			return sb.toString();
		}
	}

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

		private Scaffold scaffold = new Scaffold();

		/**
		 * Sets the path for the Experiment Column Report. Setting this path to
		 * null turns off the report. Default value is null.
		 */
		public Builder setExperimentColumnReport(final Path path) {
			scaffold.experimentColumnReportPath = path;
			return this;
		}

		/**
		 * Adds the experiment reference to the NIOReportItemHandler. Required
		 * if the Experiment Column Report is turned on. Default value is null.
		 */
		public Builder setRegularExperiment(final Experiment experiment) {
			scaffold.experiment = experiment;
			return this;
		}

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
			scaffold.reportMap.put(reportId, path);
			return this;
		}

		/**
		 * Builds the NIOReportItemHandlerImpl from the information gathered and
		 * resets the internal state of this builder.
		 */
		public NIOReportItemHandler build() {
			try {
				return new NIOReportItemHandler(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the display of experiment columns in all reports. Default value
		 * is true.
		 */
		public Builder setDisplayExperimentColumnsInReports(final boolean displayExperimentColumnsInReports) {
			scaffold.displayExperimentColumnsInReports = displayExperimentColumnsInReports;
			return this;
		}
	}

	

	private static class Scaffold {
		private final Map<ReportId, Path> reportMap = new LinkedHashMap<>();
		private Experiment experiment;
		private boolean displayExperimentColumnsInReports = DEFAULT_DISPLAY_EXPERIMENT_COLUMNS;
		private Path experimentColumnReportPath;

	}

	private final static boolean DEFAULT_DISPLAY_EXPERIMENT_COLUMNS = true;

	private static List<String> getLines(final Experiment experiment) {
		final List<String> result = new ArrayList<>();

		if (experiment != null) {

			/*
			 * Build the header line
			 */
			// final List<String> experimentFields =
			// experiment.getExperimentFields();
			StringBuilder sb = new StringBuilder();
			sb.append("Scenario");
			for (int i = 0; i < experiment.getExperimentFieldCount(); i++) {
				sb.append("\t");
				sb.append(experiment.getExperimentFieldName(i));
			}
			result.add(sb.toString());

			/*
			 * Build the scenario lines
			 */

			int scenarioCount = experiment.getScenarioCount();
			for (int i = 0; i < scenarioCount; i++) {
				final ScenarioId scenarioId = experiment.getScenarioId(i);
				sb = new StringBuilder();
				sb.append(scenarioId);

				for (int j = 0; j < experiment.getExperimentFieldCount(); j++) {
					sb.append("\t");
					final Object experimentFieldValue = experiment.getExperimentFieldValue(scenarioId, j);
					sb.append(experimentFieldValue);
				}
				result.add(sb.toString());
			}
		}
		return result;
	}

	private final Map<Object, LineWriter> lineWriterMap = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Path experimentColumnReportPath;

	private final Experiment experiment;

	private final Map<ReportId, Path> reportMap;

	private final boolean displayExperimentColumnsInReports;

	private NIOReportItemHandler(final Scaffold scaffold) {
		experimentColumnReportPath = scaffold.experimentColumnReportPath;
		experiment = scaffold.experiment;
		reportMap = scaffold.reportMap;
		displayExperimentColumnsInReports = scaffold.displayExperimentColumnsInReports;
	}

	@Override
	public void closeExperiment() {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.closeExperiment();
			}
		}
	}

	@Override
	public void closeSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.closeSimulation(scenarioId, replicationId);
			}
		}
	}

	@Override
	public Set<Class<?>> getHandledClasses() {
		final Set<Class<?>> result = new LinkedHashSet<>();
		result.add(ReportItem.class);
		return result;
	}

	

	
	@Override
	public void handle(ScenarioId scenarioId, ReplicationId replicationId, final Object output) {
		final ReportItem reportItem = (ReportItem) output;
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportId());
		if (lineWriter != null) {
			lineWriter.handle(scenarioId, replicationId, reportItem);
		}
	}

	@Override
	public void openExperiment(final ExperimentProgressLog experimentProgressLog) {
		synchronized (lineWriterMap) {
			if (experimentColumnReportPath != null) {
				writeExperimentScenarioReport(experiment);
			}
			/*
			 * Ensure that each path is associated with exactly one report id
			 */
			final Map<Path, ReportId> pathMap = new LinkedHashMap<>();
			for (final ReportId reportId : reportMap.keySet()) {				
				final Path path = reportMap.get(reportId);
				if (pathMap.containsKey(path)) {
					throw new RuntimeException(path + " is selected for mutiple report id values");
				}
				pathMap.put(path, reportId);
			}

			for (final ReportId reportId : reportMap.keySet()) {				
				final Path path = reportMap.get(reportId);
				final LineWriter lineWriter = new LineWriter(path, experiment, displayExperimentColumnsInReports, experimentProgressLog);
				lineWriter.openExperiment(experimentProgressLog);
				lineWriterMap.put(reportId, lineWriter);
			}
		}
	}

	@Override
	public void openSimulation(final ScenarioId scenarioId, final ReplicationId replicationId) {
		// do nothing
	}

	private void writeExperimentScenarioReport(final Experiment experiment) {
		final List<String> lines = getLines(experiment);
		writeLines(lines);
	}

	private void writeLines(final List<String> lines) {
		try {
			Files.write(experimentColumnReportPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

}
