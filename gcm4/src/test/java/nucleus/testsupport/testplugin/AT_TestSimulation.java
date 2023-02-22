package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.Plugin;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

public class AT_TestSimulation {

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class })
	public void testExecuteSimulation() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
		List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
		assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins));
		assertTrue(executed.getValue());

		// precondition: list of plugins is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			List<Plugin> nullPluginList = null;
			TestSimulation.executeSimulation(nullPluginList);
		});
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());

		// precondition: list of plugins is empty
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(new ArrayList<>()));
		assertEquals(NucleusError.EMPTY_PLUGIN_LIST, contractException.getErrorType());

		// precondition: list of plugins is contains a null plugin
		List<Plugin> pluginList = new ArrayList<>();
		pluginList.add(null);
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(pluginList));
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class, TestSimulationOutputConsumer.class })
	public void testExecuteSimulation_OutputConsumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();
		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
		List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
		assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins, outputConsumer));
		assertTrue(executed.getValue());
		

		// precondition: list of plugins is null
		ContractException contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(null, outputConsumer));
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());

		// precondition: list of plugins is empty
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(new ArrayList<>(), outputConsumer));
		assertEquals(NucleusError.EMPTY_PLUGIN_LIST, contractException.getErrorType());

		// precondition: list of plugins is contains a null plugin
		List<Plugin> pluginList = new ArrayList<>();
		pluginList.add(null);
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(pluginList));
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());

		// precondition: output consumer is null
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(plugins, null));
		assertEquals(NucleusError.NULL_OUTPUT_HANDLER, contractException.getErrorType());
	}
}
