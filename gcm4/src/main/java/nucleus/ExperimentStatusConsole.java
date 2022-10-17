package nucleus;

import java.util.List;

import net.jcip.annotations.ThreadSafe;

/**
 * Utility class used by the Experiment for reporting the execution progress of
 * an experiment to the console.
 * 
 * @author Shawn Hatch
 *
 */

@ThreadSafe
public final class ExperimentStatusConsole {

	/*
	 * The last reported percentage completion value that includes credit from
	 * previous executions of the experiment. Guarded by this.
	 */
	private int lastReportPercentage = -1;

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
		String experimentCompletionMessage;
		if (experimentContext.getStatusCount(ScenarioStatus.PREVIOUSLY_SUCCEEDED) == 0) {
			experimentCompletionMessage = "Experiment finished with " + totalSuccessCount + " of " + experimentCount + " scenario replications successfully completed in " + timeExpression;
		} else {
			experimentCompletionMessage = "Experiment finished with " + totalSuccessCount + "(" + previousProgressCount + " from previous run(s))" + " of " + experimentCount
					+ " scenario replications successfully completed in " + timeExpression;
		}
		if (totalSuccessCount != experimentCount) {
			System.err.println(experimentCompletionMessage);
		} else {
			System.out.println(experimentCompletionMessage);
		}
		if (failCount > 0) {
			System.err.println("Failed simulations");

			List<Integer> scenarios = experimentContext.getScenarios(ScenarioStatus.FAILED);
			int count = Math.min(100, scenarios.size());
			for (int i = 0; i < count; i++) {
				System.err.println(scenarios.get(i));
			}
			if (failCount > 100) {
				System.err.println("\t...");
			}
		}
	}

	private void handleSimulationClose(ExperimentContext experimentContext, int scenarioId) {

		int completionCount = experimentContext.getStatusCount(ScenarioStatus.SUCCEDED) + experimentContext.getStatusCount(ScenarioStatus.PREVIOUSLY_SUCCEEDED)
				+ experimentContext.getStatusCount(ScenarioStatus.FAILED);

		double completionProportion = completionCount;
		completionProportion /= experimentContext.getScenarioCount();
		completionProportion *= 100;

		int percentComplete = (int) completionProportion;

		boolean reportToConsole = false;
		synchronized (this) {
			if (percentComplete > lastReportPercentage) {
				lastReportPercentage = percentComplete;
				reportToConsole = true;
			}
		}

		if (reportToConsole) {
			int executedCountForThisRun = experimentContext.getStatusCount(ScenarioStatus.SUCCEDED) + experimentContext.getStatusCount(ScenarioStatus.FAILED);
			double averageTimePerExecution = experimentContext.getElapsedSeconds() / executedCountForThisRun;
			int remainingExecutions = experimentContext.getScenarioCount() - completionCount;
			double expectedRemainingTime = Math.round(averageTimePerExecution * remainingExecutions);
			String timeExpression = getTimeExpression(expectedRemainingTime);
			System.out.println(
					completionCount + " of " + experimentContext.getScenarioCount() + " scenario replications, " + percentComplete + "% complete. Expected experiment completion in " + timeExpression);
		}

	}

	/**
	 * Initializes this ExperimentStatusConsole, which registers for simulation
	 * and experiment close events.
	 */
	public void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
		experimentContext.subscribeToExperimentClose(this::handleExperimentClose);
	}

}
