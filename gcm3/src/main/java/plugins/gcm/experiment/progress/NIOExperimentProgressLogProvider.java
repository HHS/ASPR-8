package plugins.gcm.experiment.progress;

import java.nio.file.Path;

import net.jcip.annotations.ThreadSafe;
import plugins.gcm.experiment.output.OutputItemHandler;

@ThreadSafe
public final class NIOExperimentProgressLogProvider implements ExperimentProgressLogProvider {

	private final ExperimentProgressLog experimentProgressLog;
	private final NIOExperimentProgressLogWriter nioExperimentProgressLogWriter;

	/**
	 * Constructs this provider with the given path for experiment progress log.
	 * 
	 * 
	 * 
	 */

	public NIOExperimentProgressLogProvider(Path experimentProgressLogPath) {
		experimentProgressLog = NIOExperimentProgressLogReader.read(experimentProgressLogPath);
		nioExperimentProgressLogWriter = new NIOExperimentProgressLogWriter(experimentProgressLogPath);
	}

	@Override
	public ExperimentProgressLog getExperimentProgressLog() {
		return experimentProgressLog;
	}

	@Override
	public OutputItemHandler getSimulationStatusItemHandler() {
		return nioExperimentProgressLogWriter;
	}

}
