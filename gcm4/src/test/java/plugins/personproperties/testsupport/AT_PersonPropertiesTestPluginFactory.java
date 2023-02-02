package plugins.personproperties.testsupport;

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
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_PersonPropertiesTestPluginFactory {

	private Consumer<ActorContext> factoryConsumer(MutableBoolean executed) {
		return (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			assertEquals(100, peopleDataManager.getPeople().size());

			assertEquals(TestPersonPropertyId.values().length, personPropertiesDataManager.getPersonPropertyIds().size());
			
			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(PersonPropertiesTestPluginFactory
				.factory(100, 4135374341935235561L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
			TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				PersonPropertiesTestPluginFactory.factory(100, 92376779979686632L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(5, PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
		}).getPlugins().size());
	}

	private <T extends PluginData> void checkPlugins(List<Plugin> plugins, T expectedPluginData) {
		Class<?> classRef = expectedPluginData.getClass();
		plugins.forEach((plugin) -> {
			Set<PluginData> pluginDatas = plugin.getPluginDatas();
			if(pluginDatas.size() > 0) {
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

		checkPlugins(plugins, personPropertiesPluginData);
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

		checkPlugins(plugins, peoplePluginData);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
			RegionsPluginData.class })
	public void testSetRegionsPluginData() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7542086961742735322L);
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

		List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
		}).setRegionsPluginData(regionsPluginData).getPlugins();

		checkPlugins(plugins, regionsPluginData);

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

		checkPlugins(plugins, stochasticsPluginData);
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPersonPropertiesPluginData", args = {
			List.class, long.class })
	public void testGetStandardPersonPropertiesPluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesTestPluginFactory
				.getStandardPersonPropertiesPluginData(people, 4684903523797799712L);
		assertNotNull(personPropertiesPluginData);

		assertEquals(TestPersonPropertyId.values().length, personPropertiesPluginData.getPersonPropertyIds().size());
			
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
			int.class })
	public void testGetStandardPeoplePluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = PersonPropertiesTestPluginFactory.getStandardPeoplePluginData(100);

		assertEquals(100, peoplePluginData.getPersonIds().size());

		for(PersonId personId : people) {
			assertTrue(peoplePluginData.getPersonIds().contains(personId));
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
			List.class, long.class })
	public void testGetStandardRegionsPluginData() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		RegionsPluginData regionsPluginData = PersonPropertiesTestPluginFactory.getStandardRegionsPluginData(people,
				2729857981015931439L);
		assertNotNull(regionsPluginData);

		assertEquals(100, regionsPluginData.getPersonCount());
		
		for(PersonId personId : people) {
			assertTrue(regionsPluginData.getPersonRegion(personId).isPresent());
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
