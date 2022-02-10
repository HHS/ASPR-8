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

import nucleus.SimulationContext;
import nucleus.DataView;
import nucleus.NucleusError;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Published data view that provides person group information
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PersonGroupDataView.class)
public final class AT_PersonGroupDataView implements DataView {


	@Test
	@UnitTestConstructor(args = { SimulationContext.class, PersonGroupDataManager.class })
	public void testConstructor() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4829992068683627833L, (c) -> {
			PersonGroupDataManager personGroupDataManager = new PersonGroupDataManager(c);

			// precondition tests

			// if the context is null
			ContractException contractException = assertThrows(ContractException.class, () -> new PersonGroupDataView(null, personGroupDataManager));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			// if the person group data manager is null
			contractException = assertThrows(ContractException.class, () -> new PersonGroupDataView(c, null));
			assertEquals(GroupError.NULL_GROUP_DATA_MANAGER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupCountForGroupType", args = { GroupTypeId.class })
	public void testGetGroupCountForGroupType() {

		GroupsActionSupport.testConsumer(300, 3, 5, 2910747162784803859L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct containers to hold expectations and actual counts
			Map<GroupTypeId, MutableInteger> actualCounts = new LinkedHashMap<>();
			Map<GroupTypeId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				actualCounts.put(testGroupTypeId, new MutableInteger());
				int count = personGroupDataView.getGroupCountForGroupType(testGroupTypeId);
				expectedCounts.put(testGroupTypeId, new MutableInteger(count));
			}

			// poll through the groups and increment the corresponding counters
			for (GroupId groupId : groupIds) {
				actualCounts.get(personGroupDataView.getGroupType(groupId)).increment();
			}
			// show that expectation were met
			assertEquals(expectedCounts, actualCounts);

			// precondition tests

			// if the group type is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupCountForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupCountForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(300, 3, 5, 8663107207699222154L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();

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
				List<PersonId> peopleInGroup = personGroupDataView.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					Map<GroupTypeId, MutableInteger> map = expectedCounts.get(personId);
					GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
					MutableInteger mutableInteger = map.get(groupTypeId);
					mutableInteger.increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
					int expectedValue = expectedCounts.get(personId).get(testGroupTypeId).getValue();
					int actualValue = personGroupDataView.getGroupCountForGroupTypeAndPerson(testGroupTypeId, personId);
					assertEquals(expectedValue, actualValue);
				}
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, new PersonId(1000000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the group type id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupTypeAndPerson(null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupCountForPerson", args = { PersonId.class })
	public void testGetGroupCountForPerson() {

		GroupsActionSupport.testConsumer(300, 3, 5, 6371809280692201768L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();

			// show that there are some groups -- we expect about 180
			assertTrue(groupIds.size() > 100);

			// construct a container to hold expectations
			Map<PersonId, MutableInteger> expectedCounts = new LinkedHashMap<>();
			for (PersonId personId : people) {
				expectedCounts.put(personId, new MutableInteger());
			}

			// poll through the groups and build the expectations
			for (GroupId groupId : groupIds) {
				List<PersonId> peopleInGroup = personGroupDataView.getPeopleForGroup(groupId);
				for (PersonId personId : peopleInGroup) {
					expectedCounts.get(personId).increment();
				}
			}

			// show that the counts match the expected counts
			for (PersonId personId : people) {
				int expectedValue = expectedCounts.get(personId).getValue();
				int actualValue = personGroupDataView.getGroupCountForPerson(personId);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupCountForPerson(new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getGroupIds", args = {})
	public void testGetGroupIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 6455798573295403809L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Show that there are no groups since we selected 0 groups per
			 * person
			 */
			assertEquals(0, groupIds.size());
			Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.getRandomGroupTypeId(randomGenerator)));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
				expectedGroupIds.add(groupId);
			}

			// show that the group ids match the expected group ids
			List<GroupId> actualGroupIds = personGroupDataView.getGroupIds();
			assertEquals(expectedGroupIds.size(), actualGroupIds.size());
			assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupType", args = { GroupTypeId.class })
	public void testGetGroupsForGroupType() {

		GroupsActionSupport.testConsumer(10, 0, 5, 3948247844369837305L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
				expectedTypeToGroupIds.get(groupTypeId).add(groupId);
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<GroupId> actualGroupIds = personGroupDataView.getGroupsForGroupType(testGroupTypeId);
				Set<GroupId> expectedGroupIds = expectedTypeToGroupIds.get(testGroupTypeId);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForGroupTypeAndPerson", args = { GroupTypeId.class, PersonId.class })
	public void testGetGroupsForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4847183275886938594L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
					MultiKey multiKey = new MultiKey(groupTypeId, personId);
					Set<GroupId> groups = expectedDataStructure.get(multiKey);
					groups.add(groupId);
				}
			}

			// show that the group ids match the expected group ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				for (PersonId personId : people) {
					List<GroupId> actualGroupIds = personGroupDataView.getGroupsForGroupTypeAndPerson(testGroupTypeId, personId);
					MultiKey multiKey = new MultiKey(testGroupTypeId, personId);
					Set<GroupId> expectedGroupIds = expectedDataStructure.get(multiKey);
					assertEquals(expectedGroupIds.size(), actualGroupIds.size());
					assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
				}
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the group type id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupTypeAndPerson(null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForGroupTypeAndPerson(TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupsForPerson", args = { PersonId.class })
	public void testGetGroupsForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1095418957424488372L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
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
				List<GroupId> actualGroupIds = personGroupDataView.getGroupsForPerson(personId);
				Set<GroupId> expectedGroupIds = expectedDataStructure.get(personId);
				assertNotNull(expectedGroupIds);
				assertEquals(expectedGroupIds.size(), actualGroupIds.size());
				assertEquals(expectedGroupIds, new LinkedHashSet<>(actualGroupIds));
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupsForPerson(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupType", args = { GroupId.class })
	public void testGetGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5910635654466929788L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
				expectedDataStructure.put(groupId, groupTypeId);
			}

			// show that the group have the expected types
			for (GroupId groupId : expectedDataStructure.keySet()) {
				GroupTypeId actualGroupTypeId = personGroupDataView.getGroupType(groupId);
				GroupTypeId expectedGroupTypeId = expectedDataStructure.get(groupId);
				assertEquals(expectedGroupTypeId, actualGroupTypeId);
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupType(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupType(new GroupId(100000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupTypeCountForPersonId", args = { PersonId.class })
	public void testGetGroupTypeCountForPersonId() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1561008711822589907L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					List<GroupId> groupsForGroupType = personGroupDataView.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				int actualCount = personGroupDataView.getGroupTypeCountForPersonId(personId);
				Integer expectedCount = expectedDataStructure.get(personId);
				assertEquals(expectedCount.intValue(), actualCount);
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupTypeCountForPersonId(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupTypeCountForPersonId(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupTypesForPerson", args = { PersonId.class })
	public void testGetGroupTypesForPerson() {

		GroupsActionSupport.testConsumer(100, 0, 5, 2999448198567478958L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					List<GroupId> groupsForGroupType = personGroupDataView.getGroupsForGroupType(groupTypeId);
					GroupId groupId = groupsForGroupType.get(randomGenerator.nextInt(groupsForGroupType.size()));
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					groupTypeId = groupTypeId.next();
				}
			}

			// show that the group ids match the expected group ids

			for (PersonId personId : people) {
				List<GroupTypeId> actualGroupTypesForPerson = personGroupDataView.getGroupTypesForPerson(personId);
				Set<GroupTypeId> expectedGroupTypesForPerson = expectedDataStructure.get(personId);
				assertEquals(expectedGroupTypesForPerson.size(), actualGroupTypesForPerson.size());
				assertEquals(expectedGroupTypesForPerson, new LinkedHashSet<>(actualGroupTypesForPerson));
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupTypesForPerson(null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupTypesForPerson(new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	private static enum ExcludedPersonType {
		NULL, MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestMethod(name = "sampleGroup", args = { GroupId.class, GroupSampler.class })
	public void testSampleGroup() {

		GroupsActionSupport.testConsumer(30, 3, 5, 9211292135944399530L, (c) -> {
			// establish data views and the lists to groups and people in the
			// simulation
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
						List<PersonId> peopleForGroup = personGroupDataView.getPeopleForGroup(groupId);
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
							Optional<PersonId> optional = personGroupDataView.sampleGroup(groupId, groupSampler);
							assertFalse(optional.isPresent());
						} else {
							// Draw a reasonable number of people from the group
							// and show that they are all eligible people
							for (int i = 0; i < eligiblePeople.size(); i++) {
								Optional<PersonId> optional = personGroupDataView.sampleGroup(groupId, groupSampler);
								assertTrue(optional.isPresent());
								PersonId selectedPersonId = optional.get();
								assertTrue(eligiblePeople.contains(selectedPersonId));
							}
						}

					}
				}
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.sampleGroup(null, GroupSampler.builder().build()));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.sampleGroup(new GroupId(1000000), GroupSampler.builder().build()));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the group sampler is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.sampleGroup(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_SAMPLER, contractException.getErrorType());

			// if the group sampler is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.sampleGroup(new GroupId(0), GroupSampler.builder().setExcludedPersonId(new PersonId(1000000)).build()));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the group sampler is null
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.sampleGroup(new GroupId(0), GroupSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.getUnknownRandomNumberGeneratorId()).build()));
			assertEquals(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroup", args = { GroupId.class })
	public void testGetPeopleForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 4550534695972929193L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				List<PersonId> actualPeople = personGroupDataView.getPeopleForGroup(groupId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPeopleForGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPeopleForGroup(new GroupId(100000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleForGroupType", args = { GroupTypeId.class })
	public void testGetPeopleForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8576174021026036673L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					groupTypeId = personGroupDataView.getGroupType(groupId);
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				List<PersonId> actualPeople = personGroupDataView.getPeopleForGroupType(testGroupTypeId);
				Set<PersonId> expectedPeople = expectedDataStructure.get(testGroupTypeId);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPeopleForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPeopleForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroup", args = { GroupId.class })
	public void testGetPersonCountForGroup() {

		GroupsActionSupport.testConsumer(100, 0, 5, 1763603697244834578L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					expectedDataStructure.get(groupId).increment();
				}
			}

			// show that number of people matches expectations

			for (GroupId groupId : groupIds) {
				int actualCount = personGroupDataView.getPersonCountForGroup(groupId);
				int expectedCount = expectedDataStructure.get(groupId).getValue();
				assertEquals(expectedCount, actualCount);
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPersonCountForGroup(null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPersonCountForGroup(new GroupId(10000000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForGroupType", args = { GroupTypeId.class })
	public void testGetPersonCountForGroupType() {

		GroupsActionSupport.testConsumer(100, 0, 5, 5794665230130343350L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					groupTypeId = personGroupDataView.getGroupType(groupId);
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					expectedDataStructure.get(groupTypeId).add(personId);
				}
			}

			// show that number of people matches expectations
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				int actualCount = personGroupDataView.getPersonCountForGroupType(testGroupTypeId);
				int expectedCount = expectedDataStructure.get(testGroupTypeId).size();
				assertEquals(expectedCount, actualCount);
			}

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPersonCountForGroupType(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getPersonCountForGroupType(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "groupExists", args = { GroupId.class })
	public void testGroupExists() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		
		List<GroupId> removedGroupIds = new ArrayList<>();
		
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent",new AgentActionPlan(0.0, (c)->{
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				// add a group and show it exists
				c.resolveEvent(new GroupCreationEvent(testGroupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
				assertTrue(personGroupDataView.groupExists(groupId));
				// remove the group and record it for later verification
				c.resolveEvent(new GroupRemovalRequestEvent(groupId));
				removedGroupIds.add(groupId);
			}
		}));
		
		pluginBuilder.addAgentActionPlan("agent",new AgentActionPlan(0.0, (c)->{
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (GroupId groupId : removedGroupIds) {
				//show that the removed groups don't exist
				assertFalse(personGroupDataView.groupExists(groupId));
			}
		}));
		
		GroupsActionSupport.testConsumers(30, 3, 5, 2946647177720026906L, pluginBuilder.build());
	}

	@Test
	@UnitTestMethod(name = "isGroupMember", args = { GroupId.class, PersonId.class })
	public void testIsGroupMember() {

		GroupsActionSupport.testConsumer(100, 0, 5, 8319627382232144625L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
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
				c.resolveEvent(new GroupCreationEvent(groupTypeId));
				GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
					expectedDataStructure.get(groupId).add(personId);
				}
			}

			// show that the person ids match the expected person ids

			for (GroupId groupId : groupIds) {
				Set<PersonId> expectedPeople = expectedDataStructure.get(groupId);
				for (PersonId personId : people) {
					if (expectedPeople.contains(personId)) {
						assertTrue(personGroupDataView.isGroupMember(groupId, personId));
					} else {
						assertFalse(personGroupDataView.isGroupMember(groupId, personId));
					}
				}
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.isGroupMember(null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.isGroupMember(new GroupId(10000), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.isGroupMember(new GroupId(0), null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> personGroupDataView.isGroupMember(new GroupId(0), new PersonId(1000000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {

		GroupsActionSupport.testConsumer(10, 3, 5, 1999263877784730672L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			// show that the group ids match the expected group ids
			Set<GroupTypeId> groupTypeIds = personGroupDataView.getGroupTypeIds();
			assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupTypeIds);
		});
	}

	@Test
	@UnitTestMethod(name = "groupTypeIdExists", args = { GroupTypeId.class })
	public void testGroupTypeIdExists() {

		GroupsActionSupport.testConsumer(10, 3, 5, 1172766215251823083L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				assertTrue(personGroupDataView.groupTypeIdExists(testGroupTypeId));
			}
			assertFalse(personGroupDataView.groupTypeIdExists(TestGroupTypeId.getUnknownGroupTypeId()));
		});

	}

	@Test
	@UnitTestMethod(name = "getLastIssuedGroupId", args = {})
	public void testGetLastIssuedGroupId() {

		GroupsActionSupport.testConsumer(30, 0, 5, 8879265425057168597L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			Optional<GroupId> optional = personGroupDataView.getLastIssuedGroupId();
			assertFalse(optional.isPresent());

			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new GroupCreationEvent(testGroupTypeId));
				GroupId expectedGroupId = personGroupDataView.getLastIssuedGroupId().get();
				testGroupTypeId = testGroupTypeId.next();
				optional = personGroupDataView.getLastIssuedGroupId();
				assertTrue(optional.isPresent());
				GroupId actualGroupId = optional.get();
				assertEquals(expectedGroupId, actualGroupId);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyDefinition", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyDefinition() {

		GroupsActionSupport.testConsumer(10, 0, 5, 4462836951642761957L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// show that the personGroupDataManger has the expected property
			// definitions
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = personGroupDataView.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyIds", args = { GroupTypeId.class })
	public void testGetGroupPropertyIds() {

		GroupsActionSupport.testConsumer(10, 0, 5, 1205481410658607626L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// show that the personGroupDataManger returns the correct group
			// property ids
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
				Set<TestGroupPropertyId> expectedPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
				Set<GroupPropertyId> actualPropertyIds = personGroupDataView.getGroupPropertyIds(testGroupTypeId);
				assertEquals(expectedPropertyIds, actualPropertyIds);
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyIds(null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyIds(TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyValue() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Create a container to hold our expectations. The MultiKey will be
		 * (GroupId,GroupPropertyId) pairs and the Object will hold the most
		 * recent property value.
		 */
		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		/*
		 * At time = 1, have the agent establish the expected values.
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();

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
				List<GroupId> groupsForGroupType = personGroupDataView.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					Object value = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values and then set those properties to new values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);

				Object newValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, newValue));
				expectedValues.put(multiKey, newValue);
			}

		}));

		/*
		 * At time = 2, have the agent show that the property values still have
		 * their expected values.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			for (MultiKey multiKey : expectedValues.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				Object actualValue = personGroupDataView.getGroupPropertyValue(groupId, testGroupPropertyId);
				Object expectedValue = expectedValues.get(multiKey);
				assertEquals(expectedValue, actualValue);
			}

		}));

		/*
		 * precondition tests
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyValue(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is null
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyValue(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyValue(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();

			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyValue(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		}));

		GroupsActionSupport.testConsumers(30, 3, 5, 649112407534985381L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyExists", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyExists() {

		GroupsActionSupport.testConsumer(10, 0, 5, 8858123829776885259L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// show that the personGroupDataManger returns true for the group
			// properties that should be present
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				assertTrue(personGroupDataView.getGroupPropertyExists(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
			}

			// show that other group properties do not exits
			assertFalse(personGroupDataView.getGroupPropertyExists(null, null));
			assertFalse(personGroupDataView.getGroupPropertyExists(null, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertFalse(personGroupDataView.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), null));
			assertFalse(personGroupDataView.getGroupPropertyExists(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.getUnknownGroupPropertyId()));
		});

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyTime", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyTime() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

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
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			List<GroupId> groupIds = personGroupDataView.getGroupIds();

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
				List<GroupId> groupsForGroupType = personGroupDataView.getGroupsForGroupType(testGroupTypeId);
				for (GroupId groupId : groupsForGroupType) {
					double groupPropertyTime = personGroupDataView.getGroupPropertyTime(groupId, testGroupPropertyId);
					assertEquals(0.0, groupPropertyTime);
					expectedTimes.put(new MultiKey(groupId, testGroupPropertyId), new MutableDouble(1.0));
					c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator)));
				}
			}
		}));

		/*
		 * At time = 2, have the agent show that the property values were all
		 * set at time = 1 and then set those properties to new values
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = personGroupDataView.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);

				mutableDouble.setValue(2.0);
				c.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator)));
			}

		}));

		/*
		 * At time = 3, have the agent show that the property values were all
		 * set at time = 2
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			for (MultiKey multiKey : expectedTimes.keySet()) {
				GroupId groupId = multiKey.getKey(0);
				TestGroupPropertyId testGroupPropertyId = multiKey.getKey(1);
				double groupPropertyTime = personGroupDataView.getGroupPropertyTime(groupId, testGroupPropertyId);
				MutableDouble mutableDouble = expectedTimes.get(multiKey);
				assertEquals(mutableDouble.getValue(), groupPropertyTime);
			}

		}));

		/*
		 * precondition tests
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(4, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyTime(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is null
			contractException = assertThrows(ContractException.class,
					() -> personGroupDataView.getGroupPropertyTime(new GroupId(1000000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the group property id is null
			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyTime(new GroupId(0), null));
			assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

			// if the group property id is unknown
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();

			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyTime(groupId, TestGroupPropertyId.getUnknownGroupPropertyId()));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> personGroupDataView.getGroupPropertyTime(groupId, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
			assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		}));

		GroupsActionSupport.testConsumers(30, 3, 5, 7313144886869436931L, pluginBuilder.build());
	}

}
