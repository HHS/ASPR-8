package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import util.errors.ContractException;

/**
 * A Testing utility class that will execute a simulation given a list of
 * plugins and an outputConsumer
 */
public class TestSimulation {

	private TestSimulation() {
	}

	/**
	 * Executes a simulation instance
	 * 
	 * @param pluginsToAdd   - a list of plugins to add to the simulation
	 * @param outputConsumer - a consumer that will handle all output from the
	 *                       simulation
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain NucleusError#NULL_OUTPUT_HANDLER}
	 *                           if outputConsumer is null</li>
	 * @throws ContractException
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd is null</li>
	 *                           <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *                           pluginsToAdd is an empty list</li>
	 *                           <li>{@linkplain TestError#TEST_EXECUTION_FAILURE}
	 *                           if the simulation does not complete
	 *                           successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
		if (outputConsumer == null) {
			throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER,
					"Output consumer was not set. Either set it or call the other version of this method that doesn't take a outputConsumer as a parameter.");
		}
		if (pluginsToAdd == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN);
		}
		if (pluginsToAdd.isEmpty()) {
			throw new ContractException(NucleusError.EMPTY_PLUGIN_LIST);
		}
		_executeSimulation(pluginsToAdd, outputConsumer);
	}

	/**
	 * Executes a simulation instance
	 * 
	 * @param pluginsToAdd - a list of plugins to add to the simulation
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd is null</li>
	 *                           <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *                           pluginsToAdd is an empty list</li>
	 *                           <li>{@linkplain TestError#TEST_EXECUTION_FAILURE}
	 *                           if the simulation does not complete
	 *                           successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd) {
		if (pluginsToAdd == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN);
		}
		if (pluginsToAdd.isEmpty()) {
			throw new ContractException(NucleusError.EMPTY_PLUGIN_LIST);
		}
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
