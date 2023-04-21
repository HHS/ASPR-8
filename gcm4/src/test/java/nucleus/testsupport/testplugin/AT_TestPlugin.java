package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.Simulation;
import util.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_TestPlugin {

	@Test
	@UnitTestMethod(target = TestPlugin.class,name = "getTestPlugin", args = { TestPluginData.class })
	public void testGetTestPlugin() {
		TestPluginData testPluginData = TestPluginData.builder().build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		assertNotNull(testPlugin);

		// show that the test plugin data is returned by the plugin
		Set<PluginData> expectedPluginDatas = new LinkedHashSet<>();
		expectedPluginDatas.add(testPluginData);
		assertEquals(expectedPluginDatas, new LinkedHashSet<>(testPlugin.getPluginDatas()));

		// show that the initializer exists
		assertNotNull(testPlugin.getInitializer());

		// show that the plugin has no dependencies
		assertTrue(testPlugin.getPluginDependencies().isEmpty());

		// show that the plugin has the expected id
		assertEquals(TestPluginId.PLUGIN_ID, testPlugin.getPluginId());

		MutableBoolean dataManagerFound = new MutableBoolean();
		
		Simulation.Builder simulationBuilder = Simulation.builder();
		testPluginData = TestPluginData	.builder()//
										.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
											TestPlanDataManager dataManager = c.getDataManager(TestPlanDataManager.class);
											assertNotNull(dataManager);
											dataManagerFound.setValue(true);
										}))//
										.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);
		simulationBuilder.addPlugin(testPlugin);
		simulationBuilder.build().execute();
		
		assertTrue(dataManagerFound.getValue());
	}

}
