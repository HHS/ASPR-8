package nucleus.util.experiment.output;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import nucleus.util.experiment.Experiment;
import nucleus.util.experiment.ExperimentError;
import nucleus.util.experiment.output.NIOReportItemHandler.Builder;
import nucleus.util.experiment.progress.ExperimentProgressLog;
import util.ContractException;

public class NIOExperimentSpaceReport implements OutputItemHandler {

	private static class Data {
		private Experiment experiment;
		private Path experimentColumnReportPath;

	}

	private final Data data;

	public static class Builder {
		private Data data = new Data();

		private void validate() {
			if (data.experiment == null) {
				throw new ContractException(ExperimentError.NULL_EXPERIMENT);
			}
			if (data.experimentColumnReportPath == null) {
				throw new ContractException(ExperimentError.NULL_EXPERIMENT_REPORT_PATH);
			}
		}

		public NIOExperimentSpaceReport build() {
			try {
				validate();
				return new NIOExperimentSpaceReport(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the path for the Experiment Column Report.
		 */
		public Builder setExperimentColumnReport(final Path path) {
			data.experimentColumnReportPath = path;
			return this;
		}

		/**
		 * Adds the experiment reference
		 */
		public Builder setExperiment(final Experiment experiment) {
			data.experiment = experiment;
			return this;
		}

	}

	private NIOExperimentSpaceReport(Data data) {
		this.data = data;
	}

	@Override
	public void openSimulation(int scenarioId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {

		final List<String> lines = new ArrayList<>();

		/*
		 * Build the header line
		 */
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("Scenario");
		for (int i = 0; i < data.experiment.getExperimentFieldCount(); i++) {
			sb.append("\t");
			sb.append(data.experiment.getExperimentFieldName(i));
		}
		lines.add(sb.toString());

		/*
		 * Build the scenario lines
		 */

		int scenarioCount = data.experiment.getScenarioCount();
		for (int i = 0; i < scenarioCount; i++) {
			final int scenarioId = i;
			sb = new StringBuilder();
			sb.append(scenarioId);

			for (int j = 0; j < data.experiment.getExperimentFieldCount(); j++) {
				sb.append("\t");
				final Object experimentFieldValue = data.experiment.getExperimentFieldValue(scenarioId, j);
				sb.append(experimentFieldValue);
			}
			lines.add(sb.toString());
		}

		try {
			Files.write(data.experimentColumnReportPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void closeSimulation(int scenarioId) {
		// do nothing

	}

	@Override
	public void closeExperiment() {
		// do nothing

	}

	@Override
	public void handle(int scenarioId, Object output) {
		// do nothing

	}

	@Override
	public Set<Class<?>> getHandledClasses() {
		return new LinkedHashSet<>();
	}

}
