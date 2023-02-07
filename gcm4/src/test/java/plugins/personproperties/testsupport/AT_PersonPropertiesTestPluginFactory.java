package plugins.personproperties.testsupport;

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
import org.apache.commons.math3.util.Pair;
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
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.PersonPropertiesPluginId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.RegionsPluginData;
import plugins.regions.RegionsPluginId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_PersonPropertiesTestPluginFactory {

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(PersonPropertiesTestPluginFactory
				.factory(100, 4135374341935235561L, c -> executed.setValue(true)).getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> PersonPropertiesTestPluginFactory.factory(0, 0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				PersonPropertiesTestPluginFactory.factory(100, 92376779979686632L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> PersonPropertiesTestPluginFactory.factory(0, 0, nullTestPluginData));
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
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
		}).getPlugins();
		assertEquals(5, plugins.size());

		checkPluginExists(plugins, PersonPropertiesPluginId.PLUGIN_ID);
		checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPersonPropertiesPluginData", args = {
			PersonPropertiesPluginData.class })
	public void testSetPersonPropertiesPluginData() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2764277826547948301L);
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition());
		}
		for (PersonId personId : people) {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean doesNotHaveDefaultValue = testPersonPropertyId.getPropertyDefinition().getDefaultValue()
						.isEmpty();
				if (doesNotHaveDefaultValue || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}

		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		List<Plugin> plugins = PersonPropertiesTestPluginFactory
				.factory(0, 0, t -> {
				})
				.setPersonPropertiesPluginData(personPropertiesPluginData)
				.getPlugins();

		checkPluginDataExists(plugins, personPropertiesPluginData, PersonPropertiesPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = PersonPropertiesTestPluginFactory
				.factory(0, 0, t -> {
				})
				.setPeoplePluginData(peoplePluginData)
				.getPlugins();

		checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
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

		List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(2758378374654665699L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = PersonPropertiesTestPluginFactory
				.factory(0, 0, t -> {
				})
				.setStochasticsPluginData(stochasticsPluginData)
				.getPlugins();

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPersonPropertiesPluginData", args = {
			List.class, long.class })
	public void testGetStandardPersonPropertiesPluginData() {

		long seed = 4684903523797799712L;
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesTestPluginFactory
				.getStandardPersonPropertiesPluginData(people, seed);

		Set<TestPersonPropertyId> expectedPersonPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
		assertFalse(expectedPersonPropertyIds.isEmpty());

		Set<PersonPropertyId> actualPersonPropertyIds = personPropertiesPluginData.getPersonPropertyIds();
		assertEquals(expectedPersonPropertyIds, actualPersonPropertyIds);

		for (TestPersonPropertyId expecetedPropertyId : expectedPersonPropertyIds) {
			PropertyDefinition expectedPropertyDefinition = expecetedPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = personPropertiesPluginData
					.getPersonPropertyDefinition(expecetedPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		for (PersonId personId : people) {
			List<Pair<TestPersonPropertyId, Object>> expectedValues = new ArrayList<>();
			for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
				if (propertyId.getPropertyDefinition().getDefaultValue().isEmpty() || randomGenerator.nextBoolean()) {
					Object expectedPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
					expectedValues.add(new Pair<>(propertyId, expectedPropertyValue));
				}
			}
			List<PersonPropertyInitialization> propInitList = personPropertiesPluginData
					.getPropertyValues(personId.getValue());

			assertEquals(expectedValues.size(), propInitList.size());
			for (int i = 0; i < propInitList.size(); i++) {
				TestPersonPropertyId expectedPersonPropertyId = expectedValues.get(i).getFirst();
				Object expectedValue = expectedValues.get(i).getSecond();

				PersonPropertyId actualPropertyId = propInitList.get(i).getPersonPropertyId();
				Object actualValue = propInitList.get(i).getValue();

				assertEquals(expectedPersonPropertyId, actualPropertyId);
				assertEquals(expectedValue, actualValue);

			}

		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		int initialPopulation = 100;

		PeoplePluginData peoplePluginData = PersonPropertiesTestPluginFactory
				.getStandardPeoplePluginData(initialPopulation);

		assertEquals(initialPopulation, peoplePluginData.getPersonIds().size());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
			List.class, long.class })
	public void testGetStandardRegionsPluginData() {

		long seed = 2729857981015931439L;
		int initialPopulation = 100;
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		RegionsPluginData regionsPluginData = PersonPropertiesTestPluginFactory.getStandardRegionsPluginData(people,
				seed);

		Set<TestRegionId> expectedRegionIds = EnumSet.allOf(TestRegionId.class);
		assertFalse(expectedRegionIds.isEmpty());

		Set<RegionId> actualRegionIds = regionsPluginData.getRegionIds();
		assertEquals(expectedRegionIds, actualRegionIds);

		assertEquals(initialPopulation, regionsPluginData.getPersonCount());

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		for (PersonId personId : people) {
			assertTrue(regionsPluginData.getPersonRegion(personId).isPresent());
			TestRegionId expectedRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			assertEquals(expectedRegionId, regionsPluginData.getPersonRegion(personId).get());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = PersonPropertiesTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}
}
