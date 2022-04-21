package plugins.groups;

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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.groups.events.GroupAdditionEvent;
import plugins.groups.events.GroupImminentRemovalEvent;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.events.GroupPropertyUpdateEvent;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PersonDataManager;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;
import util.wrappers.MutableObject;

@UnitTest(target = GroupDataManager.class)
public class AT_GroupDataManager {

	@Test
	@UnitTestMethod(name = "removeGroup", args = { GroupId.class })
	public void testRemoveGroup() {

		Set<GroupId> removedGroups = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);

				// show that the returned group id is not null
				assertNotNull(groupId);

				// show that the manager indicates the group id exists
				assertTrue(groupDataManager.groupExists(groupId));

				// remove the group
				groupDataManager.removeGroup(groupId);

				removedGroups.add(groupId);

			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// show that the group is no long present
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (GroupId groupId : removedGroups) {
				assertFalse(groupDataManager.groupExists(groupId));
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		GroupsActionSupport.testConsumers(30, 3, 5, 8204685090168544876L, testPlugin);
		
		
		
		//precondition test: if the group id is null
		GroupsActionSupport.testConsumer(30, 3, 5, 1164752712088660908L, (c)->{
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,()->groupDataManager.removeGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());			
		});
		
		//precondition test: if the group id is unknown
		GroupsActionSupport.testConsumer(30, 3, 5, 6321229743136171684L, (c)->{
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,()->groupDataManager.removeGroup(new GroupId(100000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());			
		});

		
	}

	@Test
	@UnitTestMethod(name = "removePersonFromGroup", args = { GroupId.class, PersonId.class })
	public void testRemovePersonFromGroup() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupMembershipAdditionEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getPersonId()));
			});

		}));

		// add an agent to add members to groups

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (GroupId groupId : groupDataManager.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(groupDataManager.getPeopleForGroup(groupId));
				int count = 0;
				for (PersonId personId : people) {
					if (!peopleForGroup.contains(personId)) {
						groupDataManager.addPersonToGroup(personId,groupId);
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		GroupsActionSupport.testConsumers(30, 3, 10, 2733223420384068616L, testPlugin);

		/* precondition test: if the person id is null */
		GroupsActionSupport.testConsumer(30, 3, 10, 667206327628089405L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removePersonFromGroup(null,groupId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		GroupsActionSupport.testConsumer(30, 3, 10, 283038490401536931L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removePersonFromGroup(new PersonId(10000),groupId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(30, 3, 10, 6913106996750459497L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removePersonFromGroup(personId,null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(30, 3, 10, 4632472396816795419L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removePersonFromGroup(personId,new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the person is not a member of the group */
		GroupsActionSupport.testConsumer(30, 3, 10, 8295961559327801013L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removePersonFromGroup(personId,groupId));
			assertEquals(GroupError.NON_GROUP_MEMBERSHIP, contractException.getErrorType());
		});

	}

	private static enum ExcludedPersonType {
		NULL, MEMBER, NON_MEMBER;
	}

	@Test
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new GroupDataManager(null));
		assertEquals(GroupError.NULL_GROUP_INITIALIZATION_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupConstructionInfo.class })
	public void testAddGroup_GroupConstructionInfo() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedGroupObservations = new LinkedHashSet<>();
		Set<GroupId> actualGroupObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupAdditionEvent.class, (c2, e) -> {
				actualGroupObservations.add(e.getGroupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
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
				GroupId groupId = groupDataManager.addGroup(groupConstructionInfo);

				expectedGroupObservations.add(groupId);

				// show that the group was created, has the correct type and has
				// the correct property values
				assertTrue(groupDataManager.groupExists(groupId));
				assertEquals(testGroupTypeId, groupDataManager.getGroupType(groupId));
				for (TestGroupPropertyId testGroupPropertyId : expectedPropertyValues.keySet()) {
					Object expectedValue = expectedPropertyValues.get(testGroupPropertyId);
					Object actaulValue = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
					assertEquals(expectedValue, actaulValue);
				}

			}
		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertTrue(expectedGroupObservations.size() > 0);
			assertEquals(expectedGroupObservations, actualGroupObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		GroupsActionSupport.testConsumers(40, 5.0, 20.0, 5865498314869329641L, testPlugin);
		
		// precondition test: if the group construction info is null
		GroupsActionSupport.testConsumer(40, 5.0, 20.0, 5229546252018518751L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupConstructionInfo nullGroupConstructionInfo = null;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addGroup(nullGroupConstructionInfo));
			assertEquals(GroupError.NULL_GROUP_CONSTRUCTION_INFO, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group type id contained in the group
		 * construction info is unknown
		 */
		GroupsActionSupport.testConsumer(40, 5.0, 20.0, 7404840971962130072L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.addGroup(GroupConstructionInfo.builder().setGroupTypeId(TestGroupTypeId.getUnknownGroupTypeId()).build()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/*
		 * precondition test:if a group property id contained in the group
		 * construction info is unknown
		 */
		GroupsActionSupport.testConsumer(40, 5.0, 20.0, 8782123343145389682L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			ContractException contractException = assertThrows(ContractException.class, () -> {
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.getUnknownGroupPropertyId(), 1)//
																					.build();//
				groupDataManager.addGroup(groupConstructionInfo);
			});
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if a group property value contained in the group
		 * construction info is incompatible with the corresponding property
		 * definition
		 */
		GroupsActionSupport.testConsumer(40, 5.0, 20.0, 8782123343145389682L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> {
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, 1)//
																					.build();//
				groupDataManager.addGroup(groupConstructionInfo);
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupTypeId.class })
	public void testAddGroup_GroupTypeId() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupAdditionEvent.class, (c2, e) -> {
				actualObservations.add(e.getGroupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			expectedObservations.add(groupId);

			groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			expectedObservations.add(groupId);

			groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			expectedObservations.add(groupId);

		}));

		// show that the group creations were observed
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 4.0, 10.0, 8137195527612056024L, testPlugin);

		// precondition tests
		GroupsActionSupport.testConsumer(30, 4.0, 10.0, 5229546252018518751L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupTypeId groupTypeId = null;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addGroup(groupTypeId));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		// precondition tests
		GroupsActionSupport.testConsumer(30, 4.0, 10.0, 5229546252018518751L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addGroup(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupMembershipAdditionEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getPersonId()));
			});

		}));

		// add an agent to add members to groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (GroupId groupId : groupDataManager.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(groupDataManager.getPeopleForGroup(groupId));
				int count = 0;

				for (PersonId personId : people) {

					if (!peopleForGroup.contains(personId)) {
						groupDataManager.addPersonToGroup(personId,groupId);
						assertTrue(groupDataManager.isPersonInGroup(personId,groupId));
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 10, 2733223420384068616L, testPlugin);

		// precondition tests: if the person id is null
		GroupsActionSupport.testConsumer(30, 3, 10, 2886293572900391101L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addPersonToGroup(null,groupId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		// precondition tests: if the person id is unknown
		GroupsActionSupport.testConsumer(30, 3, 10, 5604775963632692909L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addPersonToGroup(new PersonId(10000),groupId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		// precondition tests: if the group id is null
		GroupsActionSupport.testConsumer(30, 3, 10, 3853147120254074375L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addPersonToGroup(personId,null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		// precondition tests: if the group id is unknown
		GroupsActionSupport.testConsumer(30, 3, 10, 7259750239550962667L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addPersonToGroup(personId, new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		// precondition tests: if the person is already a member of the group
		GroupsActionSupport.testConsumer(30, 3, 10, 3285943689624298882L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			groupDataManager.addPersonToGroup(personId,groupId);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.addPersonToGroup(personId,groupId));
			assertEquals(GroupError.DUPLICATE_GROUP_MEMBERSHIP, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create containers for observations
		Set<GroupId> actualObservations = new LinkedHashSet<>();
		Set<GroupId> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group removals
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupImminentRemovalEvent.class, (c2, e) -> {
				actualObservations.add(e.getGroupId());
			});

		}));

		// add an agent that removes groups
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			assertTrue(groupIds.size() > 5);
			for (int i = 0; i < 5; i++) {
				GroupId groupId = groupIds.get(i);
				groupDataManager.removeGroup(groupId);
				expectedObservations.add(groupId);
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (GroupId groupId : expectedObservations) {
				assertFalse(groupDataManager.groupExists(groupId));
			}
		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(2, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 10, 1454752128175396298L, testPlugin);

		// precondition test: if the group id is null
		GroupsActionSupport.testConsumer(30, 3, 10, 7796647463624453709L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removeGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		// precondition test: if the group id is unknown
		GroupsActionSupport.testConsumer(30, 3, 10, 8368986779885621446L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.removeGroup(new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "groupExists", args = { GroupId.class })
	public void testGroupExists() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		List<GroupId> removedGroupIds = new ArrayList<>();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// add a group and show it exists
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				assertTrue(groupDataManager.groupExists(groupId));
				// remove the group and record it for later verification
				groupDataManager.removeGroup(groupId);
				removedGroupIds.add(groupId);
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (GroupId groupId : removedGroupIds) {
				// show that the removed groups don't exist
				assertFalse(groupDataManager.groupExists(groupId));
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 5, 2946647177720026906L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "sampleGroup", args = { GroupId.class, GroupSampler.class })
	public void testSampleGroup() {

		GroupsActionSupport.testConsumer(30, 3, 5, 9211292135944399530L, (c) -> {
			// establish data views and the lists to groups and people in the
			// simulation
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

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
						List<PersonId> peopleForGroup = groupDataManager.getPeopleForGroup(groupId);
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
						 * everyone in the gropu
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
							Optional<PersonId> optional = groupDataManager.sampleGroup(groupId, groupSampler);
							assertFalse(optional.isPresent());
						} else {
							// Draw a reasonable number of people from the group
							// and show that they are all eligible people
							for (int i = 0; i < eligiblePeople.size(); i++) {
								Optional<PersonId> optional = groupDataManager.sampleGroup(groupId, groupSampler);
								assertTrue(optional.isPresent());
								PersonId selectedPersonId = optional.get();
								assertTrue(eligiblePeople.contains(selectedPersonId));
							}
						}

					}
				}
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(30, 3, 5, 5080244401642933835L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.sampleGroup(null, GroupSampler.builder().build()));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(30, 3, 5, 8782123343145389682L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.sampleGroup(new GroupId(1000000), GroupSampler.builder().build()));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group sampler is null */
		GroupsActionSupport.testConsumer(30, 3, 5, 4175298436277522063L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.sampleGroup(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_SAMPLER, contractException.getErrorType());
		});

		/* precondition test: if the group sampler is null */
		GroupsActionSupport.testConsumer(30, 3, 5, 7404840971962130072L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.sampleGroup(new GroupId(0), GroupSampler.builder().setExcludedPersonId(new PersonId(1000000)).build()));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupPropertyUpdateEvent.class, (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getGroupPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue()));

			});
		}));

		// have an agent change a few group property values
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// show that there are some groups
			assertTrue(groupIds.size() > 0);

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = groupDataManager.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object currentValue = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
						Object expectedValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, expectedValue);
						Object actualValue = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(100, 3, 5, 4653012806568812031L, testPlugin);

		/* precondition test if the group id is null */
		GroupsActionSupport.testConsumer(100, 3, 5, 3285943689624298882L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(null, testGroupPropertyId, true));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 3, 5, 3853147120254074375L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(new GroupId(100000), testGroupPropertyId, true));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test if the group property id is null */
		GroupsActionSupport.testConsumer(100, 3, 5, 5118884606334935158L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(groupId, null, true));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test if the group property id is unknown */
		GroupsActionSupport.testConsumer(100, 3, 5, 6389640203066924425L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId(), true));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test if the property value is null */
		GroupsActionSupport.testConsumer(100, 3, 5, 6323361964403648167L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test if property value is incompatible with the
		 * corresponding property definition
		 */
		GroupsActionSupport.testConsumer(100, 3, 5, 3728888495166492963L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, 5));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

		/*
		 * precondition test if the corresponding property definition defines
		 * the property as immutable
		 */
		GroupsActionSupport.testConsumer(100, 3, 5, 7440937277837294440L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK;
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, true));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupCountForGroupType", args = { GroupTypeId.class })
	public void testGetGroupCountForGroupType() {

		GroupsActionSupport.testConsumer(300, 3, 5, 2910747162784803859L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct containers to hold expectations and actual counts
			Map<GroupTypeId, MutableInteger> actualCounts = new LinkedHashMap<>();
			Map<GroupTypeId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				actualCounts.put(testGroupTypeId, new MutableInteger());
				int count = groupDataManager.getGroupCountForGroupType(testGroupTypeId);
				expectedCounts.put(testGroupTypeId, new MutableInteger(count));
			}

			// poll through the groups and increment the corresponding counters
			for (GroupId groupId : groupIds) {
				actualCounts.get(groupDataManager.getGroupType(groupId)).increment();
			}
			// show that expectation were met
			assertEquals(expectedCounts, actualCounts);

		});

		/* precondition test: if the group type is null */
		GroupsActionSupport.testConsumer(300, 3, 5, 8342387507356594823L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupCountForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type is unknown */
		GroupsActionSupport.testConsumer(300, 3, 5, 4573510051341354320L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupCountForPerson", args = { PersonId.class })
	public void testGetGroupCountForPerson() {

		GroupsActionSupport.testConsumer(300, 3, 5, 6371809280692201768L, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct a container to hold expectations
			Map<PersonId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (PersonId personId : people) {
				expectedCounts.put(personId, new MutableInteger());
			}

			// poll through the groups and build the expectations
			for (GroupId groupId : groupIds) {
				List<PersonId> peopleInGroup = groupDataManager.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					expectedCounts.get(personId).increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				int expectedValue = expectedCounts.get(personId).getValue();
				int actualValue = groupDataManager.getGroupCountForPerson(personId);
				assertEquals(expectedValue, actualValue);
			}

		});

		/* precondition test: if the person id is null */
		GroupsActionSupport.testConsumer(300, 3, 5, 3920152432964044129L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupCountForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		GroupsActionSupport.testConsumer(300, 3, 5, 6739633613106510243L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupCountForPerson(new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupIds", args = {})
	public void testGetGroupIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 6455798573295403809L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
				expectedGroupIds.add(groupId);
			}

			// show that the group ids match the expected group ids
			List<GroupId> actualGroupIds = groupDataManager.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyDefinition", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyDefinition() {

		GroupsActionSupport.testConsumer(10, 0, 5, 4462836951642761957L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// show that the personGroupDataManger has the expected property
			// definitions
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = groupDataManager.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		});

		/* precondition test: if the group type id is null */
		GroupsActionSupport.testConsumer(10, 0, 5, 5959643517439959298L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type id is unknown */
		GroupsActionSupport.testConsumer(10, 0, 5, 9138791522018557245L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group property id is null */
		GroupsActionSupport.testConsumer(10, 0, 5, 9138791522018557245L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition test: if the group property id is unknown */
		GroupsActionSupport.testConsumer(10, 0, 5, 9138791522018557245L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/* precondition tests: if the group property id is unknown */
		GroupsActionSupport.testConsumer(10, 0, 5, 9138791522018557245L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyExists", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyExists() {

		GroupsActionSupport.testConsumer(10, 0, 5, 8858123829776885259L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// show that the personGroupDataManger returns true for the group
			// properties that should be present
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(groupDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			// show that other group properties do not exits
			assertFalse(groupDataManager.getGroupPropertyExists(null, null));
			assertFalse(groupDataManager.getGroupPropertyExists(null, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertFalse(groupDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), null));
			assertFalse(groupDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.getUnknownGroupPropertyId()));
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyIds", args = { GroupTypeId.class })
	public void testGetGroupPropertyIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 1205481410658607626L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// show that the personGroupDataManger returns the correct group
			// property ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				Set<TestGroupPropertyId> expectedPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
				Set<GroupPropertyId> actualPropertyIds = groupDataManager.getGroupPropertyIds(testGroupTypeId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

		});

		/* precondition test: if the group type id is null */
		GroupsActionSupport.testConsumer(10, 0, 5, 8498668590902665283L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyIds(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type id is unknown */
		GroupsActionSupport.testConsumer(10, 0, 5, 3809094168724176083L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyIds(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyTime", args = { GroupId.class, GroupPropertyId.class })
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
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = groupDataManager.getGroupIds();

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
				List<GroupId> groupsForGroupType = groupDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					double groupPropertyTime = groupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
					assertEquals(0.0, groupPropertyTime);
					expectedTimes.put(new MultiKey(groupId, testGroupPropertyId), new MutableDouble(1.0));
					groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values were all
		 * set at time = 1 and then set those properties to new values
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = groupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);

				mutableDouble.setValue(2.0);
				groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}

		}));

		/*
		 * At time = 3, have the agent show that the property values were all
		 * set at time = 2
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = groupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 5, 7313144886869436931L, testPlugin);

		/*
		 * precondition test: if the group id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 4540064428634658468L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyTime(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});
		/*
		 * precondition test: if the group id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 5080244401642933835L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyTime(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 4175298436277522063L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyTime(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is unknown
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 3557052948001350675L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyTime(groupId, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is unknown
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 7349200768842830982L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyTime(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class })
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
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<GroupId> groupIds = groupDataManager.getGroupIds();

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
				List<GroupId> groupsForGroupType = groupDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					Object value = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values and then set those properties to new values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);

				Object newValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				groupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, newValue);
				expectedValues.put(multiKey, newValue);
			}

		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = groupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 5, 649112407534985381L, testPlugin);

		/*
		 * precondition test: if the group id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 1071603906331418640L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyValue(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 7115328473763483106L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyValue(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is null
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 2444842488298604050L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyValue(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is unknown
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 1772465526096544640L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

		/*
		 * precondition test: if the group property id is unknown
		 */
		GroupsActionSupport.testConsumer(30, 3, 5, 6994832854288891414L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupPropertyValue(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupType", args = { GroupTypeId.class })
	public void testGetGroupsForGroupType() {

		GroupsActionSupport.testConsumer(10, 0, 5, 3948247844369837305L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
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
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
				expectedTypeToGroupIds.get(groupTypeId).add(groupId);
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<GroupId> actualGroupIds = groupDataManager.getGroupsForGroupType(testGroupTypeId);
				Set<GroupId> expectedGroupIds = expectedTypeToGroupIds.get(testGroupTypeId);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

		/* precondition test: if the group type id is null */
		GroupsActionSupport.testConsumer(10, 0, 5, 2441670244909950371L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type id is unknown */
		GroupsActionSupport.testConsumer(10, 0, 5, 8938160844024056358L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupsForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4847183275886938594L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<MultiKey, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupDataManager.addPersonToGroup(personId,groupId);
					GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
					MultiKey multiKey = new MultiKey(groupTypeId, personId);
					Set<GroupId> groups = expectedDataStructure.get(multiKey);
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (PersonId personId : people) {
					List<GroupId> actualGroupIds = groupDataManager.getGroupsForGroupTypeAndPerson(testGroupTypeId, personId);
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					Set<GroupId> expectedGroupIds = expectedDataStructure.get(multiKey);
					assertEquals(expectedGroupIds.size(), actualGroupIds.size());
					assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
				}
			}
		});

		/* precondition test: if the person id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 5248499346426314201L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 1445347293441431961L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 1445347293441431961L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForGroupTypeAndPerson(null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group type id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 1445347293441431961L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupDataManager.getGroupsForGroupTypeAndPerson(TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForPerson", args = { PersonId.class })
	public void testGetGroupsForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1095418957424488372L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupDataManager.addPersonToGroup(personId,groupId);
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
				List<GroupId> actualGroupIds = groupDataManager.getGroupsForPerson(personId);
				Set<GroupId> expectedGroupIds = expectedDataStructure.get(personId);
				assertNotNull(expectedGroupIds);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

		});

		/* precondition tests: if the person id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 4037186565913379048L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition tests: if the person id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 5901067879853942202L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupsForPerson(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupType", args = { GroupId.class })
	public void testGetGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5910635654466929788L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
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
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
				expectedDataStructure.put(groupId, groupTypeId);
			}

			// show that the group have the expected types
			for (GroupId groupId : expectedDataStructure.keySet()) {
				GroupTypeId actualGroupTypeId = groupDataManager.getGroupType(groupId);
				GroupTypeId expectedGroupTypeId = expectedDataStructure.get(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 4697608906151940983L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupType(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 5074440747148359344L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupType(new GroupId(100000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeCountForPersonId", args = { PersonId.class })
	public void testGetGroupTypeCountForPersonId() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1561008711822589907L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Integer> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					List<GroupId> groupsForGroupType = groupDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					groupDataManager.addPersonToGroup(personId,groupId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				int actualCount = groupDataManager.getGroupTypeCountForPersonId(personId);
				Integer expectedCount = expectedDataStructure.get(personId);
				assertEquals(expectedCount.intValue(), actualCount);
			}

		});

		/* precondition test: if the person id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 2733980118690868605L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupTypeCountForPersonId(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 7646517978722507404L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupTypeCountForPersonId(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {
		GroupsActionSupport.testConsumer(10, 3, 5, 1999263877784730672L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			// show that the group ids match the expected group ids
			Set<GroupTypeId> groupTypeIds = groupDataManager.getGroupTypeIds();
			assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupTypeIds);
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypesForPerson", args = { PersonId.class })
	public void testGetGroupTypesForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 2999448198567478958L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupTypeId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					List<GroupId> groupsForGroupType = groupDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					groupDataManager.addPersonToGroup(personId,groupId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				List<GroupTypeId> actualGroupTypesForPerson = groupDataManager.getGroupTypesForPerson(personId);
				Set<GroupTypeId> expectedGroupTypesForPerson = expectedDataStructure.get(personId);
				assertEquals(expectedGroupTypesForPerson.size(), actualGroupTypesForPerson.size());
				assertEquals(expectedGroupTypesForPerson, new LinkedHashSet<>(actualGroupTypesForPerson));
			}

		});

		/* precondition tests if the person id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 5882134079494817898L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupTypesForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition tests if the person id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 4598510399026722120L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getGroupTypesForPerson(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroup", args = { GroupId.class })
	public void testGetPeopleForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4550534695972929193L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupDataManager.addPersonToGroup(personId,groupId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				List<PersonId> actualPeople = groupDataManager.getPeopleForGroup(groupId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 1054111866998260759L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPeopleForGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 976385337250084757L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPeopleForGroup(new GroupId(100000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroupType", args = { GroupTypeId.class })
	public void testGetPeopleForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8576174021026036673L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupTypeId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupTypeId = groupDataManager.getGroupType(groupId);
					groupDataManager.addPersonToGroup(personId,groupId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<PersonId> actualPeople = groupDataManager.getPeopleForGroupType(testGroupTypeId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(testGroupTypeId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 3966867633401336210L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPeopleForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 4582534442214781870L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPeopleForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroup", args = { GroupId.class })
	public void testGetPersonCountForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1763603697244834578L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, MutableInteger> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupDataManager.addPersonToGroup(personId,groupId);
					expectedDataStructure.get(groupId).increment();
				}
			}

			// show that number of people matches expectations

			for (GroupId groupId : groupIds) {
				int actualCount = groupDataManager.getPersonCountForGroup(groupId);
				int expectedCount = expectedDataStructure.get(groupId).getValue();
				assertEquals(expectedCount, actualCount);
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 2981746189003482663L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPersonCountForGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 3438693482743062795L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPersonCountForGroup(new GroupId(10000000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroupType", args = { GroupTypeId.class })
	public void testGetPersonCountForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5794665230130343350L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

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
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupTypeId = groupDataManager.getGroupType(groupId);
					groupDataManager.addPersonToGroup(personId,groupId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that number of people matches expectations
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int actualCount = groupDataManager.getPersonCountForGroupType(testGroupTypeId);
				int expectedCount = expectedDataStructure.get(testGroupTypeId).size();
				assertEquals(expectedCount, actualCount);
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 5829408984346963563L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPersonCountForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 3769874950212938109L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.getPersonCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "groupTypeIdExists", args = { GroupTypeId.class })
	public void testGroupTypeIdExists() {

		GroupsActionSupport.testConsumer(10, 3, 5, 1172766215251823083L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				assertTrue(groupDataManager.groupTypeIdExists(testGroupTypeId));
			}
			assertFalse(groupDataManager.groupTypeIdExists(TestGroupTypeId.getUnknownGroupTypeId()));
		});

	}

	@Test
	@UnitTestMethod(name = "isPersonInGroup", args = { GroupId.class, PersonId.class })
	public void testIsPersonInGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8319627382232144625L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = groupDataManager.addGroup(groupTypeId);
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
					groupDataManager.addPersonToGroup(personId,groupId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				for (PersonId personId : people) {
					if (expectedPeople.contains(personId)) {
						assertTrue(groupDataManager.isPersonInGroup(personId,groupId));
					} else {
						assertFalse(groupDataManager.isPersonInGroup(personId,groupId));
					}
				}
			}

		});

		/* precondition test: if the group id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 3623255510968295889L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.isPersonInGroup(new PersonId(0),null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the group id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 825983259283758140L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.isPersonInGroup(new PersonId(0),new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is null */
		GroupsActionSupport.testConsumer(100, 0, 5, 1009864608566885897L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.isPersonInGroup(null,new GroupId(0) ));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		/* precondition test: if the person id is unknown */
		GroupsActionSupport.testConsumer(100, 0, 5, 5275459426147794240L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> groupDataManager.isPersonInGroup(new PersonId(1000000),new GroupId(0)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

	}

	private void testEventLabeler(ActorContext c, EventLabeler<?> eventLabeler) {
		assertNotNull(eventLabeler);
		ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
		assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupAdditionEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 6706902549572603852L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			testEventLabeler(c, GroupAdditionEvent.getEventLabelerForGroupType(groupDataManager));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupImminentRemovalEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 6196206924587095446L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			
			testEventLabeler(c, GroupImminentRemovalEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupImminentRemovalEvent.getEventLabelerForGroupType(groupDataManager));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupPropertyUpdateEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 4869845127685024578L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			testEventLabeler(c, GroupPropertyUpdateEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupPropertyUpdateEvent.getEventLabelerForGroupAndProperty());
			testEventLabeler(c, GroupPropertyUpdateEvent.getEventLabelerForGroupType(groupDataManager));
			testEventLabeler(c, GroupPropertyUpdateEvent.getEventLabelerForGroupTypeAndProperty(groupDataManager));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupMembershipAdditionEventLabelers() {
		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 5331119358636307434L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			
			testEventLabeler(c, GroupMembershipAdditionEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupMembershipAdditionEvent.getEventLabelerForGroupAndPerson());
			testEventLabeler(c, GroupMembershipAdditionEvent.getEventLabelerForGroupType(groupDataManager));
			testEventLabeler(c, GroupMembershipAdditionEvent.getEventLabelerForGroupTypeAndPerson(groupDataManager));
			testEventLabeler(c, GroupMembershipAdditionEvent.getEventLabelerForPerson());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupMembershipRemovalEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 774686050832969915L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			testEventLabeler(c, GroupMembershipRemovalEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupMembershipRemovalEvent.getEventLabelerForGroupAndPerson());
			testEventLabeler(c, GroupMembershipRemovalEvent.getEventLabelerForGroupType(groupDataManager));
			testEventLabeler(c, GroupMembershipRemovalEvent.getEventLabelerForGroupTypeAndPerson(groupDataManager));
			testEventLabeler(c, GroupMembershipRemovalEvent.getEventLabelerForPerson());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testBulkPersonAdditionEvent() {
		
		// create structures to hold observations
		Set<GroupId> expectedGroupObservations = new LinkedHashSet<>();
		Set<GroupId> actualGroupObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add an observer that will observe the new groups being created as
		// well as the people being added to the groups

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GroupAdditionEvent.class, (c2, e) -> {
				actualGroupObservations.add(e.getGroupId());
			});

		}));

		/*
		 * Have an agent add several people via bulk person creation that
		 * includes group associations.
		 *
		 * Show that groups were added and the people are in the new groups and
		 * thus the resolver must have handled the corresponding
		 * BulkPersonAdditionEvent.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// establish data views and how many people and groups already exist
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			int personIdOffest = personDataManager.getPopulationCount();

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			int groupIdOffset = groupDataManager.getGroupIds().size();

			// create a bulk construction event with 5 new people and 4 new
			// groups
			BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
			PersonConstructionData.Builder peopleBuilder = PersonConstructionData.builder();
			BulkGroupMembershipData.Builder membershipBuilder = BulkGroupMembershipData.builder();

			// add 5 people
			builder.add(peopleBuilder.build());
			builder.add(peopleBuilder.build());
			builder.add(peopleBuilder.build());
			builder.add(peopleBuilder.build());
			builder.add(peopleBuilder.build());

			// add 4 groups
			membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_1);

			// record the expected group observations
			expectedGroupObservations.add(new GroupId(groupIdOffset + 0));
			expectedGroupObservations.add(new GroupId(groupIdOffset + 1));
			expectedGroupObservations.add(new GroupId(groupIdOffset + 2));
			expectedGroupObservations.add(new GroupId(groupIdOffset + 3));

			// assign the people to the groups
			membershipBuilder.addPersonToGroup(0, 0);
			membershipBuilder.addPersonToGroup(0, 1);
			membershipBuilder.addPersonToGroup(0, 2);
			membershipBuilder.addPersonToGroup(2, 0);
			membershipBuilder.addPersonToGroup(2, 1);
			membershipBuilder.addPersonToGroup(3, 2);

			builder.addAuxiliaryData(membershipBuilder.build());
			BulkPersonConstructionData bulkPersonConstructionData = builder.build();
			personDataManager.addBulkPeople(bulkPersonConstructionData);

			// show that the groups exist and have the appropriate people

			GroupId groupId = new GroupId(0 + groupIdOffset);
			assertTrue(groupDataManager.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_1, groupDataManager.getGroupType(groupId));
			assertEquals(2, groupDataManager.getPersonCountForGroup(groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 0),groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 2),groupId));

			groupId = new GroupId(1 + groupIdOffset);
			assertTrue(groupDataManager.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_2, groupDataManager.getGroupType(groupId));
			assertEquals(2, groupDataManager.getPersonCountForGroup(groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 0),groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 2),groupId));

			groupId = new GroupId(2 + groupIdOffset);
			assertTrue(groupDataManager.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_3, groupDataManager.getGroupType(groupId));
			assertEquals(2, groupDataManager.getPersonCountForGroup(groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 0),groupId));
			assertTrue(groupDataManager.isPersonInGroup(new PersonId(personIdOffest + 3),groupId));

			groupId = new GroupId(3 + groupIdOffset);
			assertTrue(groupDataManager.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_1, groupDataManager.getGroupType(groupId));
			assertEquals(0, groupDataManager.getPersonCountForGroup(groupId));

		}));

		// have the observer verify the observations
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedGroupObservations, actualGroupObservations);
		}));

		/*
		 * Initialize with some people and groups
		 */
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(10, 3, 5, 4483791915301705904L, testPlugin);

		/*
		 * precondition tests if the BulkMembership data exists and contains an
		 * unknown person id
		 */
		GroupsActionSupport.testConsumer(10, 3, 5, 3738915539234400027L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
				PersonConstructionData.Builder peopleBuilder = PersonConstructionData.builder();
				BulkGroupMembershipData.Builder membershipBuilder = BulkGroupMembershipData.builder();
				builder.add(peopleBuilder.build());
				membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				membershipBuilder.addPersonToGroup(1, 0);
				builder.addAuxiliaryData(membershipBuilder.build());
				BulkPersonConstructionData bulkPersonConstructionData = builder.build();
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				personDataManager.addBulkPeople(bulkPersonConstructionData);
			});
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		});

		/*
		 * precondition tests if the BulkMembership data exists and contains an
		 * unknown group type id
		 */
		GroupsActionSupport.testConsumer(10, 3, 5, 5431888419388886834L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
				PersonConstructionData.Builder peopleBuilder = PersonConstructionData.builder();
				BulkGroupMembershipData.Builder membershipBuilder = BulkGroupMembershipData.builder();
				builder.add(peopleBuilder.build());
				membershipBuilder.addGroup(TestGroupTypeId.getUnknownGroupTypeId());
				builder.addAuxiliaryData(membershipBuilder.build());
				BulkPersonConstructionData bulkPersonConstructionData = builder.build();
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				personDataManager.addBulkPeople(bulkPersonConstructionData);
			});
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testPersonImminentRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		MutableObject<PersonId> pId = new MutableObject<>();

		/*
		 * Have the agent add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// add a new person
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());
			pId.setValue(personId);
			// place the person in all groups
			for (GroupId groupId : groupDataManager.getGroupIds()) {
				groupDataManager.addPersonToGroup(personId,groupId);
			}

			// remove the person
			personDataManager.removePerson(personId);

		}));

		/*
		 * Have the agent show that the person is no longer present in the
		 * groups
		 * 
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {

			GroupDataManager personGroupDataManager = c.getDataManager(GroupDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();

			// get a list of all the groups
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

			// get the last added person
			PersonId personId = pId.getValue();

			// show that the person does not exist
			assertFalse(personDataManager.personExists(personId));

			// show that none of the groups contain the person
			for (GroupId groupId : groupIds) {
				List<PersonId> people = personGroupDataManager.getPeopleForGroup(groupId);
				assertFalse(people.contains(personId));
			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GroupsActionSupport.testConsumers(30, 3, 10, 2908277607868593618L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupPluginData.class })
	public void testGroupDataManagerInitialization() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7212690164088198082L);

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

		Builder builder = Simulation.builder();

		// add the group plugin
		GroupPluginData.Builder groupBuilder = GroupPluginData.builder();
		// add group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupBuilder.addGroupTypeId(testGroupTypeId);
		}
		// define group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		// add the groups and set their properties
		List<GroupId> groups = new ArrayList<>();
		for (int i = 0; i < groupCount; i++) {
			GroupId groupId = new GroupId(i);
			groups.add(groupId);
			TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			groupBuilder.addGroup(groupId, groupTypeId);
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(groupTypeId)) {
				groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}
		}

		// add the group memberships
		Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
		while (groupMemeberships.size() < membershipCount) {
			PersonId personId = people.get(randomGenerator.nextInt(people.size()));
			GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
			groupMemeberships.add(new MultiKey(groupId, personId));
		}

		for (MultiKey multiKey : groupMemeberships) {
			GroupId groupId = multiKey.getKey(0);
			PersonId personId = multiKey.getKey(1);
			groupBuilder.addPersonToGroup(groupId, personId);
		}

		GroupPluginData groupPluginData = groupBuilder.build();

		builder.addPlugin(GroupPlugin.getGroupPlugin(groupPluginData));

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			bulkBuilder.add(PersonConstructionData.builder().build());
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		builder.addPlugin(peoplePlugin);

		// add the report plugin
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// add an agent that will demonstrate that the state of the data manager
		// reflects the contents of the group plugin data.

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupDataManager personGroupDataManager = c.getDataManager(GroupDataManager.class).get();

			// show the groups are as expected
			List<GroupId> actualGroupIds = personGroupDataManager.getGroupIds();
			Set<GroupId> expectedGroupIds = groupPluginData.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

			// show that each group has the expected type
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				GroupTypeId expectedGroupTypeId = groupPluginData.getGroupTypeId(groupId);
				GroupTypeId actualGroupTypeId = personGroupDataManager.getGroupType(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

			// show the group memberships are the same
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				Set<PersonId> expectedPeople = groupPluginData.getGroupMembers(groupId);
				List<PersonId> actualPeople = personGroupDataManager.getPeopleForGroup(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

			// show that the group types are the same
			Set<GroupTypeId> expectedGroupTypeIds = groupPluginData.getGroupTypeIds();
			Set<GroupTypeId> actualGroupTypeIds = personGroupDataManager.getGroupTypeIds();
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);

			// show that the property definitions are the same
			for (GroupTypeId groupTypeId : personGroupDataManager.getGroupTypeIds()) {
				Set<GroupPropertyId> expectedGroupPropertyIds = groupPluginData.getGroupPropertyIds(groupTypeId);
				Set<GroupPropertyId> actualGroupPropertyIds = personGroupDataManager.getGroupPropertyIds(groupTypeId);
				assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
				for (GroupPropertyId groupPropertyId : actualGroupPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = groupPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					PropertyDefinition actualPropertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// show that the group property values are the same
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
				Set<GroupPropertyId> groupPropertyIds = personGroupDataManager.getGroupPropertyIds(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					Object expectedValue = groupPluginData.getGroupPropertyValue(groupId, groupPropertyId);
					Object actualValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					assertEquals(expectedValue, actualValue);

				}

			}

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

}
