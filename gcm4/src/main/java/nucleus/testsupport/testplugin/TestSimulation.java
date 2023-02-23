package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_OUTPUT_HANDLER} if
	 *             outputConsumer is null</li>
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if pluginsToAdd is
	 *             null</li>
	 *             <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *             pluginsToAdd is an empty list</li>
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if pluginsToAdd
	 *             contains a null plugin</li>
	 *             <li>{@linkplain TestError#TEST_EXECUTION_FAILURE} if the
	 *             simulation does not complete successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd, TestOutputConsumer outputConsumer) {
		if (outputConsumer == null) {
			throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER,
					"Output consumer was not set. Either set it or call the other version of this method that doesn't take a outputConsumer as a parameter.");
		}
		_executeSimulation(pluginsToAdd, outputConsumer);
	}

	/**
	 * Executes a simulation instance
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_OUTPUT_HANDLER} if
	 *             outputConsumer is null</li>            
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if  the plugin is null</li>
	 *             <li>{@linkplain TestError#TEST_EXECUTION_FAILURE} if the
	 *             simulation does not complete successfully</li>
	 */
	public static void executeSimulation(Plugin plugin, TestOutputConsumer outputConsumer) {
		if (outputConsumer == null) {
			throw new ContractException(NucleusError.NULL_OUTPUT_HANDLER,
					"Output consumer was not set. Either set it or call the other version of this method that doesn't take a outputConsumer as a parameter.");
		}
		
		List<Plugin> pluginsToAdd = new ArrayList<>();
		pluginsToAdd.add(plugin);
		
		_executeSimulation(pluginsToAdd, outputConsumer);
	}
	
	/**
	 * Executes a simulation instance
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if pluginsToAdd is
	 *             null</li>
	 *             <li>{@linkplain NucleusError#EMPTY_PLUGIN_LIST} if
	 *             pluginsToAdd is an empty list</li>
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if pluginsToAdd
	 *             contains a null plugin</li>
	 *             <li>{@linkplain TestError#TEST_EXECUTION_FAILURE} if the
	 *             simulation does not complete successfully</li>
	 */
	public static void executeSimulation(List<Plugin> pluginsToAdd) {
		_executeSimulation(pluginsToAdd, new TestOutputConsumer());
	}

	/**
	 * Executes a simulation instance
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN} if the plugin is
	 *             null</li>
	 *             <li>{@linkplain TestError#TEST_EXECUTION_FAILURE} if the
	 *             simulation does not complete successfully</li>
	 */
	public static void executeSimulation(Plugin plugin) {
		List<Plugin> pluginsToAdd = new ArrayList<>();
		pluginsToAdd.add(plugin);
		_executeSimulation(pluginsToAdd, new TestOutputConsumer());
	}

	private static void _executeSimulation(List<Plugin> pluginsToAdd, TestOutputConsumer outputConsumer) {
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
		Map<TestScenarioReport, Integer> outputItems = outputConsumer.getOutputItems(TestScenarioReport.class);
		boolean complete = false;

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
	}
}
