package nucleus;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import util.errors.ContractException;

/**
 * An experiment provides a means for executing the simulation over variants of
 * plugin data. Each such variant is referred to as a scenario. The scenarios
 * correspond to the cross product of a finite number of dimensions, with each
 * dimension having a finite number of variant levels.
 * 
 * For example: An experiment contains several plugins and correspondingly
 * several plugin data objects. The experiment has two dimensions. The first
 * dimension varies one of the plugin data objects with 5 new values. The second
 * dimension varies two values in two separate plugin data objects with 3 new
 * values each. There will be 15 resulting scenarios numbered 0 to 14
 * corresponding to each combination of altered inputs.
 * 
 * The experiment then executes the scenarios concurrently based on the number
 * of threads chosen for the execution.
 *
 *
 */

public final class ExperimentParameterData {

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Marks the scenario to be explicitly run. All other scenarios will be
		 * ignored.
		 */
		public Builder addExplicitScenarioId(Integer scenarioId) {
			data.explicitScenarioIds.add(scenarioId);
			return this;
		}

		/**
		 * Builds an experiment from the collected plugins, dimensions and
		 * output handlers.
		 */
		public ExperimentParameterData build() {
			return new ExperimentParameterData(new Data(data));
		}

		/**
		 * Sets the path for experiment progress log. A null path turns off
		 * logging and run resumption. Default value is null.
		 */
		public Builder setExperimentProgressLog(final Path path) {
			data.experimentProgressLogPath = path;
			return this;
		}

		/**
		 * Instructs the experiment to continue experiment progress from the
		 * experiment progress log. Defaults to false;
		 * 
		 */
		public Builder setContinueFromProgressLog(boolean continueFromProgressLog) {
			data.continueFromProgressLog = continueFromProgressLog;
			return this;
		}

		/**
		 * Sets the number of scenarios that may run concurrently. Generally
		 * this should be set to one less than the number of virtual processors
		 * on the machine that is running the experiment. Setting the thread
		 * count to zero causes the simulations to execute in the main thread.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NEGATIVE_THREAD_COUNT} if
		 *             the thread count is negative</li>
		 *
		 * 
		 */
		public Builder setThreadCount(final int threadCount) {
			if (threadCount < 0) {
				throw new ContractException(NucleusError.NEGATIVE_THREAD_COUNT);
			}
			data.threadCount = threadCount;
			return this;
		}

		/**
		 * Signals to simulation components to record their state as plugin data
		 * as output to the experiment Defaults to false.
		 */
		public Builder setRecordState(boolean recordState) {
			data.stateRecordingIsScheduled = recordState;
			return this;
		}

		/**
		 * Sets the halt time for the simulation. Defaults to -1, which is
		 * equivalent to not halting. If the simulation has been instructed to
		 * produce its state at halt, then the halt time must be set to a
		 * positive value. Setting this to a non-negative value that is less
		 * than the simulation time used to start the simulation will result in
		 * an exception.
		 */
		public Builder setSimulationHaltTime(Double simulationHaltTime) {
			data.simulationHaltTime = simulationHaltTime;
			return this;
		}

		/**
		 * When true, the experiment halts on any exception thrown by any of the
		 * simulation instances. The experiment will attempt to gracefully
		 * terminate, halting any ongoing simulation instances and completing
		 * the experiment. When false, the experiment logs the failure with the
		 * experiment context and continues with the remaining simulation
		 * instances. Defaulted to true.
		 */
		public Builder setHaltOnException(final boolean haltOnException) {
			data.haltOnException = haltOnException;
			return this;
		}

	}

	/*
	 * A data class for holding the inputs to this builder from its client.
	 */
	private static class Data {

		private int threadCount;
		private boolean stateRecordingIsScheduled;
		private Double simulationHaltTime = null;
		private boolean haltOnException = true;
		private Path experimentProgressLogPath;
		private boolean continueFromProgressLog;
		private Set<Integer> explicitScenarioIds = new LinkedHashSet<>();

		public Data() {
		}

		public Data(Data data) {
			threadCount = data.threadCount;
			stateRecordingIsScheduled = data.stateRecordingIsScheduled;
			simulationHaltTime = data.simulationHaltTime;
			haltOnException = data.haltOnException;
			experimentProgressLogPath = data.experimentProgressLogPath;
			continueFromProgressLog = data.continueFromProgressLog;
			explicitScenarioIds.addAll(data.explicitScenarioIds);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (continueFromProgressLog ? 1231 : 1237);
			result = prime * result + ((experimentProgressLogPath == null) ? 0 : experimentProgressLogPath.hashCode());
			result = prime * result + ((explicitScenarioIds == null) ? 0 : explicitScenarioIds.hashCode());
			result = prime * result + (haltOnException ? 1231 : 1237);
			result = prime * result + ((simulationHaltTime == null) ? 0 : simulationHaltTime.hashCode());
			result = prime * result + (stateRecordingIsScheduled ? 1231 : 1237);
			result = prime * result + threadCount;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (continueFromProgressLog != other.continueFromProgressLog) {
				return false;
			}
			if (experimentProgressLogPath == null) {
				if (other.experimentProgressLogPath != null) {
					return false;
				}
			} else if (!experimentProgressLogPath.equals(other.experimentProgressLogPath)) {
				return false;
			}
			if (!explicitScenarioIds.equals(other.explicitScenarioIds)) {
				return false;
			}
			if (haltOnException != other.haltOnException) {
				return false;
			}
			if (simulationHaltTime == null) {
				if (other.simulationHaltTime != null) {
					return false;
				}
			} else if (!simulationHaltTime.equals(other.simulationHaltTime)) {
				return false;
			}
			if (stateRecordingIsScheduled != other.stateRecordingIsScheduled) {
				return false;
			}
			if (threadCount != other.threadCount) {
				return false;
			}
			return true;
		}

		

	}

	/**
	 * Returns a builder for Experiment
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final Data data;

	private ExperimentParameterData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the number of threads that the experiment should use to execute
	 * the scenarios. A thread count of zero indicates that the scenarios should
	 * be executed in the main thread.
	 */
	public int getThreadCount() {
		return data.threadCount;
	}

	/**
	 * Signals to the simulation components to record their state as plugin data
	 * as output to the experiment.
	 */
	public boolean stateRecordingIsScheduled() {
		return data.stateRecordingIsScheduled;
	}

	/**
	 * Return the halt time for the simulation. Returns empty optional to indicate that no
	 * halting time is specified.
	 */
	public Optional<Double> getSimulationHaltTime() {
		return Optional.ofNullable(data.simulationHaltTime);
	}

	/**
	 * When true, the experiment halts on any exception thrown by any of the
	 * simulation instances. The experiment will attempt to gracefully
	 * terminate, halting any ongoing simulation instances and completing the
	 * experiment. When false, the experiment logs the failure with the
	 * experiment context and continues with the remaining simulation instances.
	 */
	public boolean haltOnException() {
		return data.haltOnException;
	}

	/**
	 * Returns the optional path for experiment progress log. An empty optional
	 * path turns off logging and run resumption.
	 */
	public Optional<Path> getExperimentProgressLogPath() {
		return Optional.ofNullable(data.experimentProgressLogPath);
	}

	/**
	 * Instructs the experiment to continue experiment progress from the
	 * experiment progress log.
	 * 
	 */
	public boolean continueFromProgressLog() {
		return data.continueFromProgressLog;
	}

	/**
	 * Returns the set of scenario id to be explicitly run. If the set is empty,
	 * then all scenarios are run.
	 */
	public Set<Integer> getExplicitScenarioIds() {
		return new LinkedHashSet<>(data.explicitScenarioIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExperimentParameterData other = (ExperimentParameterData) obj;
		return Objects.equals(data, other.data);
	}

}
