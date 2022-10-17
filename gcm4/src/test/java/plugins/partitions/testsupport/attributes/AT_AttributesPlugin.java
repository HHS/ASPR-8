package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginData.Builder;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = AttributesPlugin.class)
public class AT_AttributesPlugin {

	@Test
	@UnitTestMethod(name = "getAttributesPlugin", args = { AttributesPluginData.class })
	public void testGetAttributesPlugin() {

		/*
		 * Create an attributes plugin
		 */
		AttributesPluginData attributesPluginData = AttributesPluginData.builder().build();		
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesPluginData);

		// show that the plugin data is present
		Set<PluginData> pluginDatas = attributesPlugin.getPluginDatas();
		assertNotNull(pluginDatas);
		assertEquals(1, pluginDatas.size());
		assertTrue(pluginDatas.contains(attributesPluginData));

		// show that the plugin has the expected id
		assertEquals(AttributesPluginId.PLUGIN_ID, attributesPlugin.getPluginId());

		// show that the plugin has the correct dependencies
		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		Set<PluginId> actualDependencies = attributesPlugin.getPluginDependencies();
		assertEquals(expectedDependencies, actualDependencies);
		
		//show that the plugin contributed the AttributesDataManager to the simulation
		Builder testPluginDataBuilder = TestPluginData.builder();
		
		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0,(c)->{
			AttributesDataManager dataManager = c.getDataManager(AttributesDataManager.class);
			assertNotNull(dataManager);
		}));
		
		TestPluginData testPluginData = testPluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(435346454564566L).build()))//
					.addPlugin(PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build()))//
					.addPlugin(PartitionsPlugin.getPartitionsPlugin())
					.addPlugin(attributesPlugin)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//
		
		
		
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

}
