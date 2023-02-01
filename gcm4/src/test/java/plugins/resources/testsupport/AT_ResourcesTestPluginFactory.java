package plugins.resources.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.ResourcesPluginData;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_ResourcesTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			// show that there are 100 people
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertEquals(100, peopleDataManager.getPopulationCount());

			// show that time tracking policy
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			// show that there are regions
			assertTrue(!regionsDataManager.getRegionIds().isEmpty());

			// show that each region has a person
			for (RegionId regionId : regionsDataManager.getRegionIds()) {
				assertTrue(!regionsDataManager.getPeopleInRegion(regionId).isEmpty());
			}

			// TODO: add check for resources

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(ResourcesTestPluginFactory
				.factory(100, 5785172948650781925L, factoryConsumer(executed))
				.getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(ResourcesTestPluginFactory
				.factory(100, 5785172948650781925L, testPluginData)
				.getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(5, ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData) {
		Class<?> classRef = expectedPluginData.getClass();
		plugins.forEach((plugin) -> {
			Set<PluginData> pluginDatas = plugin.getPluginDatas();
			if (pluginDatas.size() > 0) {
				PluginData pluginData = pluginDatas.toArray(new PluginData[0])[0];
				if (classRef.isAssignableFrom(pluginData.getClass())) {
					assertEquals(expectedPluginData, classRef.cast(pluginData));
				} else {
					assertNotEquals(expectedPluginData, pluginData);
				}
			}
		});
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = ResourcesTestPluginFactory
				.factory(0, 0, t -> {
				})
				.setPeoplePluginData(peoplePluginData)
				.getPlugins();

		checkPlugins(plugins, peoplePluginData);
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
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

		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPlugins(plugins, regionsPluginData);

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = {
			ResourcesPluginData.class })
	public void testSetResourcesPluginData() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		// TODO: add stuff to builder

		ResourcesPluginData resourcesPluginData = builder.build();

		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).setResourcesPluginData(resourcesPluginData).getPlugins();

		checkPlugins(plugins, resourcesPluginData);

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2990359774692004249L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		checkPlugins(plugins, stochasticsPluginData);
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
			List.class, long.class })
	public void testGetStandardRegionsPluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}
		RegionsPluginData regionsPluginData = ResourcesTestPluginFactory.getStandardRegionsPluginData(people,
				4570318399157617579L);
		assertNotNull(regionsPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		PeoplePluginData peoplePluginData = ResourcesTestPluginFactory.getStandardPeoplePluginData(100);
		assertNotNull(peoplePluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardResourcesPluginData", args = {
			long.class })
	public void testGetStandardResourcesPluginData() {

		ResourcesPluginData resourcesPluginData = ResourcesTestPluginFactory
				.getStandardResourcesPluginData(4800551796983227153L);
		assertNotNull(resourcesPluginData);

		// TODO: add additional checks
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = ResourcesTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}