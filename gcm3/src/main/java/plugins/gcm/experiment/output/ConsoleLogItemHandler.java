package plugins.gcm.experiment.output;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;
import plugins.gcm.experiment.progress.ExperimentProgressLog;

@ThreadSafe
public class ConsoleLogItemHandler implements OutputItemHandler {

	@Override
	public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing

	}

	@Override
	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
		// do nothing

	}

	@Override
	public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
		// do nothing

	}

	@Override
	public void closeExperiment() {
		// do nothing

	}

	@Override
	public void handle(ScenarioId scenarioId, ReplicationId replicationId, Object output) {
		LogItem logItem = (LogItem) output;
		StringBuilder sb = new StringBuilder();
		if (scenarioId.getValue() < 0 || replicationId.getValue() < 0) {
			sb.append("[scenario = ");
			sb.append(scenarioId.getValue());
			sb.append(", replication = ");
			sb.append(replicationId.getValue());
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
