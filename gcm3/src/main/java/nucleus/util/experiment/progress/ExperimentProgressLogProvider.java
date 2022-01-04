package nucleus.util.experiment.progress;

import nucleus.util.experiment.output.OutputItemHandler;

public interface ExperimentProgressLogProvider {

	public ExperimentProgressLog getExperimentProgressLog();

	public OutputItemHandler getSimulationStatusItemHandler();

}
