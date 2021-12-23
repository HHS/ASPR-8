package plugins.gcm.experiment.progress;

import plugins.gcm.experiment.output.OutputItemHandler;

public interface ExperimentProgressLogProvider {

	public ExperimentProgressLog getExperimentProgressLog();

	public OutputItemHandler getSimulationStatusItemHandler();

}
