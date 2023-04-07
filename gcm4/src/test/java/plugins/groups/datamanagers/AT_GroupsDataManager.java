package plugins.groups.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.testsupport.testplugin.TestOutputConsumer;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginData;
import plugins.groups.events.GroupAdditionEvent;
import plugins.groups.events.GroupImminentRemovalEvent;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.events.GroupPropertyDefinitionEvent;
import plugins.groups.events.GroupPropertyUpdateEvent;
import plugins.groups.events.GroupTypeAdditionEvent;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyDefinitionInitialization;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import plugins.groups.testsupport.TestAuxiliaryGroupPropertyId;
import plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;
import util.wrappers.MutableObject;

public class AT_GroupsDataManager {

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "init", args = {GroupId.class})
	public void testInit_State() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(385296335335709376L);
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		GroupsPluginData groupsPluginData = builder.build();

		// add a property definition
		PropertyDefinition propertyDefinition = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
		GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
				.setPropertyDefinition(propertyDefinition)
				.setPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)
				.build();
		List<GroupId> expectedGroupIds = new ArrayList<>();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// define property definition with the data manager
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addGroupType(groupPropertyDefinitionInitialization.getGroupTypeId());
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder()
					.setGroupTypeId(groupPropertyDefinitionInitialization.getGroupTypeId())
					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true)
					.build();
			GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			expectedGroupIds.add(groupId);
			groupsDataManager.setGroupPropertyValue(groupId, groupPropertyDefinitionInitialization.getPropertyId(), true);
			}));

		// show that the plugin data contains what we defined
		TestPluginData testPluginData = pluginBuilder.build();
		Long seed = randomGenerator.nextLong();
		Factory factory = GroupsTestPluginFactory.factory(30, 1, 10, seed, testPluginData)
				.setGroupsPluginData(groupsPluginData);
		TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins())
				.setSimulationHaltTime(2)
				.setProduceSimulationStateOnHalt(true)
				.build()
				.execute();
		Map<GroupsPluginData, Integer> outputItems = testOutputConsumer.getOutputItems(GroupsPluginData.class);
		assertEquals(1, outputItems.size());
		GroupsPluginData actualPluginData = outputItems.keySet().iterator().next();
		GroupsPluginData expectedPluginData = GroupsPluginData.builder()
				.defineGroupProperty(groupPropertyDefinitionInitialization.getGroupTypeId(), groupPropertyDefinitionInitialization.getPropertyId(), groupPropertyDefinitionInitialization.getPropertyDefinition())
				.addGroupTypeId(groupPropertyDefinitionInitialization.getGroupTypeId())
				.addGroup(expectedGroupIds.get(0), groupPropertyDefinitionInitialization.getGroupTypeId())
				.setGroupPropertyValue(expectedGroupIds.get(0), groupPropertyDefinitionInitialization.getPropertyId(), true)
				.build();
		assertEquals(expectedPluginData, actualPluginData);

		// show that the plugin data persists after multiple actions
		PropertyDefinition propertyDefinition2 = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK.getPropertyDefinition();
		GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization2 = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_2)
				.setPropertyDefinition(propertyDefinition2)
				.setPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK)
				.build();

		PropertyDefinition propertyDefinition3 = TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK.getPropertyDefinition();
		GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization3 = GroupPropertyDefinitionInitialization.builder()
				.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_3)
				.setPropertyDefinition(propertyDefinition3)
				.setPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK)
				.build();

		expectedGroupIds.clear();
		pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addGroupType(groupPropertyDefinitionInitialization2.getGroupTypeId());
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization2);
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder()
					.setGroupTypeId(groupPropertyDefinitionInitialization2.getGroupTypeId())
					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 43)
					.build();
			GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			expectedGroupIds.add(groupId);
			groupsDataManager.setGroupPropertyValue(groupId, groupPropertyDefinitionInitialization2.getPropertyId(), 43);
			groupsDataManager.setGroupPropertyValue(groupId, groupPropertyDefinitionInitialization2.getPropertyId(), 57);

			groupsDataManager.addGroupType(groupPropertyDefinitionInitialization3.getGroupTypeId());
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization3);
			GroupConstructionInfo groupConstructionInfo2 = GroupConstructionInfo.builder()
					.setGroupTypeId(groupPropertyDefinitionInitialization3.getGroupTypeId())
					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 15.9)
					.build();
			GroupId groupId2 = groupsDataManager.addGroup(groupConstructionInfo2);
			expectedGroupIds.add(groupId2);
			groupsDataManager.setGroupPropertyValue(groupId2, groupPropertyDefinitionInitialization3.getPropertyId(), 15.9);
			groupsDataManager.setGroupPropertyValue(groupId2, groupPropertyDefinitionInitialization3.getPropertyId(), 34.2);
		}));

		testPluginData = pluginBuilder.build();
		seed = randomGenerator.nextLong();
		factory = GroupsTestPluginFactory.factory(30, 1, 10, seed, testPluginData)
				.setGroupsPluginData(groupsPluginData);
		testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins())
				.setSimulationHaltTime(2)
				.setProduceSimulationStateOnHalt(true)
				.build()
				.execute();
		outputItems = testOutputConsumer.getOutputItems(GroupsPluginData.class);
		assertEquals(1, outputItems.size());
		actualPluginData = outputItems.keySet().iterator().next();
		expectedPluginData = GroupsPluginData.builder()
				.defineGroupProperty(groupPropertyDefinitionInitialization2.getGroupTypeId(), groupPropertyDefinitionInitialization2.getPropertyId(), groupPropertyDefinitionInitialization2.getPropertyDefinition())
				.defineGroupProperty(groupPropertyDefinitionInitialization3.getGroupTypeId(), groupPropertyDefinitionInitialization3.getPropertyId(), groupPropertyDefinitionInitialization3.getPropertyDefinition())
				.addGroupTypeId(groupPropertyDefinitionInitialization2.getGroupTypeId())
				.addGroupTypeId(groupPropertyDefinitionInitialization3.getGroupTypeId())
				.addGroup(expectedGroupIds.get(0), groupPropertyDefinitionInitialization2.getGroupTypeId())
				.addGroup(expectedGroupIds.get(1), groupPropertyDefinitionInitialization3.getGroupTypeId())
				.setGroupPropertyValue(expectedGroupIds.get(0), groupPropertyDefinitionInitialization2.getPropertyId(), 57)
				.setGroupPropertyValue(expectedGroupIds.get(1), groupPropertyDefinitionInitialization3.getPropertyId(), 34.2)
				.build();
		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "removeGroup", args = { GroupId.class })
	public void testRemoveGroup() {

		Set<GroupId> removedGroups = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);

				// show that the returned group id is not null
				assertNotNull(groupId);

				// show that the manager indicates the group id exists
				assertTrue(groupsDataManager.groupExists(groupId));

				// remove the group
				groupsDataManager.removeGroup(groupId);

				removedGroups.add(groupId);

			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// show that the group is no long present
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : removedGroups) {
				assertFalse(groupsDataManager.groupExists(groupId));
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 8204685090168544876L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 1164752712088660908L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.removeGroup(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition test: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 6321229743136171684L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.removeGroup(new GroupId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "removePersonFromGroup", args = { PersonId.class, GroupId.class })
	public void testRemovePersonFromGroup() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupMembershipAdditionEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.groupId(), e.personId()));
			});

		}));

		// add an agent to add members to groups

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : groupsDataManager.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(groupsDataManager.getPeopleForGroup(groupId));
				int count = 0;
				for (PersonId personId : people) {
					if (!peopleForGroup.contains(personId)) {
						groupsDataManager.addPersonToGroup(personId, groupId);
						expectedObservations.add(new MultiKey(groupId, personId));
						count++;
					}
					if (count == 3) {
						break;
					}
				}
			}

		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(27, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 10, 2733223420384068616L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 667206327628089405L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.removePersonFromGroup(null, groupId);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 283038490401536931L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.removePersonFromGroup(new PersonId(10000), groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the group id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 6913106996750459497L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.removePersonFromGroup(personId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 4632472396816795419L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.removePersonFromGroup(personId, new GroupId(10000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the person is not a member of the group */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8295961559327801013L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.removePersonFromGroup(personId, groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NON_GROUP_MEMBERSHIP, contractException.getErrorType());
	}

	private static enum ExcludedPersonType {
		NULL, MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestConstructor(target = GroupsDataManager.class, args = { GroupsPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new GroupsDataManager(null));
		assertEquals(GroupError.NULL_GROUP_INITIALIZATION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "addGroup", args = { GroupConstructionInfo.class })
	public void testAddGroup_GroupConstructionInfo() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedGroupObservations = new LinkedHashSet<>();
		Set<GroupId> actualGroupObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupAdditionEvent.class).build(), (c2, e) -> {
				actualGroupObservations.add(e.groupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupConstructionInfo.Builder builder = GroupConstructionInfo.builder();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				builder.setGroupTypeId(testGroupTypeId);
				Map<TestGroupPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setGroupPropertyValue(testGroupPropertyId, value);
					expectedPropertyValues.put(testGroupPropertyId, value);
				}
				GroupConstructionInfo groupConstructionInfo = builder.build();
				GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);

				expectedGroupObservations.add(groupId);

				// show that the group was created, has the correct type and has
				// the correct property values
				assertTrue(groupsDataManager.groupExists(groupId));
				assertEquals(testGroupTypeId, groupsDataManager.getGroupType(groupId));
				for (TestGroupPropertyId testGroupPropertyId : expectedPropertyValues.keySet()) {
					Object expectedValue = expectedPropertyValues.get(testGroupPropertyId);
					Object actualValue = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
					assertEquals(expectedValue, actualValue);
				}

			}
		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertTrue(expectedGroupObservations.size() > 0);
			assertEquals(expectedGroupObservations, actualGroupObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(40, 5.0, 20.0, 5865498314869329641L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group construction info is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(40, 5.0, 20.0, 5229546252018518751L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupConstructionInfo nullGroupConstructionInfo = null;
				groupsDataManager.addGroup(nullGroupConstructionInfo);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_CONSTRUCTION_INFO, contractException.getErrorType());

		/*
		 * precondition test: if the group type id contained in the group
		 * construction info is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(40, 5.0, 20.0, 7404840971962130072L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.addGroup(GroupConstructionInfo.builder().setGroupTypeId(TestGroupTypeId.getUnknownGroupTypeId()).build());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		/*
		 * precondition test:if a group property id contained in the group
		 * construction info is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(40, 5.0, 20.0, 8782123343145389682L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.getUnknownGroupPropertyId(), 1)//
																					.build();//
				groupsDataManager.addGroup(groupConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if a group property value contained in the group
		 * construction info is incompatible with the corresponding property
		 * definition
		 */

		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(40, 5.0, 20.0, 8782123343145389682L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, 1)//
																					.build();//
				groupsDataManager.addGroup(groupConstructionInfo);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "addGroup", args = { GroupTypeId.class })
	public void testAddGroup_GroupTypeId() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupAdditionEvent.class).build(), (c2, e) -> {
				actualObservations.add(e.groupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			expectedObservations.add(groupId);

			groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			expectedObservations.add(groupId);

			groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			expectedObservations.add(groupId);

		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(3, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 8137195527612056024L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 5229546252018518751L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.addGroup(groupTypeId);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 5229546252018518751L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.addGroup(TestGroupTypeId.getUnknownGroupTypeId());

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "addPersonToGroup", args = { PersonId.class, GroupId.class })
	public void testAddPersonToGroup() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupMembershipAdditionEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.groupId(), e.personId()));
			});

		}));

		// add an agent to add members to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : groupsDataManager.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(groupsDataManager.getPeopleForGroup(groupId));
				int count = 0;

				for (PersonId personId : people) {

					if (!peopleForGroup.contains(personId)) {
						groupsDataManager.addPersonToGroup(personId, groupId);
						assertTrue(groupsDataManager.isPersonInGroup(personId, groupId));
						expectedObservations.add(new MultiKey(groupId, personId));
						count++;
					}
					if (count == 3) {
						break;
					}
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(27, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 10, 2733223420384068616L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 2886293572900391101L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.addPersonToGroup(null, groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 5604775963632692909L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.addPersonToGroup(new PersonId(10000), groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the group id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3853147120254074375L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.addPersonToGroup(personId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7259750239550962667L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.addPersonToGroup(personId, new GroupId(10000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the person is already a member of the group
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3285943689624298882L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				groupsDataManager.addPersonToGroup(personId, groupId);
				groupsDataManager.addPersonToGroup(personId, groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.DUPLICATE_GROUP_MEMBERSHIP, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "groupExists", args = { GroupId.class })
	public void testGroupExists() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		List<GroupId> removedGroupIds = new ArrayList<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// add a group and show it exists
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				assertTrue(groupsDataManager.groupExists(groupId));
				// remove the group and record it for later verification
				groupsDataManager.removeGroup(groupId);
				removedGroupIds.add(groupId);
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : removedGroupIds) {
				// show that the removed groups don't exist
				assertFalse(groupsDataManager.groupExists(groupId));
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 2946647177720026906L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "sampleGroup", args = { GroupId.class, GroupSampler.class })
	public void testSampleGroup() {

		Consumer<ActorContext> consumer = (c) -> {
			// establish data views and the lists to groups and people in the
			// simulation
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Set up boolean looping over the use of weighting functions in the
			 * GroupSampler
			 */
			Set<Boolean> weightingFunctionValues = new LinkedHashSet<>();
			weightingFunctionValues.add(false);
			weightingFunctionValues.add(true);

			/*
			 * Create a weight function that will allow us to exclude about half
			 * of the people from any group from being selected
			 */
			GroupWeightingFunction gwf = (c2, p, g) -> {
				if (p.getValue() % 2 == 0) {
					return 0;
				}
				return 1.0;
			};

			/*
			 * Test every group against every excluded person category and use
			 * of the weighting function
			 */
			for (GroupId groupId : groupIds) {
				for (ExcludedPersonType excludedPersonType : ExcludedPersonType.values()) {
					for (Boolean useWeightingFunction : weightingFunctionValues) {
						// start building the group sampler
						GroupSampler.Builder groupSamplerBuilder = GroupSampler.builder();

						// Determine the sets of people in and out of the group
						List<PersonId> peopleForGroup = groupsDataManager.getPeopleForGroup(groupId);
						Set<PersonId> peopleNotInGroupSet = new LinkedHashSet<>(people);
						peopleNotInGroupSet.removeAll(peopleForGroup);
						List<PersonId> peopleNotInGroupList = new ArrayList<>(peopleNotInGroupSet);

						// Add the weighting function if needed
						if (useWeightingFunction) {
							groupSamplerBuilder.setGroupWeightingFunction(gwf);
						}

						// Add the excluded person based on the category for
						// choosing the excluded person
						PersonId excludedPersonId = null;
						switch (excludedPersonType) {
						case MEMBER:
							if (!peopleForGroup.isEmpty()) {
								excludedPersonId = peopleForGroup.get(randomGenerator.nextInt(peopleForGroup.size()));
							}
							break;
						case NON_MEMBER:
							if (!peopleNotInGroupList.isEmpty()) {
								excludedPersonId = peopleNotInGroupList.get(randomGenerator.nextInt(peopleNotInGroupList.size()));
							}
							break;
						case NULL:
							break;
						default:
							throw new RuntimeException("unhandled case " + excludedPersonType);
						}
						groupSamplerBuilder.setExcludedPersonId(excludedPersonId);

						// build the group sampler
						GroupSampler groupSampler = groupSamplerBuilder.build();

						Set<PersonId> eligiblePeople = new LinkedHashSet<>();
						/*
						 * If we are using the weighting function, then only
						 * select the odd people as eligible, otherwise select
						 * everyone in the group
						 */
						if (useWeightingFunction) {
							for (PersonId personId : peopleForGroup) {
								if (personId.getValue() % 2 == 1) {
									eligiblePeople.add(personId);
								}
							}
						} else {
							eligiblePeople.addAll(peopleForGroup);
						}

						// Remove the excluded person from the eligible people
						eligiblePeople.remove(excludedPersonId);

						/*
						 * If there are no eligible people, then the
						 * sampleGroup() method should return an empty optional
						 */
						if (eligiblePeople.isEmpty()) {
							Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
							assertFalse(optional.isPresent());
						} else {
							// Draw a reasonable number of people from the group
							// and show that they are all eligible people
							for (int i = 0; i < eligiblePeople.size(); i++) {
								Optional<PersonId> optional = groupsDataManager.sampleGroup(groupId, groupSampler);
								assertTrue(optional.isPresent());
								PersonId selectedPersonId = optional.get();
								assertTrue(eligiblePeople.contains(selectedPersonId));
							}
						}

					}
				}
			}

		};
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 9211292135944399530L, consumer);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 5080244401642933835L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.sampleGroup(null, GroupSampler.builder().build());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 8782123343145389682L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.sampleGroup(new GroupId(1000000), GroupSampler.builder().build());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group sampler is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 4175298436277522063L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.sampleGroup(new GroupId(0), null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_SAMPLER, contractException.getErrorType());

		/* precondition test: if the group sampler is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 7404840971962130072L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.sampleGroup(new GroupId(0), GroupSampler.builder().setExcludedPersonId(new PersonId(1000000)).build());
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupPropertyUpdateEvent.class).build(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId(), e.previousPropertyValue(), e.currentPropertyValue()));

			});
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// show that there are some groups
			assertTrue(groupIds.size() > 0);

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object currentValue = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
						Object expectedValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, expectedValue);
						Object actualValue = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
						assertEquals(expectedValue, actualValue);
						expectedObservations.add(new MultiKey(groupId, testGroupPropertyId, currentValue, expectedValue));

					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 4653012806568812031L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 3285943689624298882L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.setGroupPropertyValue(null, testGroupPropertyId, true);			
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		

		/* precondition test if the group id is unknown */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 3853147120254074375L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.setGroupPropertyValue(new GroupId(100000), testGroupPropertyId, true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/* precondition test if the group property id is null */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 5118884606334935158L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.setGroupPropertyValue(groupId, null, true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		

		/* precondition test if the group property id is unknown */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 6389640203066924425L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.setGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId(), true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		

		/* precondition test if the property value is null */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 6323361964403648167L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * precondition test if property value is incompatible with the
		 * corresponding property definition
		 */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 3728888495166492963L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, 5);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		

		/*
		 * precondition test if the corresponding property definition defines
		 * the property as immutable
		 */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 7440937277837294440L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK;
				groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, true);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());		

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupCountForGroupType", args = { GroupTypeId.class })
	public void testGetGroupCountForGroupType() {

		Factory factory = GroupsTestPluginFactory.factory(300, 3, 5, 2910747162784803859L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct containers to hold expectations and actual counts
			Map<GroupTypeId, MutableInteger> actualCounts = new LinkedHashMap<>();
			Map<GroupTypeId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				actualCounts.put(testGroupTypeId, new MutableInteger());
				int count = groupsDataManager.getGroupCountForGroupType(testGroupTypeId);
				expectedCounts.put(testGroupTypeId, new MutableInteger(count));
			}

			// poll through the groups and increment the corresponding counters
			for (GroupId groupId : groupIds) {
				actualCounts.get(groupsDataManager.getGroupType(groupId)).increment();
			}
			// show that expectation were met
			assertEquals(expectedCounts, actualCounts);

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group type is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(300, 3, 5, 8342387507356594823L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupCountForGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group type is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(300, 3, 5, 4573510051341354320L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupCountForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupCountForGroupTypeAndPerson() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 6434309925268726988L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<MultiKey, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
			}

			/*
			 * For each person pick three groups at random and add the person to
			 * each group, recording this in the expected data structure
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					expectedDataStructure.put(multiKey, new LinkedHashSet<>());
				}

				for (int i = 0; i < 3; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
					MultiKey multiKey = new MultiKey(groupTypeId, personId);
					Set<GroupId> groups = expectedDataStructure.get(multiKey);
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (PersonId personId : people) {
					int actualGroupCount = groupsDataManager.getGroupCountForGroupTypeAndPerson(testGroupTypeId, personId);
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					int expectedGroupCount = expectedDataStructure.get(multiKey).size();
					assertEquals(expectedGroupCount, actualGroupCount);
				}
			}
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 3966867633401336210L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 4582534442214781870L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroupType(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupCountForPerson", args = { PersonId.class })
	public void testGetGroupCountForPerson() {

		Factory factory = GroupsTestPluginFactory.factory(300, 3, 5, 6371809280692201768L, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct a container to hold expectations
			Map<PersonId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (PersonId personId : people) {
				expectedCounts.put(personId, new MutableInteger());
			}

			// poll through the groups and build the expectations
			for (GroupId groupId : groupIds) {
				List<PersonId> peopleInGroup = groupsDataManager.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					expectedCounts.get(personId).increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				int expectedValue = expectedCounts.get(personId).getValue();
				int actualValue = groupsDataManager.getGroupCountForPerson(personId);
				assertEquals(expectedValue, actualValue);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(300, 3, 5, 3920152432964044129L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupCountForPerson(null);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(300, 3, 5, 6739633613106510243L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupCountForPerson(new PersonId(10000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupIds", args = {})
	public void testGetGroupIds() {

		Factory factory = GroupsTestPluginFactory.factory(10, 0, 5, 6455798573295403809L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
				expectedGroupIds.add(groupId);
			}

			// show that the group ids match the expected group ids
			List<GroupId> actualGroupIds = groupsDataManager.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupPropertyDefinition", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyDefinition() {

		Factory factory = GroupsTestPluginFactory.factory(10, 0, 5, 4462836951642761957L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// show that the personGroupDataManger has the expected property
			// definitions
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = groupsDataManager.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupsDataManager.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group type id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 5959643517439959298L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group type id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 9138791522018557245L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group property id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 9138791522018557245L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/* precondition test: if the group property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 9138791522018557245L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/* precondition tests: if the group property id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 9138791522018557245L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupPropertyExists", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyExists() {

		Factory factory = GroupsTestPluginFactory.factory(10, 0, 5, 8858123829776885259L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// show that the personGroupDataManger returns true for the group
			// properties that should be present
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(groupsDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			// show that other group properties do not exist
			assertFalse(groupsDataManager.getGroupPropertyExists(null, null));
			assertFalse(groupsDataManager.getGroupPropertyExists(null, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertFalse(groupsDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), null));
			assertFalse(groupsDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.getUnknownGroupPropertyId()));
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupPropertyIds", args = { GroupTypeId.class })
	public void testGetGroupPropertyIds() {

		Factory factory = GroupsTestPluginFactory.factory(10, 0, 5, 1205481410658607626L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// show that the personGroupDataManger returns the correct group
			// property ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				Set<TestGroupPropertyId> expectedPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
				Set<GroupPropertyId> actualPropertyIds = groupsDataManager.getGroupPropertyIds(testGroupTypeId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group type id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 8498668590902665283L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyIds(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group type id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 3809094168724176083L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyIds(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupPropertyTime", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Create a container to hold our expectations. The MultiKey will be
		 * (GroupId,GroupPropertyId) pairs and the MutableDoubles will hold the
		 * most recent time when each property was set.
		 */
		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		/*
		 * At time = 1, have the agent show that the property values were all
		 * set at time = 0 and then set those properties to new values
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// show that we have enough groups to conduct the test
			assertTrue(groupIds.size() > 10);

			Set<TestGroupPropertyId> mutableTrackablePropertyIds = new LinkedHashSet<>();
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition propertyDefinition = testGroupPropertyId.getPropertyDefinition();
				if (propertyDefinition.propertyValuesAreMutable()) {
					if (propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {
						mutableTrackablePropertyIds.add(testGroupPropertyId);
					}
				}
			}

			// show that we have at least one mutable, trackable property
			assertTrue(mutableTrackablePropertyIds.size() > 0);

			// Change all the mutable, trackable property values and record the
			// expected time values.
			for (TestGroupPropertyId testGroupPropertyId : mutableTrackablePropertyIds) {
				TestGroupTypeId testGroupTypeId = testGroupPropertyId.getTestGroupTypeId();
				List<GroupId> groupsForGroupType = groupsDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					double groupPropertyTime = groupsDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
					assertEquals(0.0, groupPropertyTime);
					expectedTimes.put(new MultiKey(groupId, testGroupPropertyId), new MutableDouble(1.0));
					groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values were all
		 * set at time = 1 and then set those properties to new values
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = groupsDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);

				mutableDouble.setValue(2.0);
				groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}

		}));

		/*
		 * At time = 3, have the agent show that the property values were all
		 * set at time = 2
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = groupsDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 7313144886869436931L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the group id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 4540064428634658468L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyTime(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group id is null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 5080244401642933835L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyTime(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 4175298436277522063L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyTime(new GroupId(0), null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 3557052948001350675L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.getGroupPropertyTime(groupId, TestGroupPropertyId.getUnknownGroupPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 7349200768842830982L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.getGroupPropertyTime(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Create a container to hold our expectations. The MultiKey will be
		 * (GroupId,GroupPropertyId) pairs and the Object will hold the most
		 * recent property value.
		 */
		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		/*
		 * At time = 1, have the agent establish the expected values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// show that we have enough groups to conduct the test
			assertTrue(groupIds.size() > 10);

			Set<TestGroupPropertyId> mutableTrackablePropertyIds = new LinkedHashSet<>();
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition propertyDefinition = testGroupPropertyId.getPropertyDefinition();
				if (propertyDefinition.propertyValuesAreMutable()) {
					if (propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {
						mutableTrackablePropertyIds.add(testGroupPropertyId);
					}
				}
			}

			// show that we have at least one mutable, trackable property
			assertTrue(mutableTrackablePropertyIds.size() > 0);

			// Change all the mutable, trackable property values and record
			// those values.
			for (TestGroupPropertyId testGroupPropertyId : mutableTrackablePropertyIds) {
				TestGroupTypeId testGroupTypeId = testGroupPropertyId.getTestGroupTypeId();
				List<GroupId> groupsForGroupType = groupsDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					Object value = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values and then set those properties to new values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);

				Object newValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, newValue);
				expectedValues.put(multiKey, newValue);
			}

		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = groupsDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 649112407534985381L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the group id is null
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 1071603906331418640L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyValue(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group id is null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 7115328473763483106L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyValue(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is null
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 2444842488298604050L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupPropertyValue(new GroupId(0), null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 1772465526096544640L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.getGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		/*
		 * precondition test: if the group property id is unknown
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 6994832854288891414L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				groupsDataManager.getGroupPropertyValue(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupsForGroupType", args = { GroupTypeId.class })
	public void testGetGroupsForGroupType() {

		Factory factory = GroupsTestPluginFactory.factory(10, 0, 5, 3948247844369837305L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());
			Map<TestGroupTypeId, Set<GroupId>> expectedTypeToGroupIds = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				expectedTypeToGroupIds.put(testGroupTypeId, new LinkedHashSet<>());
			}
			for (int i = 0; i < 30; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				expectedTypeToGroupIds.get(groupTypeId).add(groupId);
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<GroupId> actualGroupIds = groupsDataManager.getGroupsForGroupType(testGroupTypeId);
				Set<GroupId> expectedGroupIds = expectedTypeToGroupIds.get(testGroupTypeId);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> groupsDataManager.getGroupsForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> groupsDataManager.getGroupsForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group type id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 2441670244909950371L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group type id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(10, 0, 5, 8938160844024056358L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupType(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupsForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupsForGroupTypeAndPerson() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 4847183275886938594L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<MultiKey, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
			}

			/*
			 * For each person pick three groups at random and add the person to
			 * each group, recording this in the expected data structure
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					expectedDataStructure.put(multiKey, new LinkedHashSet<>());
				}

				for (int i = 0; i < 3; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
					MultiKey multiKey = new MultiKey(groupTypeId, personId);
					Set<GroupId> groups = expectedDataStructure.get(multiKey);
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (PersonId personId : people) {
					List<GroupId> actualGroupIds = groupsDataManager.getGroupsForGroupTypeAndPerson(testGroupTypeId, personId);
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					Set<GroupId> expectedGroupIds = expectedDataStructure.get(multiKey);
					assertEquals(expectedGroupIds.size(), actualGroupIds.size());
					assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
				}
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5248499346426314201L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 1445347293441431961L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the group type id is null */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 1445347293441431961L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupTypeAndPerson(null, new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group type id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 1445347293441431961L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupsForPerson", args = { PersonId.class })
	public void testGetGroupsForPerson() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 1095418957424488372L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
			}

			/*
			 * For each person pick three groups at random and add the person to
			 * each group, recording this in the expected data structure
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				for (int i = 0; i < 3; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					Set<GroupId> groups = expectedDataStructure.get(personId);
					if (groups == null) {
						groups = new LinkedHashSet<>();
						expectedDataStructure.put(personId, groups);
					}
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				List<GroupId> actualGroupIds = groupsDataManager.getGroupsForPerson(personId);
				Set<GroupId> expectedGroupIds = expectedDataStructure.get(personId);
				assertNotNull(expectedGroupIds);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 4037186565913379048L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForPerson(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition tests: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5901067879853942202L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupsForPerson(new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupType", args = { GroupId.class })
	public void testGetGroupType() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 5910635654466929788L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, GroupTypeId> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				expectedDataStructure.put(groupId, groupTypeId);
			}

			// show that the group have the expected types
			for (GroupId groupId : expectedDataStructure.keySet()) {
				GroupTypeId actualGroupTypeId = groupsDataManager.getGroupType(groupId);
				GroupTypeId expectedGroupTypeId = expectedDataStructure.get(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 4697608906151940983L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5074440747148359344L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupType(new GroupId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupTypeCountForPersonId", args = { PersonId.class })
	public void testGetGroupTypeCountForPersonId() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 1561008711822589907L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Integer> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
			}

			/*
			 * For each person pick either one two or three group types and
			 * record the expected group type count person person.
			 */
			for (PersonId personId : people) {
				int groupTypeCount = randomGenerator.nextInt(3) + 1;
				expectedDataStructure.put(personId, groupTypeCount);
				groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				for (int i = 0; i < groupTypeCount; i++) {
					List<GroupId> groupsForGroupType = groupsDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				int actualCount = groupsDataManager.getGroupTypeCountForPersonId(personId);
				Integer expectedCount = expectedDataStructure.get(personId);
				assertEquals(expectedCount.intValue(), actualCount);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 2733980118690868605L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupTypeCountForPersonId(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 7646517978722507404L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupTypeCountForPersonId(new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {
		Factory factory = GroupsTestPluginFactory.factory(10, 3, 5, 1999263877784730672L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			// show that the group ids match the expected group ids
			Set<GroupTypeId> groupTypeIds = groupsDataManager.getGroupTypeIds();
			assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupTypeIds);
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getGroupTypesForPerson", args = { PersonId.class })
	public void testGetGroupTypesForPerson() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 2999448198567478958L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupTypeId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
			}

			/*
			 * For each person pick either one two or three group types and
			 * record the expected group type count person person.
			 */
			for (PersonId personId : people) {
				Set<GroupTypeId> groupTypes = new LinkedHashSet<>();
				expectedDataStructure.put(personId, groupTypes);
				int groupTypeCount = randomGenerator.nextInt(3) + 1;
				groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				for (int i = 0; i < groupTypeCount; i++) {
					groupTypes.add(groupTypeId);
					List<GroupId> groupsForGroupType = groupsDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				List<GroupTypeId> actualGroupTypesForPerson = groupsDataManager.getGroupTypesForPerson(personId);
				Set<GroupTypeId> expectedGroupTypesForPerson = expectedDataStructure.get(personId);
				assertEquals(expectedGroupTypesForPerson.size(), actualGroupTypesForPerson.size());
				assertEquals(expectedGroupTypesForPerson, new LinkedHashSet<>(actualGroupTypesForPerson));
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition tests if the person id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5882134079494817898L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupTypesForPerson(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		/* precondition tests if the person id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 4598510399026722120L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getGroupTypesForPerson(new PersonId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getPeopleForGroup", args = { GroupId.class })
	public void testGetPeopleForGroup() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 4550534695972929193L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
				expectedDataStructure.put(groupId, new LinkedHashSet<>());
			}
			groupIds = new ArrayList<>(expectedDataStructure.keySet());

			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				List<PersonId> actualPeople = groupsDataManager.getPeopleForGroup(groupId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 1054111866998260759L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroup(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 976385337250084757L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroup(new GroupId(100000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getPeopleForGroupType", args = { GroupTypeId.class })
	public void testGetPeopleForGroupType() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 8576174021026036673L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupTypeId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
				expectedDataStructure.put(groupTypeId, new LinkedHashSet<>());
			}

			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupTypeId = groupsDataManager.getGroupType(groupId);
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<PersonId> actualPeople = groupsDataManager.getPeopleForGroupType(testGroupTypeId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(testGroupTypeId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 3966867633401336210L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 4582534442214781870L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPeopleForGroupType(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getPersonCountForGroup", args = { GroupId.class })
	public void testGetPersonCountForGroup() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 1763603697244834578L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, MutableInteger> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
				expectedDataStructure.put(groupId, new MutableInteger());
			}

			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedDataStructure.get(groupId).increment();
				}
			}

			// show that number of people matches expectations

			for (GroupId groupId : groupIds) {
				int actualCount = groupsDataManager.getPersonCountForGroup(groupId);
				int expectedCount = expectedDataStructure.get(groupId).getValue();
				assertEquals(expectedCount, actualCount);
			}

		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 2981746189003482663L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPersonCountForGroup(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 3438693482743062795L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPersonCountForGroup(new GroupId(10000000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getPersonCountForGroupType", args = { GroupTypeId.class })
	public void testGetPersonCountForGroupType() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 5794665230130343350L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupTypeId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				expectedDataStructure.put(testGroupTypeId, new LinkedHashSet<>());
			}

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
			}

			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupTypeId = groupsDataManager.getGroupType(groupId);
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that number of people matches expectations
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int actualCount = groupsDataManager.getPersonCountForGroupType(testGroupTypeId);
				int expectedCount = expectedDataStructure.get(testGroupTypeId).size();
				assertEquals(expectedCount, actualCount);
			}

		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5829408984346963563L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPersonCountForGroupType(null);
			});
			
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 3769874950212938109L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.getPersonCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "groupTypeIdExists", args = { GroupTypeId.class })
	public void testGroupTypeIdExists() {

		Factory factory = GroupsTestPluginFactory.factory(10, 3, 5, 1172766215251823083L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				assertTrue(groupsDataManager.groupTypeIdExists(testGroupTypeId));
			}
			assertFalse(groupsDataManager.groupTypeIdExists(TestGroupTypeId.getUnknownGroupTypeId()));
		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "isPersonInGroup", args = { PersonId.class, GroupId.class })
	public void testIsPersonInGroup() {

		Factory factory = GroupsTestPluginFactory.factory(100, 0, 5, 8319627382232144625L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
				expectedDataStructure.put(groupId, new LinkedHashSet<>());
			}
			groupIds = new ArrayList<>(expectedDataStructure.keySet());

			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				for (PersonId personId : people) {
					if (expectedPeople.contains(personId)) {
						assertTrue(groupsDataManager.isPersonInGroup(personId, groupId));
					} else {
						assertFalse(groupsDataManager.isPersonInGroup(personId, groupId));
					}
				}
			}

		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/* precondition test: if the group id is null */
		ContractException contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 3623255510968295889L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.isPersonInGroup(new PersonId(0), null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		

		/* precondition test: if the group id is unknown */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 825983259283758140L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.isPersonInGroup(new PersonId(0), new GroupId(10000));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/* precondition test: if the person id is null */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 1009864608566885897L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.isPersonInGroup(null, new GroupId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		

		/* precondition test: if the person id is unknown */
		contractException = assertThrows(ContractException.class, () ->{
			Factory factory2 = GroupsTestPluginFactory.factory(100, 0, 5, 5275459426147794240L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.isPersonInGroup(new PersonId(1000000), new GroupId(0));
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		MutableObject<PersonId> pId = new MutableObject<>();

		/*
		 * Have the agent add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// add a new person
			PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
			pId.setValue(personId);
			// place the person in all groups
			for (GroupId groupId : groupsDataManager.getGroupIds()) {
				groupsDataManager.addPersonToGroup(personId, groupId);
			}

			// remove the person
			peopleDataManager.removePerson(personId);

		}));

		/*
		 * Have the agent show that the person is no longer present in the
		 * groups
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			GroupsDataManager personGroupDataManager = c.getDataManager(GroupsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			// get a list of all the groups
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

			// get the last added person
			PersonId personId = pId.getValue();

			// show that the person does not exist
			assertFalse(peopleDataManager.personExists(personId));

			// show that none of the groups contain the person
			for (GroupId groupId : groupIds) {
				List<PersonId> people = personGroupDataManager.getPeopleForGroup(groupId);
				assertFalse(people.contains(personId));
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 10, 2908277607868593618L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testGroupDataManagerInitialization() {
		long seed = 7212690164088198082L;

		int initialPopulation = 30;
		double expectedGroupsPerPerson = 3;
		double expectedPeoplePerGroup = 5;

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}
		int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
		int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

		GroupsPluginData groupsPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount, membershipCount, people, seed);

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add an agent that will demonstrate that the state of the data manager
		// reflects the contents of the group plugin data.

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager personGroupDataManager = c.getDataManager(GroupsDataManager.class);

			// show the groups are as expected
			List<GroupId> actualGroupIds = personGroupDataManager.getGroupIds();
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>(groupsPluginData.getGroupIds());
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

			// show that each group has the expected type
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				GroupTypeId expectedGroupTypeId = groupsPluginData.getGroupTypeId(groupId);
				GroupTypeId actualGroupTypeId = personGroupDataManager.getGroupType(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

			// show the group memberships are the same
			for (PersonId personId : peopleDataManager.getPeople()) {
				int expectedListSize = groupsPluginData.getGroupsForPerson(personId).size();
				Set<GroupId> expectedGroups = new LinkedHashSet<>(groupsPluginData.getGroupsForPerson(personId));

				int actualListSize = personGroupDataManager.getGroupsForPerson(personId).size();
				Set<GroupId> actualGroups = new LinkedHashSet<>(personGroupDataManager.getGroupsForPerson(personId));

				assertEquals(expectedListSize, actualListSize);
				assertEquals(expectedGroups, actualGroups);
			}

			// show that the group types are the same
			Set<GroupTypeId> expectedGroupTypeIds = groupsPluginData.getGroupTypeIds();
			Set<GroupTypeId> actualGroupTypeIds = personGroupDataManager.getGroupTypeIds();
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);

			// show that the property definitions are the same
			for (GroupTypeId groupTypeId : personGroupDataManager.getGroupTypeIds()) {
				Set<GroupPropertyId> expectedGroupPropertyIds = groupsPluginData.getGroupPropertyIds(groupTypeId);
				Set<GroupPropertyId> actualGroupPropertyIds = personGroupDataManager.getGroupPropertyIds(groupTypeId);
				assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
				for (GroupPropertyId groupPropertyId : actualGroupPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = groupsPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					PropertyDefinition actualPropertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// show that the group property values are the same
			Set<MultiKey> expectedGroupPropertyValues = new LinkedHashSet<>();
			for (GroupId groupId : groupsPluginData.getGroupIds()) {
				for (GroupPropertyValue groupPropertyValue : groupsPluginData.getGroupPropertyValues(groupId)) {
					MultiKey multiKey = new MultiKey(groupId, groupPropertyValue.groupPropertyId(), groupPropertyValue.value());
					expectedGroupPropertyValues.add(multiKey);
				}
			}

			Set<MultiKey> actualGroupPropertyValues = new LinkedHashSet<>();
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
				Set<GroupPropertyId> groupPropertyIds = personGroupDataManager.getGroupPropertyIds(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					Object actualValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					MultiKey multiKey = new MultiKey(groupId, groupPropertyId, actualValue);
					actualGroupPropertyValues.add(multiKey);
				}
			}

			assertEquals(expectedGroupPropertyValues, actualGroupPropertyValues);

		}));

		TestPluginData testPluginData = pluginBuilder.build();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(initialPopulation, expectedGroupsPerPerson, expectedPeoplePerGroup, seed, testPluginData).getPlugins();
		// build and execute the engine
		
		TestSimulation.builder().addPlugins(plugins).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "addGroupType", args = { GroupTypeId.class })
	public void testAddGroupType() {
		Set<GroupTypeId> expectedGroupTypeIds = new LinkedHashSet<>();
		Set<GroupTypeId> actualGroupTypeIds = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupTypeAdditionEvent.class).build(), (c2, e) -> {
				actualGroupTypeIds.add(e.groupTypeId());
			});
		}));
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
				expectedGroupTypeIds.add(testAuxiliaryGroupTypeId);
				groupsDataManager.addGroupType(testAuxiliaryGroupTypeId);
			}
		}));
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 5324000203933399469L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is already present
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 10, 6531281946960607184L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.addGroupType(TestGroupTypeId.GROUP_TYPE_1);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.DUPLICATE_GROUP_TYPE, contractException.getErrorType());

		// precondition test: if the group type id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 10, 2160259964191783423L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.addGroupType(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "defineGroupProperty", args = { GroupPropertyDefinitionInitialization.class })
	public void testDefineGroupProperty() {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have an observer observe new group property definitions being created
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(EventFilter.builder(GroupPropertyDefinitionEvent.class).build(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.groupTypeId(), e.groupPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor add group property definitions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
				groupsDataManager.addGroupType(testAuxiliaryGroupTypeId);
				for (TestAuxiliaryGroupPropertyId testAuxiliaryGroupPropertyId : TestAuxiliaryGroupPropertyId.getTestGroupPropertyIds(testAuxiliaryGroupTypeId)) {
					PropertyDefinition propertyDefinition = testAuxiliaryGroupPropertyId.getPropertyDefinition();
					GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
							GroupPropertyDefinitionInitialization	.builder()//
																	.setGroupTypeId(testAuxiliaryGroupTypeId)//
																	.setPropertyId(testAuxiliaryGroupPropertyId)//
																	.setPropertyDefinition(propertyDefinition)//
																	.build();

					groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
					MultiKey multiKey = new MultiKey(c.getTime(), testAuxiliaryGroupTypeId, testAuxiliaryGroupPropertyId);
					expectedObservations.add(multiKey);
					PropertyDefinition actualPropertyDefinition = groupsDataManager.getGroupPropertyDefinition(testAuxiliaryGroupTypeId, testAuxiliaryGroupPropertyId);
					assertEquals(propertyDefinition, actualPropertyDefinition);
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 7089101878335134553L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is unknown
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 10, 8347881582083929312L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
				GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
				GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
						GroupPropertyDefinitionInitialization	.builder()//
																.setGroupTypeId(groupTypeId)//
																.setPropertyId(groupPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();
				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition test: if the group property id is already known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 10, 3203453010151124575L, (c) -> {

				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
				groupsDataManager.addGroupType(groupTypeId);
				GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
				GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
						GroupPropertyDefinitionInitialization	.builder()//
																.setGroupTypeId(groupTypeId)//
																.setPropertyId(groupPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();

				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

		/*
		 * precondition test: if the groupPropertyDefinitionInitialization
		 * contains a property assignment for a group that does not exist.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(0, 3, 10, 1757700723640970863L, (c) -> {

				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
				groupsDataManager.addGroupType(groupTypeId);
				GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
				GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
						GroupPropertyDefinitionInitialization	.builder()//
																.setGroupTypeId(groupTypeId)//
																.setPropertyId(groupPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.addPropertyValue(new GroupId(0), true)//
																.build();
				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		/*
		 * if the groupPropertyDefinitionInitialization contains a property
		 * assignment for a group that is not of the correct group type.
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 10, 8541542687515887761L, (c) -> {

				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				groupsDataManager.addGroupType(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_2);
				GroupId groupId = groupsDataManager.addGroup(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_2);
				GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
				groupsDataManager.addGroupType(groupTypeId);
				GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
				GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
						GroupPropertyDefinitionInitialization	.builder()//
																.setGroupTypeId(groupTypeId)//
																.setPropertyId(groupPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.addPropertyValue(groupId, true)//
																.build();
				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.INCORRECT_GROUP_TYPE_ID, contractException.getErrorType());

		/*
		 * precondition test: if the groupPropertyDefinitionInitialization does
		 * not contain property value assignments for every extant group when
		 * the property definition does not contain a default value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(0, 3, 10, 244590355339669479L, (c) -> {

				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
				groupsDataManager.addGroupType(groupTypeId);
				groupsDataManager.addGroup(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1);
				GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).build();
				GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
						GroupPropertyDefinitionInitialization	.builder()//
																.setGroupTypeId(groupTypeId)//
																.setPropertyId(groupPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();
				groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupAdditionEvent", args = { GroupTypeId.class })
	public void testGetEventFilterForGroupAdditionEvent_GroupType() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		Set<TestGroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);

		// have the observer subscribe to group creation for the selected groups
		// types
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestGroupTypeId testGroupTypeId : selectedGroupTypes) {
				EventFilter<GroupAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupAdditionEvent(testGroupTypeId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(e.groupId());
				});
			}
		}));

		// have the actor create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				if (selectedGroupTypes.contains(groupTypeId)) {
					expectedObservations.add(groupId);
				}
			}

		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 5589772229734037226L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 7641347481169234356L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group type id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 5165611005555046251L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupAdditionEvent", args = {})
	public void testGetEventFilterForGroupAdditionEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation for the selected groups
		// types
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			EventFilter<GroupAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(e.groupId());
			});

		}));

		// have the actor create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				expectedObservations.add(groupId);
			}

		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(100, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 4873414306435646846L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 1195149554612948377L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group type id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 4.0, 10.0, 4200302716872534102L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupImminentRemovalEvent", args = { GroupTypeId.class })
	public void testGetEventFilterForGroupImminentRemovalEvent_GroupType() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<TestGroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an actor observe imminent group removals for the selected group
		// types
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestGroupTypeId testGroupTypeId : selectedGroupTypes) {
				EventFilter<GroupImminentRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupImminentRemovalEvent(testGroupTypeId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c.getTime(), e.groupId()));
				});
			}
		}));

		int comparisonDay = 100;

		// have an actor add a few new groups and immediately remove them
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			for (int i = 1; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
					StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
					GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
					GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
					groupsDataManager.removeGroup(groupId);
					if (selectedGroupTypes.contains(groupTypeId)) {
						expectedObservations.add(new MultiKey(c2.getTime(), groupId));
					}
				}, i);
			}
		}));

		// have the observer show that the expected and actual observations are
		// the same
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 5220753097952239863L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 9054394261904590543L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupImminentRemovalEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition test: if the group type id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 1762165471886047056L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupImminentRemovalEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupImminentRemovalEvent", args = { GroupId.class })
	public void testGetEventFilterForGroupImminentRemovalEvent_GroupId() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		List<GroupId> selectedGroups = new ArrayList<>();

		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		int groupCount = 30;

		// have an actor add a few new groups, selecting about half for later
		// removal
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			for (int i = 0; i < groupCount; i++) {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				if (randomGenerator.nextBoolean()) {
					selectedGroups.add(groupId);
				}
			}
		}));

		// have an actor observe imminent group removals for the selected group
		// ids
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : selectedGroups) {

				EventFilter<GroupImminentRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupImminentRemovalEvent(groupId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(e.groupId());
				});
			}
		}));

		// have an actor remove all the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			for (GroupId groupId : groupIds) {
				if (selectedGroups.contains(groupId)) {
					expectedObservations.add(groupId);
				}
				groupsDataManager.removeGroup(groupId);
			}
		}));

		// have the observer show that the expected and actual observations are
		// the same
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 8387884383247064182L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 4632329546403944029L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				groupsDataManager.getEventFilterForGroupImminentRemovalEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition test: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 5, 6422007986358180059L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(100000);
				groupsDataManager.getEventFilterForGroupImminentRemovalEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupImminentRemovalEvent", args = {})
	public void testGetEventFilterForGroupImminentRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		int groupCount = 30;

		// have an actor add a few new groups, selecting about half for later
		// removal
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			for (int i = 0; i < groupCount; i++) {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestGroupTypeId testGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				groupsDataManager.addGroup(testGroupTypeId);
			}
		}));

		// have an actor observe imminent group removals
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupImminentRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupImminentRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(e.groupId());
			});
		}));

		// have an actor remove all the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			for (GroupId groupId : groupIds) {
				if (randomGenerator.nextBoolean()) {
					expectedObservations.add(groupId);
					groupsDataManager.removeGroup(groupId);
				}
			}
		}));

		// have the observer show that the expected and actual observations are
		// the same
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 5769859947365341767L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = { GroupId.class })
	public void testGetEventFilterForGroupMembershipAdditionEvent_Group() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupId> selectedGroups = new LinkedHashSet<>();

		int groupCount = 20;
		/*
		 * have the actor create some groups and selected about half of them
		 * they will then be used for filtering membership addition observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				if (i % 2 == 0) {
					selectedGroups.add(groupId);
				}
			}
		}));

		// add an agent to observe the group membership additions to the
		// selected groups
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : selectedGroups) {
				EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		int comparisonDay = 100;

		// have the actor plan the addition of people to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			for (int i = 3; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
					PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
					GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

					// create a person
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());

					// select a group
					List<GroupId> groupIds = groupsDataManager.getGroupIds();
					GroupId groupId = groupIds.get(randomGenerator.nextInt(groupIds.size()));

					// add the person to the group
					groupsDataManager.addPersonToGroup(personId, groupId);

					if (selectedGroups.contains(groupId)) {
						expectedObservations.add(new MultiKey(c2.getTime(), groupId, personId));
					}
				}, i);
			}

		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 4356365020352320873L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3554135401743252689L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7801862262246131770L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = { GroupId.class, PersonId.class })
	public void testGetEventFilterForGroupMembershipAdditionEvent_Group_Person() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 20;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and selected about half of them
		 * they will then be used for filtering membership addition observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId selectedPersonId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					PersonId nonselectedPersonId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					selectedPairs.add(new Pair<>(groupId, selectedPersonId));
					nonSelectedPairs.add(new Pair<>(groupId, nonselectedPersonId));
				}
			}
		}));

		// add an agent to observe the group membership additions to the
		// selected pairs
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor plan the addition of people to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);

				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 4356365020352320873L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3554135401743252689L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7801862262246131770L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(1000000);
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3554135401743252689L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3554135401743252689L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = { GroupTypeId.class })
	public void testGetEventFilterForGroupMembershipAdditionEvent_GroupType() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);

		// add an agent to observe the group membership additions to the
		// selected groups
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupTypeId groupTypeId : selectedGroupTypes) {
				EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId);

				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.groupId(), e.personId()));

				});
			}
		}));

		int groupCount = 100;
		/*
		 * have the actor create some groups and add some people to them
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);

				for (int j = 0; j < 3; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					groupsDataManager.addPersonToGroup(personId, groupId);
					if (selectedGroupTypes.contains(groupTypeId)) {
						expectedObservations.add(new MultiKey(groupId, personId));
					}
				}

			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 8388981611967284165L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8821737193954784979L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group type id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8185554283901963798L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = { GroupTypeId.class, PersonId.class })
	public void testGetEventFilterForGroupMembershipAdditionEvent_GroupType_Person() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);
		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 100;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and people who will eventually move
		 * into those groups
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					if (selectedGroupTypes.contains(groupTypeId)) {
						selectedPairs.add(new Pair<>(groupId, personId));
					} else {
						nonSelectedPairs.add(new Pair<>(groupId, personId));
					}
				}
			}
		}));

		// add an agent to observe the group membership additions to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
				EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId, personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor add the people to the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);

				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 4289325374116700754L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8053944455114188764L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group id type is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 2656849630874291785L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupTypeId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 4303859622582466624L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 763446688943355921L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = { PersonId.class })
	public void testGetEventFilterForGroupMembershipAdditionEvent_Person() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 100;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and people who will eventually move
		 * into those groups
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					selectedPairs.add(new Pair<>(groupId, personId));
					personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					nonSelectedPairs.add(new Pair<>(groupId, personId));
				}
			}
		}));

		// add an agent to observe the group membership additions to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				PersonId personId = pair.getSecond();
				EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor add the people to the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);
				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 7894583767324913975L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8185554283901963798L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8821737193954784979L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipAdditionEvent(personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipAdditionEvent", args = {})
	public void testGetEventFilterForGroupMembershipAdditionEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupMembershipAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
			});
		}));

		/*
		 * have the actor create some groups and people
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < 100; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < 10; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					groupsDataManager.addPersonToGroup(personId, groupId);
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 8388981611967284165L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = { GroupId.class })
	public void testGetEventFilterForGroupMembershipRemovalEvent_Group() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupId> selectedGroups = new LinkedHashSet<>();

		int groupCount = 20;
		/*
		 * have the actor create some groups and selected about half of them
		 * they will then be used for filtering membership removal observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				if (i % 2 == 0) {
					selectedGroups.add(groupId);
				}
			}
		}));

		// add an agent to observe the group membership removals to the
		// selected groups
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : selectedGroups) {
				EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		int comparisonDay = 100;

		// have the actor plan the removal of people to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			for (int i = 3; i < comparisonDay; i++) {
				c.addPlan((c2) -> {
					RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
					PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
					GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

					// create a person
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());

					// select a group
					List<GroupId> groupIds = groupsDataManager.getGroupIds();
					GroupId groupId = groupIds.get(randomGenerator.nextInt(groupIds.size()));

					// add the person to the group
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupsDataManager.removePersonFromGroup(personId, groupId);

					if (selectedGroups.contains(groupId)) {
						expectedObservations.add(new MultiKey(c2.getTime(), groupId, personId));
					}
				}, i);
			}

		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(comparisonDay, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 1408892559376906541L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7647888786891229419L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 4061098775259808370L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = { GroupId.class, PersonId.class })
	public void testGetEventFilterForGroupMembershipRemovalEvent_Group_Person() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 20;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and selected about half of them
		 * they will then be used for filtering membership removal observations
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId selectedPersonId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					PersonId nonselectedPersonId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					selectedPairs.add(new Pair<>(groupId, selectedPersonId));
					nonSelectedPairs.add(new Pair<>(groupId, nonselectedPersonId));
				}
			}
		}));

		// add an agent to observe the group membership removals to the
		// selected pairs
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor plan the removal of people to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);
				groupsDataManager.removePersonFromGroup(personId, groupId);
				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 850494789248046062L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3817909950120643137L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 1158315623391808977L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(1000000);
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// precondition tests: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7381815331080809057L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 730725479830632841L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = { GroupTypeId.class })
	public void testGetEventFilterForGroupMembershipRemovalEvent_GroupType() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);

		// add an agent to observe the group membership removals to the
		// selected groups
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupTypeId groupTypeId : selectedGroupTypes) {
				EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId);

				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.groupId(), e.personId()));

				});
			}
		}));

		int groupCount = 100;
		/*
		 * have the actor create some groups and add some people to them
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);

				for (int j = 0; j < 3; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupsDataManager.removePersonFromGroup(personId, groupId);
					if (selectedGroupTypes.contains(groupTypeId)) {
						expectedObservations.add(new MultiKey(groupId, personId));
					}
				}

			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 5729813629540803760L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 1847985412434537556L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group type id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7066368881974432975L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = { GroupTypeId.class, PersonId.class })
	public void testGetEventFilterForGroupMembershipRemovalEvent_GroupType_Person() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<GroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);
		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 100;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and people who will eventually move
		 * into those groups
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					if (selectedGroupTypes.contains(groupTypeId)) {
						selectedPairs.add(new Pair<>(groupId, personId));
					} else {
						nonSelectedPairs.add(new Pair<>(groupId, personId));
					}
				}
			}
		}));

		// add an agent to observe the group membership removals to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
				EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId, personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor add the people to the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);
				groupsDataManager.removePersonFromGroup(personId, groupId);
				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 4777272524165165597L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 5144779074591935394L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the group id type is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 4113468214758049896L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				PersonId personId = new PersonId(0);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupTypeId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition tests: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 8361722504062590493L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 6049511004252974580L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(groupId, personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = { PersonId.class })
	public void testGetEventFilterForGroupMembershipRemovalEvent_Person() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		Set<Pair<GroupId, PersonId>> selectedPairs = new LinkedHashSet<>();
		Set<Pair<GroupId, PersonId>> nonSelectedPairs = new LinkedHashSet<>();

		int groupCount = 100;
		int peopleCount = 5;
		/*
		 * have the actor create some groups and people who will eventually move
		 * into those groups
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < groupCount; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < peopleCount; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					selectedPairs.add(new Pair<>(groupId, personId));
					personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					nonSelectedPairs.add(new Pair<>(groupId, personId));
				}
			}
		}));

		// add an agent to observe the group membership removals to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupId, PersonId> pair : selectedPairs) {
				PersonId personId = pair.getSecond();
				EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(personId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
				});
			}
		}));

		// have the actor add the people to the groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			List<Pair<GroupId, PersonId>> totalPairs = new ArrayList<>();
			totalPairs.addAll(selectedPairs);
			totalPairs.addAll(nonSelectedPairs);

			Collections.shuffle(totalPairs, new Random(randomGenerator.nextLong()));
			for (Pair<GroupId, PersonId> pair : totalPairs) {
				GroupId groupId = pair.getFirst();
				PersonId personId = pair.getSecond();
				groupsDataManager.addPersonToGroup(personId, groupId);
				groupsDataManager.removePersonFromGroup(personId, groupId);
				if (selectedPairs.contains(pair)) {
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 4102753717872340366L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition tests: if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 3402841194395285411L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PersonId personId = null;
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition tests: if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(30, 3, 10, 7511275143655411369L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				PersonId personId = new PersonId(1000000);
				groupsDataManager.getEventFilterForGroupMembershipRemovalEvent(personId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupMembershipRemovalEvent", args = {})
	public void testGetEventFilterForGroupMembershipRemovalEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership removals to the
		// selected pairs by way of the group type id
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupMembershipRemovalEvent> eventFilter = groupsDataManager.getEventFilterForGroupMembershipRemovalEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.groupId(), e.personId()));
			});
		}));

		/*
		 * have the actor create some groups and people
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (int i = 0; i < 100; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupsDataManager.addGroup(groupTypeId);
				for (int j = 0; j < 10; j++) {
					PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupsDataManager.removePersonFromGroup(personId, groupId);
					expectedObservations.add(new MultiKey(c.getTime(), groupId, personId));
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 3, 10, 617923429956310846L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyUpdateEvent", args = { GroupId.class })
	public void testGetEventFilterForGroupPropertyUpdateEvent_Group() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<GroupId> selectedGroups = new LinkedHashSet<>();

		// create an actor to select about half of the existing groups for the
		// observer to watch
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			// show that there are some groups
			assertTrue(groupIds.size() > 0);
			boolean shouldSelect = false;
			for (GroupId groupId : groupIds) {
				if (shouldSelect) {
					selectedGroups.add(groupId);
				}
				shouldSelect = !shouldSelect;
			}
		}));

		// create an actor to observe group properties being changed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupId groupId : selectedGroups) {
				EventFilter<GroupPropertyUpdateEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId()));
				});
			}
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, propertyValue);
						if (selectedGroups.contains(groupId)) {
							expectedObservations.add(new MultiKey(groupId, testGroupPropertyId));
						}
					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 5968311683269278335L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 862611649140739209L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition test: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 4350585872528673625L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(10000000);
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyUpdateEvent", args = { GroupPropertyId.class, GroupId.class })
	public void testGetEventFilterForGroupPropertyUpdateEvent_Property_Group() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<Pair<GroupPropertyId, GroupId>> selectedPairs = new LinkedHashSet<>();

		// create an actor to select about half of the existing groups and about
		// half of their properties for the
		// observer to watch
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			// show that there are some groups
			assertTrue(groupIds.size() > 0);

			for (GroupId groupId : groupIds) {
				if (randomGenerator.nextBoolean()) {
					GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
					Set<GroupPropertyId> groupPropertyIds = groupsDataManager.getGroupPropertyIds(groupTypeId);
					for (GroupPropertyId groupPropertyId : groupPropertyIds) {
						if (randomGenerator.nextBoolean()) {
							Pair<GroupPropertyId, GroupId> pair = new Pair<>(groupPropertyId, groupId);
							selectedPairs.add(pair);
						}
					}
				}
			}
		}));

		// create an actor to observe the selected group properties being
		// changed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (Pair<GroupPropertyId, GroupId> pair : selectedPairs) {
				GroupPropertyId groupPropertyId = pair.getFirst();
				GroupId groupId = pair.getSecond();
				EventFilter<GroupPropertyUpdateEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId()));
				});
			}
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, propertyValue);
						Pair<GroupPropertyId, GroupId> pair = new Pair<>(testGroupPropertyId, groupId);
						if (selectedPairs.contains(pair)) {
							expectedObservations.add(new MultiKey(groupId, testGroupPropertyId));
						}
					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 4475568313075757118L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 7069545924217450784L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				GroupPropertyId groupPropertyId = null;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the group property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 7612521001250321841L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(0);
				GroupPropertyId groupPropertyId = TestGroupPropertyId.getUnknownGroupPropertyId();
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the group id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 6231606808502548902L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = null;
				GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupId);

			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition test: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 7604482353903847440L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupId groupId = new GroupId(10000000);
				GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyUpdateEvent", args = { GroupTypeId.class })
	public void testGetEventFilterForGroupPropertyUpdateEvent_GroupType() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<GroupTypeId> selectedGroupTypes = new LinkedHashSet<>();
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_1);
		selectedGroupTypes.add(TestGroupTypeId.GROUP_TYPE_2);

		// create an actor to observe the selected group properties being
		// changed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (GroupTypeId groupTypeId : selectedGroupTypes) {
				EventFilter<GroupPropertyUpdateEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupTypeId);
				c.subscribe(eventFilter, (c2, e) -> {
					actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId()));
				});
			}
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, propertyValue);

						if (selectedGroupTypes.contains(testGroupTypeId)) {
							expectedObservations.add(new MultiKey(groupId, testGroupPropertyId));
						}
					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 7860201282796014649L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 3217133270896467859L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition test: if the group type id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 289438765224007761L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyUpdateEvent", args = { GroupPropertyId.class, GroupTypeId.class })
	public void testGetEventFilterForGroupPropertyUpdateEvent_GroupProperty_GroupType() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<Pair<GroupPropertyId, GroupTypeId>> selectedPairs = new LinkedHashSet<>();

		// create an actor to observe about half of all properties
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			Set<GroupTypeId> groupTypeIds = groupsDataManager.getGroupTypeIds();
			for (GroupTypeId groupTypeId : groupTypeIds) {
				Set<GroupPropertyId> groupPropertyIds = groupsDataManager.getGroupPropertyIds(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					if (randomGenerator.nextBoolean()) {
						Pair<GroupPropertyId, GroupTypeId> pair = new Pair<>(groupPropertyId, groupTypeId);
						selectedPairs.add(pair);
						EventFilter<GroupPropertyUpdateEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupTypeId);
						c.subscribe(eventFilter, (c2, e) -> {
							actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId()));
						});
					}
				}
			}
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, propertyValue);
						Pair<GroupPropertyId, GroupTypeId> pair = new Pair<>(testGroupPropertyId, testGroupTypeId);
						if (selectedPairs.contains(pair)) {
							expectedObservations.add(new MultiKey(groupId, testGroupPropertyId));
						}
					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 806128103582088681L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the group property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 3969612657033464070L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
				GroupPropertyId groupPropertyId = null;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupTypeId);
			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the group property id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 2325782745323432023L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
				GroupPropertyId groupPropertyId = TestGroupPropertyId.getUnknownGroupPropertyId();
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the group type id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 1401012088879526006L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = null;
				GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition test: if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = GroupsTestPluginFactory.factory(100, 3, 5, 7868034517160861554L, (c) -> {
				GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
				GroupTypeId groupTypeId = TestGroupTypeId.getUnknownGroupTypeId();
				GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
				groupsDataManager.getEventFilterForGroupPropertyUpdateEvent(groupPropertyId, groupTypeId);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyUpdateEvent", args = {})
	public void testGetEventFilterForGroupPropertyUpdateEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// create an actor to observe all group property changes
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupPropertyUpdateEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(e.groupId(), e.groupPropertyId()));
			});

		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupsDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object propertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupsDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, propertyValue);
						expectedObservations.add(new MultiKey(groupId, testGroupPropertyId));
					}
				}
			}
		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertFalse(expectedObservations.isEmpty());
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 2633645086420948220L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupPropertyDefinitionEvent", args = {})
	public void testGetEventFilterForGroupPropertyDefinitionEvent() {
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have an observer observe new group property definitions being created
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupPropertyDefinitionEvent> eventFilter = groupsDataManager.getEventFilterForGroupPropertyDefinitionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.groupTypeId(), e.groupPropertyId());
				actualObservations.add(multiKey);
			});
		}));

		// have an actor add group property definitions
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
				groupsDataManager.addGroupType(testAuxiliaryGroupTypeId);
				for (TestAuxiliaryGroupPropertyId testAuxiliaryGroupPropertyId : TestAuxiliaryGroupPropertyId.getTestGroupPropertyIds(testAuxiliaryGroupTypeId)) {
					PropertyDefinition propertyDefinition = testAuxiliaryGroupPropertyId.getPropertyDefinition();
					GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
							GroupPropertyDefinitionInitialization	.builder()//
																	.setGroupTypeId(testAuxiliaryGroupTypeId)//
																	.setPropertyId(testAuxiliaryGroupPropertyId)//
																	.setPropertyDefinition(propertyDefinition)//
																	.build();

					groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
					MultiKey multiKey = new MultiKey(c.getTime(), testAuxiliaryGroupTypeId, testAuxiliaryGroupPropertyId);
					expectedObservations.add(multiKey);
					PropertyDefinition actualPropertyDefinition = groupsDataManager.getGroupPropertyDefinition(testAuxiliaryGroupTypeId, testAuxiliaryGroupPropertyId);
					assertEquals(propertyDefinition, actualPropertyDefinition);
				}
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 2918652330043276295L, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = GroupsDataManager.class, name = "getEventFilterForGroupTypeAdditionEvent", args = {})
	public void testGetEventFilterForGroupTypeAdditionEvent() {
		Set<GroupTypeId> expectedGroupTypeIds = new LinkedHashSet<>();
		Set<GroupTypeId> actualGroupTypeIds = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have an actor observe the addition of new group types
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			EventFilter<GroupTypeAdditionEvent> eventFilter = groupsDataManager.getEventFilterForGroupTypeAdditionEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualGroupTypeIds.add(e.groupTypeId());
			});
		}));

		// have an actor add some new group types
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			for (TestAuxiliaryGroupTypeId testAuxiliaryGroupTypeId : TestAuxiliaryGroupTypeId.values()) {
				expectedGroupTypeIds.add(testAuxiliaryGroupTypeId);
				groupsDataManager.addGroupType(testAuxiliaryGroupTypeId);
			}
		}));
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertFalse(expectedGroupTypeIds.size() == 0);
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 7349170569580375646L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
