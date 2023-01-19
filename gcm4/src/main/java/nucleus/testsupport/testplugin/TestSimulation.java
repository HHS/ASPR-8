package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import util.errors.ContractException;

public class TestSimulation {
	public static void executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
		if (outputConsumer == null) {
			throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER, "Output consumer was not set. Either set it or call the other version of this method that doesn't take a outputConsumer as a parameter.");
		}
		_executeSimulation(pluginsToAdd, outputConsumer);
	}

	public static void executeSimulation(List<Plugin> pluginsToAdd) {
		_executeSimulation(pluginsToAdd, new TestSimulationOutputConsumer());
	}

	private static void _executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
		Builder builder = Simulation.builder();

		for (Plugin plugin : pluginsToAdd) {
			builder.addPlugin(plugin);
		}

		// build and execute the engine
		builder.setOutputConsumer(outputConsumer)
				.build()
				.execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
