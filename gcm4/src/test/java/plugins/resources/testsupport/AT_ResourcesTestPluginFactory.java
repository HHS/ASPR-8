package plugins.resources.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.RegionsPluginId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.ResourcesPluginId;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_ResourcesTestPluginFactory {

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(ResourcesTestPluginFactory
				.factory(100, 5785172948650781925L, c -> executed.setValue(true))
				.getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> ResourcesTestPluginFactory.factory(0, 0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(ResourcesTestPluginFactory
				.factory(100, 5785172948650781925L, testPluginData)
				.getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> ResourcesTestPluginFactory.factory(0, 0, nullTestPluginData));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
	}

	/*
	 * Given a list of plugins, will show that the plugin with the given pluginId
	 * exists, and exists EXACTLY once.
	 */
	private Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
		Plugin actualPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getPluginId().equals(pluginId)) {
				assertNull(actualPlugin);
				actualPlugin = plugin;
			}
		}

		assertNotNull(actualPlugin);

		return actualPlugin;
	}

	/**
	 * Given a list of plugins, will show that the explicit plugindata for the given
	 * pluginid exists, and exists EXACTLY once.
	 */
	private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
			PluginId pluginId) {
		Plugin actualPlugin = checkPluginExists(plugins, pluginId);
		Set<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
		assertNotNull(actualPluginDatas);
		assertEquals(1, actualPluginDatas.size());
		PluginData actualPluginData = actualPluginDatas.stream().toList().get(0);
		assertTrue(expectedPluginData == actualPluginData);
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).getPlugins();
		assertEquals(5, plugins.size());

		checkPluginExists(plugins, ResourcesPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
		checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
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

		checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
			RegionsPluginData.class })
	public void testSetRegionsPluginData() {
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

		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}

		regionPluginBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		RegionsPluginData regionsPluginData = regionPluginBuilder.build();

		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.Factory.class, name = "setResourcesPluginData", args = {
			ResourcesPluginData.class })
	public void testSetResourcesPluginData() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		ResourcesPluginData resourcesPluginData = builder.build();

		List<Plugin> plugins = ResourcesTestPluginFactory.factory(0, 0, t -> {
		}).setResourcesPluginData(resourcesPluginData).getPlugins();

		checkPluginDataExists(plugins, resourcesPluginData, ResourcesPluginId.PLUGIN_ID);

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

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
			List.class, long.class })
	public void testGetStandardRegionsPluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}
		long seed = 4570318399157617579L;
		RegionsPluginData regionsPluginData = ResourcesTestPluginFactory.getStandardRegionsPluginData(people,
				seed);
		assertNotNull(regionsPluginData);

		Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);
		assertFalse(expectedRegionIds.isEmpty());

		Set<RegionId> actualRegionIds = regionsPluginData.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		assertEquals(people.size(), regionsPluginData.getPersonCount());
		for (PersonId person : people) {
			assertTrue(regionsPluginData.getPersonRegion(person).isPresent());
			TestRegionId expectedRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			RegionId actualRegionId = regionsPluginData.getPersonRegion(person).get();
			assertEquals(expectedRegionId, actualRegionId);
		}

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		int initialPopulation = 100;
		PeoplePluginData peoplePluginData = ResourcesTestPluginFactory.getStandardPeoplePluginData(initialPopulation);

		assertEquals(initialPopulation, peoplePluginData.getPersonIds().size());

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardResourcesPluginData", args = {
			long.class })
	public void testGetStandardResourcesPluginData() {

		long seed = 4800551796983227153L;
		ResourcesPluginData resourcesPluginData = ResourcesTestPluginFactory
				.getStandardResourcesPluginData(seed);

		Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
		assertFalse(expectedResourceIds.isEmpty());

		Set<ResourceId> actualResourceIds = resourcesPluginData.getResourceIds();
		assertEquals(expectedResourceIds, actualResourceIds);

		for (TestResourceId resourceId : TestResourceId.values()) {
			TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
			TimeTrackingPolicy actualPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
			assertEquals(expectedPolicy, actualPolicy);
		}

		Set<TestResourcePropertyId> expectedResourcePropertyIds = EnumSet.allOf(TestResourcePropertyId.class);
		assertFalse(expectedResourcePropertyIds.isEmpty());

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
			PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
			Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

			assertTrue(resourcesPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId));

			PropertyDefinition actualPropertyDefinition = resourcesPluginData
					.getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

			Object actualPropertyValue = resourcesPluginData.getResourcePropertyValue(expectedResourceId,
					resourcePropertyId);
			assertEquals(expectedPropertyValue, actualPropertyValue);
		}

	}

	@Test
	@UnitTestMethod(target = ResourcesTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = ResourcesTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
	}
}