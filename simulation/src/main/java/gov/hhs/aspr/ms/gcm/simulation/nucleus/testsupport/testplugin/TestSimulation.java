package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A Testing utility class that will execute a simulation given a list of
 * plugins and an outputConsumer
 */
public class TestSimulation {

	private static class Data {
		private Double simulationHaltTime;
		private boolean produceSimulationStateOnHalt;
		private List<Plugin> plugins = new ArrayList<>();
		private TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		private SimulationState simulationState = SimulationState.builder().build();
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			simulationHaltTime = data.simulationHaltTime;
			produceSimulationStateOnHalt = data.produceSimulationStateOnHalt;
			plugins.addAll(data.plugins);
			testOutputConsumer = data.testOutputConsumer;
			simulationState = data.simulationState;
			locked = data.locked;
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder {

		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * If true and an output consumer is also assigned then the simulation will
		 * produce plugins and a SimulationTime that reflect the final state of the
		 * simulation. Defaults to false.
		 */
		public Builder setProduceSimulationStateOnHalt(boolean produceSimulationStateOnHalt) {
			ensureDataMutability();
			data.produceSimulationStateOnHalt = produceSimulationStateOnHalt;
			return this;
		}

		/**
		 * Set the simulation halt time.
		 */
		public Builder setSimulationHaltTime(double simulationHaltTime) {
			ensureDataMutability();
			data.simulationHaltTime = simulationHaltTime;
			return this;
		}

		/**
		 * Set the simulation time. Defaults to the current date and a start time of
		 * zero.
		 * 
		 * @throws ContractException {@link NucleusError#NULL_SIMULATION_TIME} if the
		 *                           simulation time is null
		 */
		public Builder setSimulationState(SimulationState simulationState) {
			ensureDataMutability();
			if (simulationState == null) {
				throw new ContractException(NucleusError.NULL_SIMULATION_TIME);
			}
			data.simulationState = simulationState;
			return this;
		}

		/**
		 * Adds a plugin to this builder for inclusion in the test simulation
		 * 
		 * @throws ContractException {@link NucleusError#NULL_PLUGINS} if the plugin
		 *                           collection is null
		 */
		public Builder addPlugin(Plugin plugin) {
			ensureDataMutability();
			if (plugin == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN);
			}
			data.plugins.add(plugin);
			return this;
		}

		/**
		 * Add a plugin initializer to this builder for inclusion in the simulation
		 * 
		 * @throws ContractException {@link NucleusError#NULL_PLUGIN} if the plugin is
		 *                           null
		 */
		public Builder addPlugins(Collection<Plugin> plugins) {
			ensureDataMutability();
			if (plugins == null) {
				throw new ContractException(NucleusError.NULL_PLUGINS);
			}
			for (Plugin plugin : plugins) {
				if (plugin == null) {
					throw new ContractException(NucleusError.NULL_PLUGIN);
				}
				data.plugins.add(plugin);
			}
			return this;
		}

		/**
		 * Returns an Engine instance that is initialized with the plugins and output
		 * consumer collected by this builder.
		 */
		public TestSimulation build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new TestSimulation(data);
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

	private final Data data;

	private TestSimulation(Data data) {
		this.data = data;
	}

	public TestOutputConsumer execute() {
		Simulation.Builder builder = Simulation.builder();

		for (Plugin plugin : data.plugins) {
			builder.addPlugin(plugin);
		}

		// build and execute the engine
		builder//
				.setRecordState(data.produceSimulationStateOnHalt)//
				.setOutputConsumer(data.testOutputConsumer)//
				.setSimulationState(data.simulationState)//
				.setSimulationHaltTime(data.simulationHaltTime)//
				.build().execute();

		// show that all actions were executed
		Map<TestScenarioReport, Integer> outputItems = data.testOutputConsumer
				.getOutputItemMap(TestScenarioReport.class);
		boolean complete = false;

		if (outputItems.size() < 1) {
			throw new ContractException(TestError.MISSING_TEST_SCENARIO_REPORTS);
		}

		if (outputItems.size() > 1) {
			throw new ContractException(TestError.DUPLICATE_TEST_SCENARIO_REPORTS);
		}

		TestScenarioReport testScenarioReport = outputItems.keySet().iterator().next();
		Integer count = outputItems.get(testScenarioReport);
		if (count > 1) {
			throw new ContractException(TestError.DUPLICATE_TEST_SCENARIO_REPORTS);
		}
		complete = testScenarioReport.isComplete();

		if (!complete) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
		return data.testOutputConsumer;
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}
}
