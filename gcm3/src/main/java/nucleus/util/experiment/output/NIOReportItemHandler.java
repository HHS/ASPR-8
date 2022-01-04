package nucleus.util.experiment.output;

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
import nucleus.util.experiment.Experiment;
import nucleus.util.experiment.progress.ExperimentProgressLog;
import plugins.reports.support.ReportItem;


/**
 * An implementor of OutputItemHandler that uses the java.nio framework. Reports are
 * headered, tab-delimited files.
 * 
 * @author Shawn Hatch
 *
 */
public final class NIOReportItemHandler implements OutputItemHandler {

	

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
		
		private void validate() {
			/*
			 * Ensure that each path is associated with exactly one report id
			 */
			final Map<Path, ReportId> pathMap = new LinkedHashMap<>();
			for (final ReportId reportId : scaffold.reportMap.keySet()) {				
				final Path path = scaffold.reportMap.get(reportId);
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
	public void closeSimulation(final int scenarioId) {
		synchronized (lineWriterMap) {
			for (final LineWriter lineWriter : lineWriterMap.values()) {
				lineWriter.closeSimulation(scenarioId);
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
	public void handle(int scenarioId, final Object output) {
		final ReportItem reportItem = (ReportItem) output;
		final LineWriter lineWriter = lineWriterMap.get(reportItem.getReportId());
		if (lineWriter != null) {
			lineWriter.handle(scenarioId, reportItem);
		}
	}

	@Override
	public void openExperiment(final ExperimentProgressLog experimentProgressLog) {
		synchronized (lineWriterMap) {
			if (experimentColumnReportPath != null) {
				writeExperimentScenarioReport(experiment);
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
	public void openSimulation(final int scenarioId) {
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
