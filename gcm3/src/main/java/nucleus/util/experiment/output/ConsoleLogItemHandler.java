package nucleus.util.experiment.output;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.util.experiment.progress.ExperimentProgressLog;

@ThreadSafe
public class ConsoleLogItemHandler implements OutputItemHandler {

	@Override
	public void openSimulation(int scenarioId) {
		// do nothing

	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
		// do nothing

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
		LogItem logItem = (LogItem) output;
		StringBuilder sb = new StringBuilder();
		if (scenarioId < 0 ) {
			sb.append("[scenario = ");
			sb.append(scenarioId);			
			sb.append("] ");
		}
		sb.append(logItem.getLogStatus());
		sb.append(": ");
		sb.append(logItem.getMessage());
		String message = sb.toString();

		if (logItem.getLogStatus() == LogStatus.ERROR) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}

	@Override
	public Set<Class<?>> getHandledClasses() {
		Set<Class<?>> result = new LinkedHashSet<>();
		result.add(LogItem.class);
		return result;
	}

}
