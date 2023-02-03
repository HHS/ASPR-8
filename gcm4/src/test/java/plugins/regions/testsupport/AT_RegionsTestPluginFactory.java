package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.RegionsPluginId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_RegionsTestPluginFactory {

	/**
	 * Convience method to create a consumer to facilitate testing the factory
	 * methods
	 * {@link AT_RegionsTestPluginFactory#testFactory_Consumer()}
	 * and
	 * {@link AT_RegionsTestPluginFactory#testFactory_TestPluginData()}
	 * 
	 * <li>either for passing directly to
	 * <li>{@link RegionsTestPluginFactory#factory(long, Consumer)}
	 * <li>or indirectly via creating a TestPluginData and passing it to
	 * <li>{@link RegionsTestPluginFactory#factory(long, TestPluginData)}
	 * 
	 * @param executed boolean to set once the consumer completes
	 * @return the consumer
	 * 
	 */
	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			// show that there are 100 people
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(100, peopleDataManager.getPopulationCount());

			// show that time tracking policy
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			assertEquals(TimeTrackingPolicy.TRACK_TIME, regionsDataManager.getPersonRegionArrivalTrackingPolicy());

			// show that there are regions
			assertTrue(!regionsDataManager.getRegionIds().isEmpty());

			// show that each region has a person
			for (RegionId regionId : regionsDataManager.getRegionIds()) {
				assertTrue(!regionsDataManager.getPeopleInRegion(regionId).isEmpty());
			}

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TimeTrackingPolicy.class, Consumer.class }, tags = { UnitTag.INCOMPLETE })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(RegionsTestPluginFactory
				.factory(100, 5785172948650781925L, TimeTrackingPolicy.TRACK_TIME, factoryConsumer(executed))
				.getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TimeTrackingPolicy.class, TestPluginData.class }, tags = { UnitTag.INCOMPLETE })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(RegionsTestPluginFactory
				.factory(100, 5166994853007999229L, TimeTrackingPolicy.TRACK_TIME, testPluginData).getPlugins());

	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(4, RegionsTestPluginFactory.factory(0, 0, TimeTrackingPolicy.TRACK_TIME, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData, PluginId pluginId) {
		Plugin actualPlugin = null;
		for(Plugin plugin : plugins) {
			if(plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}

		assertNotNull(actualPlugin);
		Set<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
		assertNotNull(actualPluginDatas);
		assertEquals(1, actualPluginDatas.size());
		PluginData actualPluginData = actualPluginDatas.stream().toList().get(0);
		assertTrue(expectedPluginData == actualPluginData);
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = RegionsTestPluginFactory
				.factory(0, 0, TimeTrackingPolicy.TRACK_TIME, t -> {
				})
				.setPeoplePluginData(peoplePluginData)
				.getPlugins();

		checkPlugins(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
			RegionsPluginData.class })
	public void testSetRegionsPluginData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6932994470470639085L);
		int initialPopulation = 30;
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the region plugin
		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId,
					testRegionPropertyId.getPropertyDefinition());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
						|| randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, randomPropertyValue);
				}
			}
		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, TimeTrackingPolicy.TRACK_TIME, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPlugins(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(1286485118818778304L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = RegionsTestPluginFactory
				.factory(0, 0, TimeTrackingPolicy.TRACK_TIME, t -> {
				})
				.setStochasticsPluginData(stochasticsPluginData)
				.getPlugins();

		checkPlugins(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = RegionsTestPluginFactory.getStandardPeoplePluginData(100);

		assertEquals(100, peoplePluginData.getPersonIds().size());

		for (PersonId personId : people) {
			assertTrue(peoplePluginData.getPersonIds().contains(personId));
		}
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
			List.class, TimeTrackingPolicy.class, long.class })
	public void testGetStandardRegionsPluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		RegionsPluginData regionsPluginData = RegionsTestPluginFactory.getStandardRegionsPluginData(people,
				TimeTrackingPolicy.TRACK_TIME,
				6178540698301704248L);
		assertNotNull(regionsPluginData);

		assertEquals(100, regionsPluginData.getPersonCount());
		assertEquals(TimeTrackingPolicy.TRACK_TIME, regionsPluginData.getPersonRegionArrivalTrackingPolicy());

		for (PersonId personId : people) {
			assertTrue(regionsPluginData.getPersonRegion(personId).isPresent());
		}
	}

	@Test
	@UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 8184805053177550601L;
		StochasticsPluginData stochasticsPluginData = RegionsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}
