package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginData;
import plugins.groups.GroupsPluginId;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

@Testable
public class AT_GroupsTestPluginFactory {

	/**
	 * Convience method to create a consumer to facilitate testing the factory
	 * methods
	 * {@link AT_GroupsTestPluginFactory#testFactory_Consumer()}
	 * and
	 * {@link AT_GroupsTestPluginFactory#testFactory_TestPluginData()}
	 * 
	 * <li>either for passing directly to
	 * <li>{@link GroupsTestPluginFactory#factory(long, Consumer)}
	 * <li>or indirectly via creating a TestPluginData and passing it to
	 * <li>{@link GroupsTestPluginFactory#factory(long, TestPluginData)}
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

			// show that there are 60 groups
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			assertEquals(60, groupsDataManager.getGroupIds().size());

			// show that there are 300 group memberships
			int membershipCount = 0;
			for (GroupId groupId : groupsDataManager.getGroupIds()) {
				membershipCount += groupsDataManager.getPersonCountForGroup(groupId);

			}
			assertEquals(300, membershipCount);

			// show that the group properties exist
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(groupsDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(),
						testGroupPropertyId));
			}

			executed.setValue(true);
		};
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
			double.class, long.class, Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(GroupsTestPluginFactory
				.factory(100, 3, 5, 3765548905828391577L, factoryConsumer(executed)).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
			double.class, long.class, TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, factoryConsumer(executed)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				GroupsTestPluginFactory.factory(100, 3, 5, 1937810385546394605L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		assertEquals(4, GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
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
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupsPluginData", args = {
			GroupsPluginData.class })
	public void testSetGroupsPluginData() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)
				.addGroup(new GroupId(1), TestGroupTypeId.GROUP_TYPE_2)
				.addGroup(new GroupId(2), TestGroupTypeId.GROUP_TYPE_1)
				.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
				.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_2);

		GroupsPluginData groupsPluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory
				.factory(0, 0, 0, 0, t -> {
				})
				.setGroupsPluginData(groupsPluginData)
				.getPlugins();

		checkPlugins(plugins, groupsPluginData, GroupsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
			PeoplePluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for (int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory
				.factory(0, 0, 0, 0, t -> {
				})
				.setPeoplePluginData(peoplePluginData)
				.getPlugins();

		checkPlugins(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
			StochasticsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(8478739978811865148L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory
				.factory(0, 0, 0, 0, t -> {
				})
				.setStochasticsPluginData(stochasticsPluginData)
				.getPlugins();

		checkPlugins(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardGroupsPluginData", args = { int.class,
			int.class, List.class, long.class })
	public void testGetStandardGroupsPluginData() {

		int initialPopulation = 100;
		int expectedGroupsPerPerson = 3;
		int expectedPeoplePerGroup = 5;

		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		GroupsPluginData groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount,
				membershipCount, people, 6442469165497328184L);

		assertEquals(groupsPluginData.getGroupIds().size(), groupCount);
		assertEquals(groupsPluginData.getPersonCount(), initialPopulation);

		double numGroups = 0;
		for (PersonId person : people) {
			numGroups += groupsPluginData.getGroupsForPerson(person).size();
		}

		double actualGroupsPerPerson = numGroups / initialPopulation;

		double lowerBound = expectedGroupsPerPerson * 0.9;
		double upperBound = expectedGroupsPerPerson * 1.1;

		assertTrue(actualGroupsPerPerson <= upperBound);
		assertTrue(actualGroupsPerPerson > lowerBound);
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = { int.class })
	public void testGetStandardPeoplePluginData() {

		int initialPopulation = 100;
		PeoplePluginData peoplePluginData = GroupsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);

		assertEquals(initialPopulation, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
			long.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = GroupsTestPluginFactory
				.getStandardStochasticsPluginData(seed);

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}

}
