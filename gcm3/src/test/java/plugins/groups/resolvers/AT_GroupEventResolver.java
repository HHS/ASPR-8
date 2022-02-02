package plugins.groups.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Event;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ResolverActionPlan;
import plugins.components.ComponentPlugin;
import plugins.groups.GroupPlugin;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupConstructionEvent;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.events.observation.GroupCreationObservationEvent;
import plugins.groups.events.observation.GroupImminentRemovalObservationEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.events.observation.GroupPropertyChangeObservationEvent;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupEventResolver.class)
public class AT_GroupEventResolver {

	@Test
	@UnitTestConstructor(args = { GroupInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new GroupEventResolver(null));
		assertEquals(GroupError.NULL_GROUP_INITIALIZATION_DATA, contractException.getErrorType());
	}

	private void testEventLabeler(AgentContext c, EventLabeler<?> eventLabeler) {
		assertNotNull(eventLabeler);
		ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
		assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupMembershipAdditionObservationEventLabelers() {
		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 5331119358636307434L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForAll());
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForGroupAndPerson());
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForGroupType(personGroupDataView));
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForGroupTypeAndPerson(personGroupDataView));
			testEventLabeler(c, GroupMembershipAdditionObservationEvent.getEventLabelerForPerson());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupMembershipRemovalObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 774686050832969915L, (c) -> {
			
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForAll());
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForGroupAndPerson());
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForGroupType(personGroupDataView));
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForGroupTypeAndPerson(personGroupDataView));
			testEventLabeler(c, GroupMembershipRemovalObservationEvent.getEventLabelerForPerson());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupCreationObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 6706902549572603852L, (c) -> {
			
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			
			testEventLabeler(c, GroupCreationObservationEvent.getEventLabelerForAll());
			testEventLabeler(c, GroupCreationObservationEvent.getEventLabelerForGroupType(personGroupDataView));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupImminentRemovalObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 6196206924587095446L, (c) -> {
			
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			
			testEventLabeler(c, GroupImminentRemovalObservationEvent.getEventLabelerForAll());
			testEventLabeler(c, GroupImminentRemovalObservationEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupImminentRemovalObservationEvent.getEventLabelerForGroupType(personGroupDataView));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupPropertyChangeObservationEventLabelers() {

		// Have the agent attempt to add the event labelers and show that a
		// contract exception is thrown, indicating that the labelers were
		// previously added by the resolver.

		GroupsActionSupport.testConsumer(100, 3, 5, 4869845127685024578L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			
			testEventLabeler(c, GroupPropertyChangeObservationEvent.getEventLabelerForAll());
			testEventLabeler(c, GroupPropertyChangeObservationEvent.getEventLabelerForGroup());
			testEventLabeler(c, GroupPropertyChangeObservationEvent.getEventLabelerForGroupAndProperty());
			testEventLabeler(c, GroupPropertyChangeObservationEvent.getEventLabelerForGroupType(personGroupDataView));
			testEventLabeler(c, GroupPropertyChangeObservationEvent.getEventLabelerForGroupTypeAndProperty(personGroupDataView));
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupConstructionEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers for observations
		Set<GroupId> expectedGroupObservations = new LinkedHashSet<>();
		Set<GroupId> actualGroupObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualGroupObservations.add(e.getGroupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
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
				c.resolveEvent(new GroupConstructionEvent(groupConstructionInfo));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
				expectedGroupObservations.add(personGroupDataView.getLastIssuedGroupId().get());

				// show that the group was created, has the correct type and has
				// the correct property values
				assertTrue(personGroupDataView.groupExists(groupId));
				assertEquals(testGroupTypeId, personGroupDataView.getGroupType(groupId));
				for (TestGroupPropertyId testGroupPropertyId : expectedPropertyValues.keySet()) {
					Object expectedValue = expectedPropertyValues.get(testGroupPropertyId);
					Object actaulValue = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
					assertEquals(expectedValue, actaulValue);
				}

			}
		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			// if the group construction info is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupConstructionEvent(null)));
			assertEquals(GroupError.NULL_GROUP_CONSTRUCTION_INFO, contractException.getErrorType());

			// if the group type id contained in the group construction info is
			// unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new GroupConstructionEvent(GroupConstructionInfo.builder().setGroupTypeId(TestGroupTypeId.getUnknownGroupTypeId()).build())));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

			// if a group property id contained in the group construction info
			// is unknown
			contractException = assertThrows(ContractException.class, () -> {
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.getUnknownGroupPropertyId(), 1)//
																					.build();//
				c.resolveEvent(new GroupConstructionEvent(groupConstructionInfo));
			});
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if a group property value contained in the group construction
			// info is incompatible with the corresponding property definition
			contractException = assertThrows(ContractException.class, () -> {
				GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo	.builder()//
																					.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
																					.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, 1)//
																					.build();//
				c.resolveEvent(new GroupConstructionEvent(groupConstructionInfo));
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		}));

		// show that the group creations were observed
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertTrue(expectedGroupObservations.size() > 0);
			assertEquals(expectedGroupObservations, actualGroupObservations);
		}));

		GroupsActionSupport.testConsumers(40, 5, 20, 5865498314869329641L, pluginBuilder.build());
	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupCreationEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers for observations
		Set<GroupId> expectedObservations = new LinkedHashSet<>();
		Set<GroupId> actualObservations = new LinkedHashSet<>();

		// have the observer subscribe to group creation
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(e.getGroupId());
			});

		}));

		// have the agent create add a few groups and collect expected
		// observations
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			expectedObservations.add(personGroupDataView.getLastIssuedGroupId().get());

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			expectedObservations.add(personGroupDataView.getLastIssuedGroupId().get());

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));
			expectedObservations.add(personGroupDataView.getLastIssuedGroupId().get());
		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupCreationEvent(null)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.getUnknownGroupTypeId())));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		}));

		// show that the group creations were observed
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		GroupsActionSupport.testConsumers(30, 4, 10, 8137195527612056024L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testBulkPersonCreationObservationEvent() {
		// create structures to hold observations
		Set<GroupId> expectedGroupObservations = new LinkedHashSet<>();
		Set<GroupId> actualGroupObservations = new LinkedHashSet<>();

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an observer that will observe the new groups being created as
		// well as the people being added to the groups
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualGroupObservations.add(e.getGroupId());
			});

		}));

		/*
		 * Have an agent add several people via bulk person creation that
		 * includes group associations.
		 * 
		 * Show that groups were added and the people are in the new groups and
		 * thus the resolver must have handled the corresponding
		 * BulkPersonCreationObservationEvent.
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			// establish data views and how many people and groups already exist
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			int personIdOffest = personDataView.getPopulationCount();

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			int groupIdOffset = personGroupDataView.getGroupIds().size();

			// create a bulk construction event with 5 new people and 4 new
			// groups
			BulkPersonContructionData.Builder builder = BulkPersonContructionData.builder();
			PersonContructionData.Builder peopleBuilder = PersonContructionData.builder();
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
			c.resolveEvent(new BulkPersonCreationEvent(builder.build()));

			// show that the groups exist and have the appropriate people

			GroupId groupId = new GroupId(0 + groupIdOffset);
			assertTrue(personGroupDataView.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_1, personGroupDataView.getGroupType(groupId));
			assertEquals(2, personGroupDataView.getPersonCountForGroup(groupId));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 0)));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 2)));

			groupId = new GroupId(1 + groupIdOffset);
			assertTrue(personGroupDataView.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_2, personGroupDataView.getGroupType(groupId));
			assertEquals(2, personGroupDataView.getPersonCountForGroup(groupId));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 0)));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 2)));

			groupId = new GroupId(2 + groupIdOffset);
			assertTrue(personGroupDataView.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_3, personGroupDataView.getGroupType(groupId));
			assertEquals(2, personGroupDataView.getPersonCountForGroup(groupId));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 0)));
			assertTrue(personGroupDataView.isGroupMember(groupId, new PersonId(personIdOffest + 3)));

			groupId = new GroupId(3 + groupIdOffset);
			assertTrue(personGroupDataView.groupExists(groupId));
			assertEquals(TestGroupTypeId.GROUP_TYPE_1, personGroupDataView.getGroupType(groupId));
			assertEquals(0, personGroupDataView.getPersonCountForGroup(groupId));

		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			/*
			 * if the event exists and contains an unknown person id -- this is
			 * not possible to test since the person plugin will prevent this
			 * from happening.
			 */

			// if the BulkMembership data exists and contains an unknown person
			// id

			ContractException contractException = assertThrows(ContractException.class, () -> {
				BulkPersonContructionData.Builder builder = BulkPersonContructionData.builder();
				PersonContructionData.Builder peopleBuilder = PersonContructionData.builder();
				BulkGroupMembershipData.Builder membershipBuilder = BulkGroupMembershipData.builder();
				builder.add(peopleBuilder.build());
				membershipBuilder.addGroup(TestGroupTypeId.GROUP_TYPE_1);
				membershipBuilder.addPersonToGroup(1, 0);
				builder.addAuxiliaryData(membershipBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(builder.build()));
			});
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the BulkMembership data exists and contains an unknown group
			// type id

			contractException = assertThrows(ContractException.class, () -> {
				BulkPersonContructionData.Builder builder = BulkPersonContructionData.builder();
				PersonContructionData.Builder peopleBuilder = PersonContructionData.builder();
				BulkGroupMembershipData.Builder membershipBuilder = BulkGroupMembershipData.builder();
				builder.add(peopleBuilder.build());
				membershipBuilder.addGroup(TestGroupTypeId.getUnknownGroupTypeId());
				builder.addAuxiliaryData(membershipBuilder.build());
				c.resolveEvent(new BulkPersonCreationEvent(builder.build()));
			});
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		}));

		// have the observer verify the observations
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedGroupObservations, actualGroupObservations);
		}));

		/*
		 * Initialize with some people and groups
		 */
		GroupsActionSupport.testConsumers(10, 3, 5, 4483791915301705904L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupMembershipAdditionEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupMembershipAdditionObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getPersonId()));
			});

		}));

		// add an agent to add members to groups
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(personGroupDataView.getPeopleForGroup(groupId));
				int count = 0;
				for (PersonId personId : people) {
					if (!peopleForGroup.contains(personId)) {
						c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
						assertTrue(personGroupDataView.isGroupMember(groupId, personId));
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
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertEquals(27, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();

			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(null, groupId)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(10000), groupId)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the group id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, null)));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, new GroupId(10000))));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the person is already a member of the group
			c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId)));
			assertEquals(GroupError.DUPLICATE_GROUP_MEMBERSHIP, contractException.getErrorType());

		}));

		GroupsActionSupport.testConsumers(30, 3, 10, 2733223420384068616L, pluginBuilder.build());
	}

	private static class CustomEvent implements Event {
		private final PersonId personId;

		private CustomEvent(PersonId personId) {
			this.personId = personId;
		}

		public PersonId getPersonId() {
			return personId;
		}

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testPersonImminentRemovalObservationEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Precondition checks
		 * 
		 * The group event resolver should throw a contract exception when a
		 * PersonImminentRemovalObservationEvent is generated without a valid
		 * person id. Normally these events are generated by the people
		 * resolver, but that resolver will not produce such an invalid event.
		 * 
		 * We will force the production of an invalid event with a custom
		 * resolver and custom event. Due to ordering in the addition of
		 * plugins, we can guarantee that the first resolver to validate will be
		 * the group event compartment resolver. Multiple resolvers may throw
		 * these exceptions and we will have to verify that the correct resolver
		 * is the source of the exception by examining the exception.
		 * 
		 * Since resolvers do not have immediate event resolution, it is best to
		 * have an agent that creates a custom event for the custom resolver to
		 * resolve. This will in turn produce the desired invalid
		 * PersonImminentRemovalObservationEvent
		 */
		ResolverId resolverId = new SimpleResolverId("custom resolver");
		pluginBuilder.addResolver(resolverId);

		pluginBuilder.addResolverActionPlan(resolverId, new ResolverActionPlan(0, (c) -> {
			c.subscribeToEventExecutionPhase(CustomEvent.class, (c2, e) -> {
				PersonImminentRemovalObservationEvent event = new PersonImminentRemovalObservationEvent(e.getPersonId());
				c.queueEventForResolution(event);
			});
		}));

		/*
		 * Have an agent send custom event to the custom resolver that will
		 * cause the required contract exceptions
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(null)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new CustomEvent(new PersonId(-1))));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
			assertTrue(contractException.getMessage().contains(GroupEventResolver.class.getSimpleName()));

		}));

		/*
		 * Have the agent add a person and then remove it. There will be a delay
		 * of 0 time for the person to be removed.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			// add a new person
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			// place the person in all groups
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
			}

			// remove the person
			c.resolveEvent(new PersonRemovalRequestEvent(personId));

		}));

		/*
		 * Have the agent show that the person is no longer present in the
		 * groups
		 * 
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			
			// get a list of all the groups
			List<GroupId> groupIds = personGroupDataView.getGroupIds();

			//get the last added person
			PersonId personId = personDataView.getLastIssuedPersonId().get();
			
			//show that the person does not exist
			assertFalse(personDataView.personExists(personId));
			
			//show that none of the groups contain the person
			for (GroupId groupId : groupIds) {
				List<PersonId> people = personGroupDataView.getPeopleForGroup(groupId);
				assertFalse(people.contains(personId));
			}

		}));

		GroupsActionSupport.testConsumers(30, 3, 10, 2908277607868593618L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testPersonGroupDataViewInitialization() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7212690164088198082L);

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
		GroupInitialData.Builder groupBuilder = GroupInitialData.builder();
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

		GroupInitialData groupInitialData = groupBuilder.build();

		builder.addPlugin(GroupPlugin.PLUGIN_ID, new GroupPlugin(groupInitialData)::init);

		// add the people plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the report plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent that will demonstrate that the state of the data view
		// reflects the contents of the group initial data.
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// show the groups are as expected
			List<GroupId> actualGroupIds = personGroupDataView.getGroupIds();
			Set<GroupId> expectedGroupIds = groupInitialData.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

			// show that each group has the expected type
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				GroupTypeId expectedGroupTypeId = groupInitialData.getGroupTypeId(groupId);
				GroupTypeId actualGroupTypeId = personGroupDataView.getGroupType(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

			// show the group memberships are the same
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				Set<PersonId> expectedPeople = groupInitialData.getGroupMembers(groupId);
				List<PersonId> actualPeople = personGroupDataView.getPeopleForGroup(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

			// show that the group types are the same
			Set<GroupTypeId> expectedGroupTypeIds = groupInitialData.getGroupTypeIds();
			Set<GroupTypeId> actualGroupTypeIds = personGroupDataView.getGroupTypeIds();
			assertEquals(expectedGroupTypeIds, actualGroupTypeIds);

			// show that the property definitions are the same
			for (GroupTypeId groupTypeId : personGroupDataView.getGroupTypeIds()) {
				Set<GroupPropertyId> expectedGroupPropertyIds = groupInitialData.getGroupPropertyIds(groupTypeId);
				Set<GroupPropertyId> actualGroupPropertyIds = personGroupDataView.getGroupPropertyIds(groupTypeId);
				assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
				for (GroupPropertyId groupPropertyId : actualGroupPropertyIds) {
					PropertyDefinition expectedPropertyDefinition = groupInitialData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					PropertyDefinition actualPropertyDefinition = personGroupDataView.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// show that the group property values are the same
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
				Set<GroupPropertyId> groupPropertyIds = personGroupDataView.getGroupPropertyIds(groupTypeId);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					Object expectedValue = groupInitialData.getGroupPropertyValue(groupId, groupPropertyId);
					Object actualValue = personGroupDataView.getGroupPropertyValue(groupId, groupPropertyId);
					assertEquals(expectedValue, actualValue);

				}

			}

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupPropertyValueAssignmentEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create data structures for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent start observing group property value assignmentEvents.
		pluginBuilder.addAgent("observer");

		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupPropertyChangeObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getGroupPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue()));

			});
		}));

		// have an agent change a few group property values
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();

			// show that there are some groups
			assertTrue(groupIds.size() > 0);

			for (GroupId groupId : groupIds) {
				TestGroupTypeId testGroupTypeId = personGroupDataView.getGroupType(groupId);
				for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId)) {
					if (testGroupPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object currentValue = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
						Object expectedValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
						c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, expectedValue));
						Object actualValue = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
						assertEquals(expectedValue, actualValue);

						expectedObservations.add(new MultiKey(groupId, testGroupPropertyId, currentValue, expectedValue));

					}
				}
			}
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(null, testGroupPropertyId, true)));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(100000), testGroupPropertyId, true)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, null, true)));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, TestGroupPropertyId.getUnknownGroupPropertyId(), true)));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, null)));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_VALUE, contractException.getErrorType());

			// if property value is incompatible with the corresponding property
			// definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, 5)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the corresponding property definition defines the property as
			// immutable
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));
			GroupId groupId2 = personGroupDataView.getLastIssuedGroupId().get();
			TestGroupPropertyId testGroupPropertyId2 = TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK;

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId2, testGroupPropertyId2, true)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		}));

		// show that the observations of the group property value assignments
		// were correct
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		GroupsActionSupport.testConsumers(100, 3, 5, 4653012806568812031L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupRemovalRequestEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers for observations
		Set<GroupId> actualObservations = new LinkedHashSet<>();
		Set<GroupId> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group removals
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(e.getGroupId());
			});

		}));

		// add an agent that removes groups
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			assertTrue(groupIds.size() > 5);
			for (int i = 0; i < 5; i++) {
				GroupId groupId = groupIds.get(i);
				c.resolveEvent(new GroupRemovalRequestEvent(groupId));
				expectedObservations.add(groupId);
			}
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (GroupId groupId : expectedObservations) {
				assertFalse(personGroupDataView.groupExists(groupId));
			}

		}));

		// have the observer verify that the observations were correct
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupRemovalRequestEvent(null)));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(10000))));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		}));

		GroupsActionSupport.testConsumers(30, 3, 10, 1454752128175396298L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "init", args = { GroupInitialData.class })
	public void testGroupMembershipRemovalEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers for observations
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();

		// add an agent to observe the group membership additions
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(GroupMembershipAdditionObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(e.getGroupId(), e.getPersonId()));
			});

		}));

		// add an agent to add members to groups
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			Collections.shuffle(people, new Random(randomGenerator.nextLong()));
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (GroupId groupId : personGroupDataView.getGroupIds()) {
				Set<PersonId> peopleForGroup = new LinkedHashSet<>(personGroupDataView.getPeopleForGroup(groupId));
				int count = 0;
				for (PersonId personId : people) {
					if (!peopleForGroup.contains(personId)) {
						c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
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
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertEquals(27, expectedObservations.size());
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();

			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(null, groupId)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(10000), groupId)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the group id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, null)));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, new GroupId(10000))));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the person is already a member of the group
			c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId)));
			assertEquals(GroupError.DUPLICATE_GROUP_MEMBERSHIP, contractException.getErrorType());

		}));

		GroupsActionSupport.testConsumers(30, 3, 10, 2733223420384068616L, pluginBuilder.build());

	}

}
