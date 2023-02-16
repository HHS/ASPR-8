package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginData;
import plugins.groups.GroupsPluginId;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableBoolean;

@Testable
public class AT_GroupsTestPluginFactory {

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
			double.class, long.class, Consumer.class })
	public void testFactory_Consumer() {
		MutableBoolean executed = new MutableBoolean();
		TestSimulation.executeSimulation(GroupsTestPluginFactory
				.factory(100, 3, 5, 3765548905828391577L, c -> executed.setValue(true)).getPlugins());
		assertTrue(executed.getValue());

		// precondition: consumer is null
		Consumer<ActorContext> nullConsumer = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsTestPluginFactory.factory(0, 0, 0, 0, nullConsumer));
		assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
			double.class, long.class, TestPluginData.class })
	public void testFactory_TestPluginData() {
		MutableBoolean executed = new MutableBoolean();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulation.executeSimulation(
				GroupsTestPluginFactory.factory(100, 3, 5, 1937810385546394605L, testPluginData).getPlugins());
		assertTrue(executed.getValue());

		// precondition: testPluginData is null
		TestPluginData nullTestPluginData = null;
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsTestPluginFactory.factory(0, 0, 0, 0, nullTestPluginData));
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
	@UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
	public void testGetPlugins() {
		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
		}).getPlugins();
		assertEquals(4, plugins.size());

		checkPluginExists(plugins, GroupsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
		checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
		checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
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

		checkPluginDataExists(plugins, groupsPluginData, GroupsPluginId.PLUGIN_ID);

		// precondition: groupsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsTestPluginFactory
						.factory(0, 0, 0, 0, t -> {
						})
						.setGroupsPluginData(null));
		assertEquals(GroupError.NULL_GROUP_PLUGIN_DATA, contractException.getErrorType());
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

		checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

		// precondition: peoplePluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsTestPluginFactory
						.factory(0, 0, 0, 0, t -> {
						})
						.setPeoplePluginData(null));
		assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
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

		checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

		// precondition: stochasticsPluginData is not null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupsTestPluginFactory
						.factory(0, 0, 0, 0, t -> {
						})
						.setStochasticsPluginData(null));
		assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardGroupsPluginData", args = { int.class,
			int.class, List.class, long.class })
	public void testGetStandardGroupsPluginData() {

		long seed = 6442469165497328184L;
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
				membershipCount, people, seed);

		Set<TestGroupTypeId> expectedGroupTypeIds = EnumSet.allOf(TestGroupTypeId.class);
		assertFalse(expectedGroupTypeIds.isEmpty());

		Set<GroupTypeId> actualGroupTypeIds = groupsPluginData.getGroupTypeIds();
		assertEquals(expectedGroupTypeIds, actualGroupTypeIds);

		Set<TestGroupPropertyId> expectedGroupPropertyIds = EnumSet.allOf(TestGroupPropertyId.class);
		assertFalse(expectedGroupPropertyIds.isEmpty());

		for (TestGroupPropertyId expectedPropertyId : TestGroupPropertyId.values()) {
			TestGroupTypeId expectedGroupTypeId = expectedPropertyId.getTestGroupTypeId();
			PropertyDefinition expectedPropertyDefinition = expectedPropertyId.getPropertyDefinition();

			assertTrue(groupsPluginData.getGroupPropertyIds(expectedGroupTypeId).contains(expectedPropertyId));
			PropertyDefinition actualPropertyDefinition = groupsPluginData
					.getGroupPropertyDefinition(expectedGroupTypeId, expectedPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		assertEquals(groupCount, groupsPluginData.getGroupIds().size());
		assertEquals(initialPopulation, groupsPluginData.getPersonCount());

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		for (GroupId groupId : groupsPluginData.getGroupIds()) {
			TestGroupTypeId expectedGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			GroupTypeId actualGroupTypeId = groupsPluginData.getGroupTypeId(groupId);
			assertEquals(expectedGroupTypeId, actualGroupTypeId);

			List<GroupPropertyValue> expectedGroupPropertyValues = new ArrayList<>();
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
					.getTestGroupPropertyIds(expectedGroupTypeId)) {
				GroupPropertyValue expectedValue = new GroupPropertyValue(testGroupPropertyId,
						testGroupPropertyId.getRandomPropertyValue(randomGenerator));
				expectedGroupPropertyValues.add(expectedValue);
			}

			assertEquals(expectedGroupPropertyValues.size(), groupsPluginData.getGroupPropertyValues(groupId).size());
			for (int i = 0; i < expectedGroupPropertyValues.size(); i++) {
				assertEquals(expectedGroupPropertyValues.get(i),
						groupsPluginData.getGroupPropertyValues(groupId).get(i));
			}
		}

		Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
		List<GroupId> groups = groupsPluginData.getGroupIds();
		while (groupMemeberships.size() < membershipCount) {
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
			groupMemeberships.add(new MultiKey(groupId, personId));
		}

		for (MultiKey multiKey : groupMemeberships) {
			GroupId expectedGroupId = multiKey.getKey(0);
			PersonId expectedPersonId = multiKey.getKey(1);

			assertTrue(groupsPluginData.getGroupsForPerson(expectedPersonId).contains(expectedGroupId));
		}

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
