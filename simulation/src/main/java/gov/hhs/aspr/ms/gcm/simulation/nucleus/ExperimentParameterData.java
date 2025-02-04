package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An experiment provides a means for executing the simulation over variants of
 * plugin data. Each such variant is referred to as a scenario. The scenarios
 * correspond to the cross product of a finite number of dimensions, with each
 * dimension having a finite number of variant levels. For example: An
 * experiment contains several plugins and correspondingly several plugin data
 * objects. The experiment has two dimensions. The first dimension varies one of
 * the plugin data objects with 5 new values. The second dimension varies two
 * values in two separate plugin data objects with 3 new values each. There will
 * be 15 resulting scenarios numbered 0 to 14 corresponding to each combination
 * of altered inputs. The experiment then executes the scenarios concurrently
 * based on the number of threads chosen for the execution.
 */
public final class ExperimentParameterData {

	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Marks the scenario to be explicitly run. All other scenarios will be ignored.
		 */
		public Builder addExplicitScenarioId(Integer scenarioId) {
			ensureDataMutability();
			data.explicitScenarioIds.add(scenarioId);
			return this;
		}

		/**
		 * Builds an experiment from the collected plugins, dimensions and output
		 * handlers.
		 */
		public ExperimentParameterData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ExperimentParameterData(data);
		}

		/**
		 * Sets the path for experiment progress log. A null path turns off logging and
		 * run resumption. Default value is null.
		 */
		public Builder setExperimentProgressLog(final Path path) {
			ensureDataMutability();
			data.experimentProgressLogPath = path;
			return this;
		}

		/**
		 * Instructs the experiment to continue experiment progress from the experiment
		 * progress log. Defaults to false;
		 */
		public Builder setContinueFromProgressLog(boolean continueFromProgressLog) {
			ensureDataMutability();
			data.continueFromProgressLog = continueFromProgressLog;
			return this;
		}

		/**
		 * Sets the number of scenarios that may run concurrently. Generally this should
		 * be set to one less than the number of virtual processors on the machine that
		 * is running the experiment. Setting the thread count to zero causes the
		 * simulations to execute in the main thread.
		 *
		 * @throws ContractException {@linkplain NucleusError#NEGATIVE_THREAD_COUNT} if
		 *                           the thread count is negative
		 */
		public Builder setThreadCount(final int threadCount) {
			ensureDataMutability();
			if (threadCount < 0) {
				throw new ContractException(NucleusError.NEGATIVE_THREAD_COUNT);
			}
			data.threadCount = threadCount;
			return this;
		}

		/**
		 * Signals to simulation components to record their state as plugin data as
		 * output to the experiment Defaults to false.
		 */
		public Builder setRecordState(boolean recordState) {
			ensureDataMutability();
			data.stateRecordingIsScheduled = recordState;
			return this;
		}

		/**
		 * Sets the halt time for the simulation. Defaults to -1, which is equivalent to
		 * not halting. If the simulation has been instructed to produce its state at
		 * halt, then the halt time must be set to a positive value. Setting this to a
		 * non-negative value that is less than the simulation time used to start the
		 * simulation will result in an exception.
		 */
		public Builder setSimulationHaltTime(Double simulationHaltTime) {
			ensureDataMutability();
			data.simulationHaltTime = simulationHaltTime;
			return this;
		}

		/**
		 * When true, the experiment halts on any exception thrown by any of the
		 * simulation instances. The experiment will attempt to gracefully terminate,
		 * halting any ongoing simulation instances and completing the experiment. When
		 * false, the experiment logs the failure with the experiment context and
		 * continues with the remaining simulation instances. Defaulted to true.
		 */
		public Builder setHaltOnException(final boolean haltOnException) {
			ensureDataMutability();
			data.haltOnException = haltOnException;
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
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
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			threadCount = data.threadCount;
			stateRecordingIsScheduled = data.stateRecordingIsScheduled;
			simulationHaltTime = data.simulationHaltTime;
			haltOnException = data.haltOnException;
			experimentProgressLogPath = data.experimentProgressLogPath;
			continueFromProgressLog = data.continueFromProgressLog;
			explicitScenarioIds.addAll(data.explicitScenarioIds);
			locked = data.locked;
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
		return new Builder(new Data());
	}

	private final Data data;

	private ExperimentParameterData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the number of threads that the experiment should use to execute the
	 * scenarios. A thread count of zero indicates that the scenarios should be
	 * executed in the main thread.
	 */
	public int getThreadCount() {
		return data.threadCount;
	}

	/**
	 * Signals to the simulation components to record their state as plugin data as
	 * output to the experiment.
	 */
	public boolean stateRecordingIsScheduled() {
		return data.stateRecordingIsScheduled;
	}

	/**
	 * Return the halt time for the simulation. Returns empty optional to indicate
	 * that no halting time is specified.
	 */
	public Optional<Double> getSimulationHaltTime() {
		return Optional.ofNullable(data.simulationHaltTime);
	}

	/**
	 * When true, the experiment halts on any exception thrown by any of the
	 * simulation instances. The experiment will attempt to gracefully terminate,
	 * halting any ongoing simulation instances and completing the experiment. When
	 * false, the experiment logs the failure with the experiment context and
	 * continues with the remaining simulation instances.
	 */
	public boolean haltOnException() {
		return data.haltOnException;
	}

	/**
	 * Returns the optional path for experiment progress log. An empty optional path
	 * turns off logging and run resumption.
	 */
	public Optional<Path> getExperimentProgressLogPath() {
		return Optional.ofNullable(data.experimentProgressLogPath);
	}

	/**
	 * Instructs the experiment to continue experiment progress from the experiment
	 * progress log.
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

	/**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
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

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
