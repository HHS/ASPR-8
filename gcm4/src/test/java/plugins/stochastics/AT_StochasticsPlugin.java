package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTestMethod;

public class AT_StochasticsPlugin {

	@Test
	@UnitTestMethod(target = StochasticsPlugin.class, name = "getStochasticsPlugin", args = { StochasticsPluginData.class })
	public void testGetPlugin() {

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(34534).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		// show the plugin is not null
		assertNotNull(stochasticsPlugin);

		// show that the plugin has no dependencies
		assertTrue(stochasticsPlugin.getPluginDependencies().isEmpty());

		// show that the plugin has the correct id
		assertEquals(StochasticsPluginId.PLUGIN_ID, stochasticsPlugin.getPluginId());

		/*
		 * Show that the plugin establishes the StochasticsDataManager
		 */
		TestPluginData.Builder testPluginBuilder = TestPluginData.builder();

		TestPluginData testPluginData = testPluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.addPlugin(stochasticsPlugin)//
					.build()//
					.execute();//

	}

	// the code below should show that the state of the data manager properly
	// reflects the plugin data

	// @Test
	// @UnitTestMethod(name = "init", args = { ResolverContext.class })
	// public void testStochasticsDataViewInitialzation() {
	// long seed = 745645785689L;
	//
	// // show that the stochastics data view is published and has the correct
	// // state
	//
	// // show that we are contributing random generator ids
	// assertTrue(TestRandomGeneratorId.values().length > 0);
	//
	// // build the initial data
	// Set<TestRandomGeneratorId> expectedRandomGeneratorIds = new
	// LinkedHashSet<>();
	// StochasticsPlugin.Builder builder = StochasticsPlugin.builder();
	// for (TestRandomGeneratorId testRandomGeneratorId :
	// TestRandomGeneratorId.values()) {
	// expectedRandomGeneratorIds.add(testRandomGeneratorId);
	// builder.addRandomGeneratorId(testRandomGeneratorId);
	// }
	// builder.setSeed(seed);
	// StochasticsPlugin stochasticsPlugin = builder.build();
	//
	// List<DataView> publishedDataViews = new ArrayList<>();
	//
	// // build the manager
	// MockResolverContext mockResolverContext =
	// MockResolverContext.builder().setPublishDataViewConsumer((d) ->
	// publishedDataViews.add(d)).build();
	// StochasticsResolver stochasticsResolver = new
	// StochasticsResolver(stochasticsPlugin);
	// stochasticsResolver.init(mockResolverContext);
	//
	// // show that only one data view was published
	// assertEquals(1, publishedDataViews.size());
	//
	// // show that the published data view is not null
	// DataView dataView = publishedDataViews.get(0);
	// assertNotNull(dataView);
	//
	// // show that the published data view is a StochasticsDataView
	// assertEquals(StochasticsDataView.class, dataView.getClass());
	//
	// StochasticsDataView stochasticsDataView = (StochasticsDataView) dataView;
	//
	// // show that the data view returns the correct random generator
	// RandomGenerator randomGenerator =
	// stochasticsDataView.getRandomGenerator();
	// // show that the random generator is not null
	// assertNotNull(randomGenerator);
	// // show that the random generator is the expected implementor
	// assertEquals(Well44497b.class, randomGenerator.getClass());
	//
	// // show that the random generator is likely to have been seeded
	// // correctly
	// Well44497b well44497b = new Well44497b(seed);
	// for (int i = 0; i < 100; i++) {
	// assertEquals(well44497b.nextLong(), randomGenerator.nextLong());
	// }
	//
	// // show that the data view returns the correct random generator ids
	// Set<RandomNumberGeneratorId> actualRandomNumberGeneratorIds =
	// stochasticsDataView.getRandomNumberGeneratorIds();
	// assertEquals(expectedRandomGeneratorIds, actualRandomNumberGeneratorIds);
	//
	// // show that the random generators associated with id values are correct
	// for (TestRandomGeneratorId testRandomGeneratorId :
	// TestRandomGeneratorId.values()) {
	// // show that the data view returns the correct random generator
	// randomGenerator =
	// stochasticsDataView.getRandomGeneratorFromId(testRandomGeneratorId);
	// // show that the random generator is not null
	// assertNotNull(randomGenerator);
	// // show that the random generator is the expected implementor
	// assertEquals(Well44497b.class, randomGenerator.getClass());
	//
	// // show that the random generator is likely to have been seeded
	// // correctly
	// well44497b = new Well44497b(seed +
	// testRandomGeneratorId.toString().hashCode());
	// for (int i = 0; i < 100; i++) {
	// assertEquals(well44497b.nextLong(), randomGenerator.nextLong());
	// }
	// }
	//
	// }

}
