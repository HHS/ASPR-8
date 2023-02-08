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

public class TestSimulation {
	public static void executeSimulation(List<Plugin> pluginsToAdd, TestSimulationOutputConsumer outputConsumer) {
		if (outputConsumer == null) {
			throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER,
					"Output consumer was not set. Either set it or call the other version of this method that doesn't take a outputConsumer as a parameter.");
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
