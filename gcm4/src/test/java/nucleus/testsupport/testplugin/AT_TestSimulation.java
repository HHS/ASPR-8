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
	public void testExecuteSimulation_Plugins() {
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

		// precondition: if the simulation does not complete successfully
		testPluginData = TestPluginData	.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, c -> c.halt()))//
										.addTestActorPlan("actor", new TestActorPlan(1, c -> {
										}))//
										.build();
		List<Plugin> plugins2 = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));

		contractException = assertThrows(ContractException.class, () -> {
			TestSimulation.executeSimulation(plugins2);
		});
		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { Plugin.class })
	public void testExecuteSimulation_Plugin() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		assertDoesNotThrow(() -> TestSimulation.executeSimulation(testPlugin));
		assertTrue(executed.getValue());

		// precondition: if the plugin is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Plugin nullPlugin = null;
			TestSimulation.executeSimulation(nullPlugin);
		});
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());

		// precondition: if the simulation does not complete successfully
		testPluginData = TestPluginData	.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, c -> c.halt()))//
										.addTestActorPlan("actor", new TestActorPlan(1, c -> {
										}))//
										.build();
		Plugin testPlugin2 = TestPlugin.getTestPlugin(testPluginData);
		contractException = assertThrows(ContractException.class, () -> {
			TestSimulation.executeSimulation(testPlugin2);
		});
		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { List.class, TestOutputConsumer.class })
	public void testExecuteSimulation_Plugins_OutputConsumer() {
		MutableBoolean executed = new MutableBoolean();
		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
		List<Plugin> plugins = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));
		assertDoesNotThrow(() -> TestSimulation.executeSimulation(plugins, outputConsumer));
		assertTrue(executed.getValue());

		// precondition: list of plugins is null
		List<Plugin> nullPluginList = null;
		ContractException contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(nullPluginList, outputConsumer));
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

		// precondition: if the simulation does not complete successfully
		testPluginData = TestPluginData	.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, c -> c.halt()))//
										.addTestActorPlan("actor", new TestActorPlan(1, c -> {
										}))//
										.build();
		List<Plugin> plugins2 = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));

		contractException = assertThrows(ContractException.class, () -> {
			TestSimulation.executeSimulation(plugins2);
		});
		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());

	}
	
	@Test
	@UnitTestMethod(target = TestSimulation.class, name = "executeSimulation", args = { Plugin.class, TestOutputConsumer.class })
	public void testExecuteSimulation_Plugin_OutputConsumer() {
		MutableBoolean executed = new MutableBoolean();
		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		TestPluginData testPluginData = TestPluginData.builder().addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true))).build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);		
		assertDoesNotThrow(() -> TestSimulation.executeSimulation(testPlugin, outputConsumer));
		assertTrue(executed.getValue());

		// precondition: if the plugin is null
		Plugin nullPlugin = null;
		ContractException contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(nullPlugin, outputConsumer));
		assertEquals(NucleusError.NULL_PLUGIN, contractException.getErrorType());
		
		// precondition: output consumer is null
		contractException = assertThrows(ContractException.class, () -> TestSimulation.executeSimulation(testPlugin, null));
		assertEquals(NucleusError.NULL_OUTPUT_HANDLER, contractException.getErrorType());

		// precondition: if the simulation does not complete successfully
		TestOutputConsumer outputConsumer2 = new TestOutputConsumer();
		testPluginData = TestPluginData	.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, c -> c.halt()))//
										.addTestActorPlan("actor", new TestActorPlan(1, c -> {
										}))//
										.build();
		List<Plugin> plugins2 = Arrays.asList(TestPlugin.getTestPlugin(testPluginData));

		contractException = assertThrows(ContractException.class, () -> {
			TestSimulation.executeSimulation(plugins2,outputConsumer2);
		});
		assertEquals(TestError.TEST_EXECUTION_FAILURE, contractException.getErrorType());

	}

}
