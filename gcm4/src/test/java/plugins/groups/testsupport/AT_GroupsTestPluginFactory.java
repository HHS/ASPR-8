package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginData;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_GroupsTestPluginFactory {

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class, double.class, long.class, Consumer.class })
	public void testFactory1() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 5, 3765548905828391577L, (c) -> {

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
				assertTrue(groupsDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			executed.setValue(true);
		}).getPlugins());
		assertTrue(executed.getValue());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class, double.class, long.class, Plugin.class })
	public void testFactory2() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

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
				assertTrue(groupsDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			executed.setValue(true);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 5, 1937810385546394605L, testPlugin).getPlugins());
		assertTrue(executed.getValue());
		
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupsPluginData", args = { GroupsPluginData.class })
	public void testSetGroupsPluginData() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)
		.addGroup(new GroupId(1), TestGroupTypeId.GROUP_TYPE_2)
		.addGroup(new GroupId(2), TestGroupTypeId.GROUP_TYPE_1)
		.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
		.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_2);

		GroupsPluginData groupsPluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {}).setGroupsPluginData(groupsPluginData).getPlugins();

		plugins.forEach((plugin) -> {
			// can do this because it is known that each plugin only gets one data associated with it in this instance
			PluginData pluginData = plugin.getPluginDatas().toArray(new PluginData[0])[0];
			if(pluginData instanceof GroupsPluginData) {
				assertEquals(groupsPluginData, (GroupsPluginData) pluginData);
			} else {
				assertNotEquals(groupsPluginData, pluginData);
			}
		});
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = { GroupsPluginData.class })
	public void testSetPeoplePluginData() {
		PeoplePluginData.Builder builder = PeoplePluginData.builder();

		for(int i = 0; i < 100; i++) {
			builder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {}).setPeoplePluginData(peoplePluginData).getPlugins();

		plugins.forEach((plugin) -> {
			// can do this because it is known that each plugin only gets one data associated with it in this instance
			PluginData pluginData = plugin.getPluginDatas().toArray(new PluginData[0])[0];
			if(pluginData instanceof PeoplePluginData) {
				assertEquals(peoplePluginData, (PeoplePluginData) pluginData);
			} else {
				assertNotEquals(peoplePluginData, pluginData);
			}
		});
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = { GroupsPluginData.class })
	public void testSetStochasticsPluginData() {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

		builder.setSeed(8478739978811865148L).addRandomGeneratorId(TestRandomGeneratorId.BLITZEN);

		StochasticsPluginData stochasticsPluginData = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {}).setStochasticsPluginData(stochasticsPluginData).getPlugins();

		plugins.forEach((plugin) -> {
			// can do this because it is known that each plugin only gets one data associated with it in this instance
			PluginData pluginData = plugin.getPluginDatas().toArray(new PluginData[0])[0];
			if(pluginData instanceof StochasticsPluginData) {
				assertEquals(stochasticsPluginData, (StochasticsPluginData) pluginData);
			} else {
				assertNotEquals(stochasticsPluginData, pluginData);
			}
		});
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardGroupsPluginData", args = { int.class, int.class, List.class, RandomGenerator.class })
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
			
		GroupsPluginData groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount, membershipCount, people, RandomGeneratorProvider.getRandomGenerator(6442469165497328184L));

		assertEquals(groupsPluginData.getGroupIds().size(), groupCount);
		assertEquals(groupsPluginData.getPersonCount(), initialPopulation);
		for(PersonId person : people) {
			assertTrue(groupsPluginData.getGroupsForPerson(person).size() < groupCount);
		}
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = { int.class, int.class, List.class, RandomGenerator.class })
	public void testGetStandardPeoplePluginData() {

		int initialPopulation = 100;
		PeoplePluginData peoplePluginData = GroupsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);

		assertEquals(initialPopulation, peoplePluginData.getPersonIds().size());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = { int.class, int.class, List.class, RandomGenerator.class })
	public void testGetStandardStochasticsPluginData() {
		long seed = 6072871729256538807L;
		StochasticsPluginData stochasticsPluginData = GroupsTestPluginFactory.getStandardStochasticsPluginData(RandomGeneratorProvider.getRandomGenerator(seed));

		assertEquals(RandomGeneratorProvider.getRandomGenerator(seed).nextLong(), stochasticsPluginData.getSeed());
		assertEquals(0, stochasticsPluginData.getRandomNumberGeneratorIds().size());
	}

}
