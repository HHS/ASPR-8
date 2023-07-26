package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData.Builder;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.partitions.PartitionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.annotations.UnitTestMethod;

public class AT_AttributesPlugin {

	@Test
	@UnitTestMethod(target = AttributesPlugin.class, name = "getAttributesPlugin", args = { AttributesPluginData.class })
	public void testGetAttributesPlugin() {

		/*
		 * Create an attributes plugin
		 */
		AttributesPluginData attributesPluginData = AttributesPluginData.builder().build();
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesPluginData);

		Plugin partitionsPlugin = PartitionsPlugin.builder()//		
				.setPartitionsPluginData(PartitionsPluginData.builder().build())//
				//.addPluginDependency(AttributesPluginId.PLUGIN_ID)//
				.getPartitionsPlugin();

		
		// show that the plugin data is present
		List<PluginData> pluginDatas = attributesPlugin.getPluginDatas();
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

		// show that the plugin contributed the AttributesDataManager to the
		// simulation
		Builder testPluginDataBuilder = TestPluginData.builder();

		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			AttributesDataManager dataManager = c.getDataManager(AttributesDataManager.class);
			assertNotNull(dataManager);
		}));

		TestPluginData testPluginData = testPluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		
		List<Plugin> plugins = new ArrayList<>();
		plugins.add(testPlugin);
		WellState wellState = WellState.builder().setSeed(435346454564566L).build();
		plugins.add(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setMainRNGState(wellState).build()));
		plugins.add(PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build()));
		plugins.add(partitionsPlugin);
		plugins.add(attributesPlugin);
		
		TestSimulation.builder().addPlugins(plugins).build().execute();
		

	}

}
