package nucleus;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Consumer;

import net.jcip.annotations.ThreadSafe;

/**
 * A Consumer<ExperimentContext> implementation that can be used in an
 * Experiment for reporting experiment progress to the console.
 * 
 * @author Shawn Hatch
 *
 */

@ThreadSafe
public final class ExperimentStatusConsole implements Consumer<ExperimentContext> {

	// this is thread safe so long as there are no mutations
	private final StatusConsoleState statusConsoleState;

	private ExperimentStatusConsole(StatusConsoleState statusConsoleState) {
		this.statusConsoleState = statusConsoleState;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private StatusConsoleState statusConsoleState = getInitializedStatusConsoleState();

		private StatusConsoleState getInitializedStatusConsoleState() {

			/*
			 * initialize the state
			 */
			StatusConsoleState result = new StatusConsoleState();
			result.setImmediateErrorReporting(false);
			result.setReportScenarioProgress(true);
			result.setStackTraceReportLimit(100);
			result.setLastReportedCompletionPercentage(-1);

			return result;
		}

		private Builder() {

		}

		public ExperimentStatusConsole build() {
			try {
				return new ExperimentStatusConsole(statusConsoleState);
			} finally {
				statusConsoleState = getInitializedStatusConsoleState();
			}
		}

		/**
		 * Sets the immediate error reporting policy. When true, simulation
		 * exceptions will be written to the system error console with the
		 * scenario id and the corresponding stack trace. Defaulted to false.
		 */
		public Builder setImmediateErrorReporting(final boolean immediateErrorReporting) {
			statusConsoleState.setImmediateErrorReporting(immediateErrorReporting);
			return this;
		}

		/**
		 * Sets the report scenario progress policy. When true, interim progress
		 * of the scenarios are reported as 1)the number of scenarios completed,
		 * 2)the percentage of scenarios completed and 3) as a projected
		 * remaining time until experiment completion. Defaulted to true.
		 */
		public Builder setReportScenarioProgress(final boolean reportScenarioProgress) {
			statusConsoleState.setReportScenarioProgress(reportScenarioProgress);
			return this;
		}

		/**
		 * Sets the maximum number of stack traces that are printed to the
		 * console. This limit is applied independently to stack traces that are
		 * immediately reported and those that are reported in the experiment
		 * summary. Defaulted to 100.
		 */
		public Builder setStackTraceReportLimit(Integer stackTraceReportLimit) {
			statusConsoleState.setStackTraceReportLimit(stackTraceReportLimit);
			return this;
		}

	}

	/*
	 * Returns the string representation of the value with a prepended "0" for
	 * numbers less than 10
	 */
	private static String getBase60String(int value) {
		if (value < 10) {
			return "0" + Integer.toString(value);
		}
		return Integer.toString(value);
	}

	/*
	 * Returns a colon delimited string representation for the number of seconds
	 * in the form HH:MM:SS
	 */
	private static String getTimeExpression(double seconds) {
		int n = (int) Math.round(seconds);
		int h = n / 3600;
		n = n % 3600;
		int m = n / 60;
		int s = n % 60;

		return h + ":" + getBase60String(m) + ":" + getBase60String(s);
	}

	private void handleExperimentClose(ExperimentContext experimentContext) {
		String timeExpression = getTimeExpression(experimentContext.getElapsedSeconds());

		int previousProgressCount = experimentContext.getStatusCount(ScenarioStatus.PREVIOUSLY_SUCCEEDED);
		int totalSuccessCount = previousProgressCount + experimentContext.getStatusCount(ScenarioStatus.SUCCEDED);
		int experimentCount = experimentContext.getScenarioCount();
		int failCount = experimentContext.getStatusCount(ScenarioStatus.FAILED);

		PrintStream printStream = System.out;
		if (totalSuccessCount != experimentCount) {
			printStream = System.err;

		}
		String scenarioString = "scenarios";
		if (experimentCount == 1) {
			scenarioString = "scenario";
		}

		printStream.println("Experiment completion of " + experimentCount + " " + scenarioString + " in " + timeExpression + ":");
		for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
			List<Integer> scenarios = experimentContext.getScenarios(scenarioStatus);
			if (!scenarios.isEmpty()) {
				printStream.println("\t" + scenarioStatus + " : " + scenarios.size());
			}
		}

		int maxFailureCount = statusConsoleState.getStackTraceReportLimit();

		if (failCount > 0) {
			printStream.println("failed scenarios:");
			List<Integer> failedScenarios = experimentContext.getScenarios(ScenarioStatus.FAILED);
			int count = Math.min(maxFailureCount, failedScenarios.size());
			for (int i = 0; i < count; i++) {
				Integer scenarioId = failedScenarios.get(i);
				Exception e = experimentContext.getScenarioFailureCause(scenarioId).get();
				printStream.println("Sceanrio " + scenarioId + " failed with stackTrace:");
				e.printStackTrace();
			}
			if (failCount > maxFailureCount) {
				int unprintedFailureCount = failCount - maxFailureCount;
				printStream.println("..." + unprintedFailureCount + " more failed scenarios");
			}			
		}
		printStream.println("end of experiment status console");
	}

	private void handleSimulationClose(ExperimentContext experimentContext, int scenarioId) {

		int completionCount = experimentContext.getStatusCount(ScenarioStatus.SUCCEDED) + experimentContext.getStatusCount(ScenarioStatus.PREVIOUSLY_SUCCEEDED)
				+ experimentContext.getStatusCount(ScenarioStatus.FAILED);

		double completionProportion = completionCount;
		completionProportion /= experimentContext.getScenarioCount();
		completionProportion *= 100;

		int percentComplete = (int) completionProportion;

		boolean reportToConsole = false;

		if (percentComplete > statusConsoleState.getLastReportedCompletionPercentage()) {
			statusConsoleState.setLastReportedCompletionPercentage(percentComplete);
			reportToConsole = true;
		}

		reportToConsole &= statusConsoleState.isReportScenarioProgress();

		if (statusConsoleState.isImmediateErrorReporting()) {

			ScenarioStatus scenarioStatus = experimentContext.getScenarioStatus(scenarioId).get();
			if (scenarioStatus == ScenarioStatus.FAILED) {
				boolean printStackTrace = false;

				if (statusConsoleState.getImmediateStackTraceCount() < statusConsoleState.getStackTraceReportLimit()) {
					printStackTrace = true;
					statusConsoleState.incrementImmediateStackTraceCount();
				}

				if (printStackTrace) {
					Exception failureCause = experimentContext.getScenarioFailureCause(scenarioId).get();
					System.err.println("Simulation failure for scenario " + scenarioId);
					failureCause.printStackTrace();
				}
			}
		}

		if (reportToConsole) {
			int executedCountForThisRun = experimentContext.getStatusCount(ScenarioStatus.SUCCEDED) + experimentContext.getStatusCount(ScenarioStatus.FAILED);
			double averageTimePerExecution = experimentContext.getElapsedSeconds() / executedCountForThisRun;
			int remainingExecutions = experimentContext.getScenarioCount() - completionCount;
			double expectedRemainingTime = Math.round(averageTimePerExecution * remainingExecutions);
			String timeExpression = getTimeExpression(expectedRemainingTime);
			String scenarioString = "scenarios";
			if (experimentContext.getScenarioCount() == 1) {
				scenarioString = "scenario";
			}
			System.out.println(completionCount + " of " + experimentContext.getScenarioCount() + " " + scenarioString + ", " + percentComplete + "% complete. Expected experiment completion in "
					+ timeExpression);
		}

	}

	/**
	 * Initializes this ExperimentStatusConsole, which registers for simulation
	 * and experiment close events.
	 */

	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
		experimentContext.subscribeToExperimentClose(this::handleExperimentClose);
	}

}
