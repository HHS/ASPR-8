package plugins.groups.datacontainers;

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
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.stochastics.StochasticsDataView;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonGroupDataManager.class)
public class AT_PersonGroupDataManager {


	@Test
	@UnitTestConstructor(args = { Context.class, GroupInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonGroupDataManager(null));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
	}

	// Returns a copy of the PersonGroupDataManger that is owned by the plugin
	private PersonGroupDataManager getPersonGroupDataManger(Context context) {

		PersonGroupDataManager result = new PersonGroupDataManager(context);
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();

		for (GroupTypeId groupTypeId : personGroupDataView.getGroupTypeIds()) {
			result.addGroupType(groupTypeId);
			for (GroupPropertyId groupPropertyId : personGroupDataView.getGroupPropertyIds(groupTypeId)) {
				PropertyDefinition propertyDefinition = personGroupDataView.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				result.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
			}
		}

		List<GroupId> groupIds = new ArrayList<>(personGroupDataView.getGroupIds());
		Collections.sort(groupIds);

		for (GroupId groupId : groupIds) {
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
			GroupId groupId2 = result.addGroup(groupTypeId);
			assertEquals(groupId, groupId2);

			Set<GroupPropertyId> groupPropertyIds = personGroupDataView.getGroupPropertyIds(groupTypeId);
			for (GroupPropertyId groupPropertyId : groupPropertyIds) {
				Object value = personGroupDataView.getGroupPropertyValue(groupId, groupPropertyId);
				result.setGroupPropertyValue(groupId, groupPropertyId, value);
			}

			List<PersonId> peopleForGroup = personGroupDataView.getPeopleForGroup(groupId);
			for (PersonId personId : peopleForGroup) {
				result.addPersonToGroup(groupId, personId);
			}
		}

		return result;
	}

	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupTypeId.class })
	public void testAddGroup() {
		
		GroupsActionSupport.testConsumer(30, 3, 5, 5028011062064947292L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group
				GroupId groupId = personGroupDataManager.addGroup(testGroupTypeId);

				// show that the returned group id is not null
				assertNotNull(groupId);

				// show that the manager indicates the group id exists
				assertTrue(personGroupDataManager.groupExists(groupId));

				// show that the group has the correct group type
				assertEquals(testGroupTypeId, personGroupDataManager.getGroupType(groupId));

				// show that the group has the default values for its properties
				Set<GroupPropertyId> groupPropertyIds = personGroupDataManager.getGroupPropertyIds(testGroupTypeId);
				assertTrue(groupPropertyIds.size() > 0);
				for (GroupPropertyId groupPropertyId : groupPropertyIds) {
					PropertyDefinition propertyDefinition = personGroupDataManager.getGroupPropertyDefinition(testGroupTypeId, groupPropertyId);
					Object expectedValue = propertyDefinition.getDefaultValue().get();
					Object actualValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					assertEquals(expectedValue, actualValue);
				}

				// show that the group has no members
				assertEquals(0, personGroupDataManager.getPersonCountForGroup(groupId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {
		GroupsActionSupport.testConsumer(30, 3, 5, 7212679629946010570L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			// show that there are some groups -- we expect about 18
			assertTrue(groupIds.size() > 10);

			for (GroupId groupId : groupIds) {
				// find a person not in the group and add them
				PersonId selectedPersonId = null;
				for (int i = 0; i < 10; i++) {
					PersonId candidatePersonId = people.get(randomGenerator.nextInt(people.size()));
					if (!personGroupDataManager.isGroupMember(groupId, candidatePersonId)) {
						selectedPersonId = candidatePersonId;
						break;
					}
				}
				assertNotNull(selectedPersonId);

				personGroupDataManager.addPersonToGroup(groupId, selectedPersonId);
				// show that the person is in the group
				assertTrue(personGroupDataManager.isGroupMember(groupId, selectedPersonId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupCountForGroupType", args = { GroupTypeId.class })
	public void testGetGroupCountForGroupType() {
		GroupsActionSupport.testConsumer(300, 3, 5, 1397470169952758056L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct containers to hold expectations and actual counts
			Map<GroupTypeId, MutableInteger> actualCounts = new LinkedHashMap<>();
			Map<GroupTypeId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				actualCounts.put(testGroupTypeId, new MutableInteger());
				int count = personGroupDataManager.getGroupCountForGroupType(testGroupTypeId);
				expectedCounts.put(testGroupTypeId, new MutableInteger(count));
			}

			// poll through the groups and increment the corresponding counters
			for (GroupId groupId : groupIds) {
				actualCounts.get(personGroupDataManager.getGroupType(groupId)).increment();
			}
			// show that expectation were met
			assertEquals(expectedCounts, actualCounts);
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupCountForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupCountForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(300, 3, 5, 2349257575750038336L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct a container to hold expectations
			Map<PersonId, Map<GroupTypeId, MutableInteger>> expectedCounts = new LinkedHashMap<>();
			for (PersonId personId : people) {
				Map<GroupTypeId, MutableInteger> map = new LinkedHashMap<>();
				expectedCounts.put(personId, map);
				for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
					map.put(testGroupTypeId, new MutableInteger());
				}
			}

			// poll through the groups and build the expectations
			for (GroupId groupId : groupIds) {
				List<PersonId> peopleInGroup = personGroupDataManager.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					Map<GroupTypeId, MutableInteger> map = expectedCounts.get(personId);
					GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
					MutableInteger mutableInteger = map.get(groupTypeId);
					mutableInteger.increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
					int expectedValue = expectedCounts.get(personId).get(testGroupTypeId).getValue();
					int actualValue = personGroupDataManager.getGroupCountForGroupTypeAndPerson(testGroupTypeId, personId);
					assertEquals(expectedValue, actualValue);
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupCountForPerson", args = { PersonId.class })
	public void testGetGroupCountForPerson() {

		GroupsActionSupport.testConsumer(300, 3, 5, 369489281172127222L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct a container to hold expectations
			Map<PersonId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (PersonId personId : people) {
				expectedCounts.put(personId, new MutableInteger());
			}

			// poll through the groups and build the expectations
			for (GroupId groupId : groupIds) {
				List<PersonId> peopleInGroup = personGroupDataManager.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					expectedCounts.get(personId).increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				int expectedValue = expectedCounts.get(personId).getValue();
				int actualValue = personGroupDataManager.getGroupCountForPerson(personId);
				assertEquals(expectedValue, actualValue);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupIds", args = {})
	public void testGetGroupIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 3198741833858076412L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				GroupId groupId = personGroupDataManager.addGroup(TestGroupTypeId.getRandomGroupTypeId(randomGenerator));
				expectedGroupIds.add(groupId);
			}

			// show that the group ids match the expected group ids
			List<GroupId> actualGroupIds = personGroupDataManager.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyDefinition", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyDefinition() {

		GroupsActionSupport.testConsumer(10, 0, 5, 6327026947655873965L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);

			// show that the personGroupDataManger has the expected property
			// definitions
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = personGroupDataManager.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// add a new property definition and show that it can be retrieved

			GroupPropertyId groupPropertyId = new GroupPropertyId() {
			};

			PropertyDefinition expectedPropertyDefinition = PropertyDefinition.builder().setDefaultValue(12).setType(Integer.class).build();

			GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

			personGroupDataManager.defineGroupProperty(groupTypeId, groupPropertyId, expectedPropertyDefinition);

			PropertyDefinition actualPropertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);

			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyExists", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyExists() {

		GroupsActionSupport.testConsumer(10, 0, 5, 1868084197385440902L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);

			// show that the personGroupDataManger returns true for the group
			// properties that should be present
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(personGroupDataManager.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			// show that other group properties do not exits
			assertFalse(personGroupDataManager.getGroupPropertyExists(null, null));
			assertFalse(personGroupDataManager.getGroupPropertyExists(null, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertFalse(personGroupDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), null));
			assertFalse(personGroupDataManager.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.getUnknownGroupPropertyId()));
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyIds", args = { GroupTypeId.class })
	public void testGetGroupPropertyIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 7097785170729722349L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);

			// show that the personGroupDataManger returns the correct group
			// property ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				Set<TestGroupPropertyId> expectedPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
				Set<GroupPropertyId> actualPropertyIds = personGroupDataManager.getGroupPropertyIds(testGroupTypeId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

		});
	}

	private static class LocalDataView implements DataView {
		private PersonGroupDataManager personGroupDataManager;

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyTime", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create a container to hold our expectations. The MultiKey will be
		 * (GroupId,GroupPropertyId) pairs and the MutableDoubles will hold the
		 * most recent time when each property was set.
		 */
		Map<MultiKey, MutableDouble> expectedTimes = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		// Have the agent build a person group data manager for this test
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.personGroupDataManager = personGroupDataManager;
		}));

		/*
		 * At time = 1, have the agent show that the property values were all
		 * set at time = 0 and then set those properties to new values
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

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
				List<GroupId> groupsForGroupType = personGroupDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					double groupPropertyTime = personGroupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
					assertEquals(0.0, groupPropertyTime);
					expectedTimes.put(new MultiKey(groupId, testGroupPropertyId), new MutableDouble(1.0));
					personGroupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values were all
		 * set at time = 1 and then set those properties to new values
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = personGroupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);

				mutableDouble.setValue(2.0);
				personGroupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
			}

		}));

		/*
		 * At time = 3, have the agent show that the property values were all
		 * set at time = 2
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = personGroupDataManager.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);
			}

		}));

		GroupsActionSupport.testConsumers(30, 3, 5, 2287871854375264378L, pluginBuilder.build());
	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyValue() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Create a container to hold our expectations. The MultiKey will be
		 * (GroupId,GroupPropertyId) pairs and the Object will hold the most
		 * recent property value.
		 */
		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		pluginBuilder.addDataView(new LocalDataView());

		// Have the agent build a person group data manager for this test
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			localDataView.personGroupDataManager = personGroupDataManager;
		}));

		/*
		 * At time = 1, have the agent establish the expected values.
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;

			List<GroupId> groupIds = personGroupDataManager.getGroupIds();

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
				List<GroupId> groupsForGroupType = personGroupDataManager.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					Object value = personGroupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values and then set those properties to new values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = personGroupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);

				Object newValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				personGroupDataManager.setGroupPropertyValue(groupId, testGroupPropertyId, newValue);
				expectedValues.put(multiKey, newValue);
			}

		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			LocalDataView localDataView = c.getDataView(LocalDataView.class).get();
			PersonGroupDataManager personGroupDataManager = localDataView.personGroupDataManager;

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = personGroupDataManager.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);
			}

		}));

		GroupsActionSupport.testConsumers(30, 3, 5, 6255802738457050309L, pluginBuilder.build());
	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupType", args = { GroupTypeId.class })
	public void testGetGroupsForGroupType() {

		GroupsActionSupport.testConsumer(10, 0, 5, 2069048534280021641L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

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
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
				expectedTypeToGroupIds.get(groupTypeId).add(groupId);
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<GroupId> actualGroupIds = personGroupDataManager.getGroupsForGroupType(testGroupTypeId);
				Set<GroupId> expectedGroupIds = expectedTypeToGroupIds.get(testGroupTypeId);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupsForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 6444222302427318739L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<MultiKey, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					personGroupDataManager.addPersonToGroup(groupId, personId);
					GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
					MultiKey multiKey = new MultiKey(groupTypeId, personId);
					Set<GroupId> groups = expectedDataStructure.get(multiKey);
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (PersonId personId : people) {
					List<GroupId> actualGroupIds = personGroupDataManager.getGroupsForGroupTypeAndPerson(testGroupTypeId, personId);
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					Set<GroupId> expectedGroupIds = expectedDataStructure.get(multiKey);
					assertEquals(expectedGroupIds.size(), actualGroupIds.size());
					assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupsForPerson", args = { PersonId.class })
	public void testGetGroupsForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5460063158946173122L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
			}

			/*
			 * For each persosm pick three groups at random and add the person
			 * to each group, recording this in the expected data structure
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				for (int i = 0; i < 3; i++) {
					GroupId groupId = groupIds.get(i);
					personGroupDataManager.addPersonToGroup(groupId, personId);
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
				List<GroupId> actualGroupIds = personGroupDataManager.getGroupsForPerson(personId);
				Set<GroupId> expectedGroupIds = expectedDataStructure.get(personId);
				assertNotNull(expectedGroupIds);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupType", args = { GroupId.class })
	public void testGetGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4537580348785203196L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, GroupTypeId> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			for (int i = 0; i < 60; i++) {
				TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
				expectedDataStructure.put(groupId, groupTypeId);
			}

			// show that the group have the expected types
			for (GroupId groupId : expectedDataStructure.keySet()) {
				GroupTypeId actualGroupTypeId = personGroupDataManager.getGroupType(groupId);
				GroupTypeId expectedGroupTypeId = expectedDataStructure.get(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeCountForPersonId", args = { PersonId.class })
	public void testGetGroupTypeCountForPersonId() {

		GroupsActionSupport.testConsumer(100, 0, 5, 9176341034868820866L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Integer> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					List<GroupId> groupsForGroupType = personGroupDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					personGroupDataManager.addPersonToGroup(groupId, personId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				int actualCount = personGroupDataManager.getGroupTypeCountForPersonId(personId);
				Integer expectedCount = expectedDataStructure.get(personId);
				assertEquals(expectedCount.intValue(), actualCount);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {

		GroupsActionSupport.testConsumer(10, 3, 5, 3665027658897241062L, (c) -> {
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			// show that the group ids match the expected group ids
			Set<GroupTypeId> groupTypeIds = personGroupDataManager.getGroupTypeIds();
			assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupTypeIds);
		});
	}

	@Test
	@UnitTestMethod(name = "getGroupTypesForPerson", args = { PersonId.class })
	public void testGetGroupTypesForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8718449598021344214L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<PersonId, Set<GroupTypeId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					List<GroupId> groupsForGroupType = personGroupDataManager.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					personGroupDataManager.addPersonToGroup(groupId, personId);
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				List<GroupTypeId> actualGroupTypesForPerson = personGroupDataManager.getGroupTypesForPerson(personId);
				Set<GroupTypeId> expectedGroupTypesForPerson = expectedDataStructure.get(personId);
				assertEquals(expectedGroupTypesForPerson.size(), actualGroupTypesForPerson.size());
				assertEquals(expectedGroupTypesForPerson, new LinkedHashSet<>(actualGroupTypesForPerson));
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getLastIssuedGroupId", args = {})
	public void testGetLastIssuedGroupId() {

		GroupsActionSupport.testConsumer(30, 0, 5, 6820082779197265887L, (c) -> {
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			Optional<GroupId> optional = personGroupDataManager.getLastIssuedGroupId();
			assertFalse(optional.isPresent());
			
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				GroupId expectedGroupId = personGroupDataManager.addGroup(testGroupTypeId);
				testGroupTypeId = testGroupTypeId.next();
				optional = personGroupDataManager.getLastIssuedGroupId();
				assertTrue(optional.isPresent());
				GroupId actualGroupId = optional.get();
				assertEquals(expectedGroupId, actualGroupId);
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroup", args = { GroupId.class })
	public void testGetPeopleForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5155969046483450971L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					personGroupDataManager.addPersonToGroup(groupId, personId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				List<PersonId> actualPeople = personGroupDataManager.getPeopleForGroup(groupId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroupType", args = { GroupTypeId.class })
	public void testGetPeopleForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 7195539983719902145L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupTypeId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					groupTypeId = personGroupDataManager.getGroupType(groupId);
					personGroupDataManager.addPersonToGroup(groupId, personId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<PersonId> actualPeople = personGroupDataManager.getPeopleForGroupType(testGroupTypeId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(testGroupTypeId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroup", args = { GroupId.class })
	public void testGetPersonCountForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4381779178686512285L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, MutableInteger> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					personGroupDataManager.addPersonToGroup(groupId, personId);
					expectedDataStructure.get(groupId).increment();
				}
			}

			// show that number of people matches expectations

			for (GroupId groupId : groupIds) {
				int actualCount = personGroupDataManager.getPersonCountForGroup(groupId);
				int expectedCount = expectedDataStructure.get(groupId).getValue();
				assertEquals(expectedCount, actualCount);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroupType", args = { GroupTypeId.class })
	public void testGetPersonCountForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8117598363809829939L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

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
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);
				groupTypeId = groupTypeId.next();
			}

			System.out.println();
			/*
			 * For each person pick either one two or three group types and
			 * record.
			 */
			for (PersonId personId : people) {
				Collections.shuffle(groupIds, new Random(randomGenerator.nextLong()));
				int groupCount = randomGenerator.nextInt(3) + 1;
				for (int i = 0; i < groupCount; i++) {
					GroupId groupId = groupIds.get(i);
					groupTypeId = personGroupDataManager.getGroupType(groupId);
					personGroupDataManager.addPersonToGroup(groupId, personId);
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that number of people matches expectations
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int actualCount = personGroupDataManager.getPersonCountForGroupType(testGroupTypeId);
				int expectedCount = expectedDataStructure.get(testGroupTypeId).size();
				assertEquals(expectedCount, actualCount);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "groupExists", args = { GroupId.class })
	public void testGroupExists() {

		GroupsActionSupport.testConsumer(30, 3, 5, 8706871164283308577L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group
				GroupId groupId = personGroupDataManager.addGroup(testGroupTypeId);
				assertTrue(personGroupDataManager.groupExists(groupId));
				personGroupDataManager.removeGroup(groupId);
				assertFalse(personGroupDataManager.groupExists(groupId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "groupTypeIdExists", args = { GroupTypeId.class })
	public void testGroupTypeIdExists() {

		GroupsActionSupport.testConsumer(10, 3, 5, 1172766215251823083L, (c) -> {
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				assertTrue(personGroupDataManager.groupTypeIdExists(testGroupTypeId));
			}
			assertFalse(personGroupDataManager.groupTypeIdExists(TestGroupTypeId.getUnknownGroupTypeId()));
		});
	}

	@Test
	@UnitTestMethod(name = "isGroupMember", args = { GroupId.class, PersonId.class })
	public void testIsGroupMember() {

		GroupsActionSupport.testConsumer(100, 0, 5, 6571438869269708906L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());

			Map<GroupId, Set<PersonId>> expectedDataStructure = new LinkedHashMap<>();

			// create 60 groups
			TestGroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 60; i++) {
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
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
					personGroupDataManager.addPersonToGroup(groupId, personId);
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				for (PersonId personId : people) {
					if (expectedPeople.contains(personId)) {
						assertTrue(personGroupDataManager.isGroupMember(groupId, personId));
					} else {
						assertFalse(personGroupDataManager.isGroupMember(groupId, personId));
					}
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "removeGroup", args = { GroupId.class })
	public void testRemoveGroup() {
		
		GroupsActionSupport.testConsumer(30, 3, 5, 8204685090168544876L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group
				GroupId groupId = personGroupDataManager.addGroup(testGroupTypeId);

				// show that the returned group id is not null
				assertNotNull(groupId);

				// show that the manager indicates the group id exists
				assertTrue(personGroupDataManager.groupExists(groupId));

				// remove the group
				personGroupDataManager.removeGroup(groupId);

				// show that the group is no long present
				assertFalse(personGroupDataManager.groupExists(groupId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "removePerson", args = { PersonId.class })
	public void testRemovePerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 2443170149816161864L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			for (PersonId personId : people) {
				List<GroupId> groupsForPerson = personGroupDataManager.getGroupsForPerson(personId);
				if (groupsForPerson.size() > 0) {
					personGroupDataManager.removePerson(personId);
					groupsForPerson = personGroupDataManager.getGroupsForPerson(personId);
					assertTrue(groupsForPerson.isEmpty());
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "removePersonFromGroup", args = { GroupId.class, PersonId.class })
	public void testRemovePersonFromGroup() {

		GroupsActionSupport.testConsumer(30, 3, 5, 1820147295941437099L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			// remove the people from their groups and show that they are no
			// long members
			int testCount = 0;
			for (PersonId personId : people) {
				List<GroupId> groupsForPerson = personGroupDataManager.getGroupsForPerson(personId);
				for (GroupId groupId : groupsForPerson) {
					personGroupDataManager.removePersonFromGroup(groupId, personId);
					assertFalse(personGroupDataManager.isGroupMember(groupId, personId));
					testCount++;
				}
			}
			// show that the assertFalse was executed a reasonable number of
			// times

			assertTrue(testCount > 30);

		});
	}

	@Test
	@UnitTestMethod(name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void setGroupPropertyValue() {

		GroupsActionSupport.testConsumer(30, 3, 5, 9100121552375309262L, (c) -> {

			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Show that every property of every group is currently at the
			 * default value. For properties that can be mutated, show that the
			 * value of the property is correct after having set it to a new
			 * value.
			 */
			for (GroupId groupId : personGroupDataManager.getGroupIds()) {
				GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
				Set<TestGroupPropertyId> groupPropertyIds = personGroupDataManager.getGroupPropertyIds(groupTypeId);
				for (TestGroupPropertyId groupPropertyId : groupPropertyIds) {
					PropertyDefinition propertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
					/*
					 * Show that property's current value is the default
					 * 
					 */
					Object propertyValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					assertEquals(propertyDefinition.getDefaultValue().get(), propertyValue);
					if (propertyDefinition.propertyValuesAreMutable()) {
						/*
						 * Show that the property can be updated to a new
						 * randomized value.
						 */
						Object newValue = groupPropertyId.getRandomPropertyValue(randomGenerator);
						personGroupDataManager.setGroupPropertyValue(groupId, groupPropertyId, newValue);
						propertyValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
						assertEquals(newValue, propertyValue);
					}
				}
			}

		});
	}

	private static enum ExcludedPersonType {
		NULL, MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestMethod(name = "sampleGroup", args = { GroupId.class, GroupSampler.class })
	public void testSampleGroup() {

		GroupsActionSupport.testConsumer(30, 3, 5, 4497295149828614266L, (c) -> {
			// establish data views and the lists to groups and people in the
			// simuation
			PersonGroupDataManager personGroupDataManager = getPersonGroupDataManger(c);
			List<GroupId> groupIds = personGroupDataManager.getGroupIds();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

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
						List<PersonId> peopleForGroup = personGroupDataManager.getPeopleForGroup(groupId);
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
							Optional<PersonId> optional = personGroupDataManager.sampleGroup(groupId, groupSampler);
							assertFalse(optional.isPresent());
						} else {
							// Draw a reasonable number of people from the group
							// and show that they are all eligible people
							for (int i = 0; i < eligiblePeople.size(); i++) {
								Optional<PersonId> optional = personGroupDataManager.sampleGroup(groupId, groupSampler);
								assertTrue(optional.isPresent());
								PersonId selectedPersonId = optional.get();
								assertTrue(eligiblePeople.contains(selectedPersonId));
							}
						}

					}
				}
			}

		});

	}

}
