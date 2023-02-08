package nucleus.testsupport.testplugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginId;
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
	 * @throws ContractException
	 *                           <li>{@linkplain NucleusError#NULL_OUTPUT_HANDLER}
	 *                           if outputConsumer is null</li>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd is null</li>
	 *                           <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *                           pluginsToAdd is an empty list</li>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd contains a null plugin</li>
	 *                           <li>{@linkplain TestError#TEST_EXECUTION_FAILURE}
	 *                           if the simulation does not complete
	 *                           successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
		_executeSimulation(pluginsToAdd, outputConsumer);
	}

	/**
	 * Executes a simulation instance
	 *  
	 * @throws ContractException
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd is null</li>
	 *                           <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *                           pluginsToAdd is an empty list</li>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN} if
	 *                           pluginsToAdd contains a null plugin</li>
	 *                           <li>{@linkplain TestError#TEST_EXECUTION_FAILURE}
	 *                           if the simulation does not complete
	 *                           successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd) {
		_executeSimulation(pluginsToAdd, new TestSimulationOutputConsumer());
	}

	private static void _executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
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
		for (Plugin plugin : pluginsToAdd) {
			if (plugin == null) {
				throw new ContractException(NucleusError.NULL_PLUGIN);
			}
		}
		Builder builder = Simulation.builder();

		for (Plugin plugin : pluginsToAdd) {
			builder.addPlugin(plugin);
		}

		// build and execute the engine
		builder.setOutputConsumer(outputConsumer).build().execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

	public static void comparePluginLists(List<Plugin> expectedPlugins, List<Plugin> actualPlugins) {
		if (expectedPlugins == null) {
			throw new RuntimeException("expected plugins list is null");
		}
		if (actualPlugins == null) {
			throw new RuntimeException("actual plugins list is null");
		}

		Map<PluginId, Plugin> expectedMap = new LinkedHashMap<>();
		for (Plugin plugin : expectedPlugins) {
			if (plugin == null) {
				throw new RuntimeException("expected plugins contains a null plugin");
			}
			Plugin replacedPlugin = expectedMap.put(plugin.getPluginId(), plugin);
			if (replacedPlugin != null) {
				throw new RuntimeException("expected plugins contains a duplicate plugin for " + plugin.getPluginId());
			}
		}

		Map<PluginId, Plugin> actualMap = new LinkedHashMap<>();
		for (Plugin plugin : actualPlugins) {
			if (plugin == null) {
				throw new RuntimeException("actual plugins contains a null plugin");
			}
			Plugin replacedPlugin = actualMap.put(plugin.getPluginId(), plugin);
			if (replacedPlugin != null) {
				throw new RuntimeException("actual plugins contains a duplicate plugin for " + plugin.getPluginId());
			}
		}

		if (!expectedMap.keySet().equals(actualMap.keySet())) {
			throw new RuntimeException("expected and actual plugins do not contain the same plugin ids ");
		}
		for (PluginId pluginId : expectedMap.keySet()) {
			Plugin expectedPlugin = expectedMap.get(pluginId);
			Plugin actualPlugin = actualMap.get(pluginId);
			if (!expectedPlugin.equals(actualPlugin)) {
				throw new RuntimeException("Plugin equality failure for " + pluginId);
			}
		}
	}
}
