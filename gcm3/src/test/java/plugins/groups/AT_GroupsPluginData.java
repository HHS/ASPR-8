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
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.PluginData;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

@UnitTest(target = GroupsPluginData.class)
public class AT_GroupsPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(GroupsPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// show that the builder does not return null
		assertNotNull(GroupsPluginData.builder().build());

		// show that the builder clears its state on build invocation
		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		GroupsPluginData groupInitialData = builder //
													.addPersonToGroup(new GroupId(0), new PersonId(0))//
													.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
													.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)//
													.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
															TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition())//
													.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true)//
													.build();//

		assertFalse(groupInitialData.getGroupIds().isEmpty());
		assertFalse(groupInitialData.getGroupTypeIds().isEmpty());

		groupInitialData = builder.build();
		assertTrue(groupInitialData.getGroupIds().isEmpty());
		assertTrue(groupInitialData.getGroupTypeIds().isEmpty());

		// precondition tests

		// if a person was added to a group that was not defined
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addPersonToGroup(new GroupId(0), new PersonId(0)).build());
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// if a group was added with a group type id that was not defined
		contractException = assertThrows(ContractException.class, () -> builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1).build());
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// if a group property definition was defined for a group type id that
		// was not defined.
		contractException = assertThrows(ContractException.class, () -> builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition()).build());
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// if a group property value was set for a group id that was not
		// defined.
		contractException = assertThrows(ContractException.class, () -> {
			builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
			builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
			builder.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
			builder.build();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// if a group property value is added for a group property id that is
		// not associated with the group.
		contractException = assertThrows(ContractException.class, () -> {
			builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
			builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
			builder.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 15);
			builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
			builder.build();
		});
		assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		// if a group property value is added that is incompatible with the
		// corresponding property definition
		contractException = assertThrows(ContractException.class, () -> {
			builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
			builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
			builder.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, 15);
			builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
			builder.build();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		// if a group property definition does not contain a default value
		contractException = assertThrows(ContractException.class, () -> {
			builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);

			builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, PropertyDefinition.builder().setType(Boolean.class).build());
			builder.build();
		});
		assertEquals(GroupError.PROPERTY_DEFINITION_REQUIRES_DEFAULT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addGroupTypeId", args = { GroupTypeId.class })
	public void testAddGroupTypeId() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}
		GroupsPluginData groupInitialData = builder.build();

		// show that the group type ids exist in the groupInitialData
		assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupInitialData.getGroupTypeIds());
	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addGroup", args = { GroupId.class, GroupTypeId.class })
	public void testAddGroup() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		int masterGroupId = 0;
		Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);

			GroupId groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupIds.add(groupId);

			groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupIds.add(groupId);
		}
		GroupsPluginData groupInitialData = builder.build();

		// show that the group ids that were added are present in the
		// groupInitialData
		assertEquals(expectedGroupIds, groupInitialData.getGroupIds());
	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "defineGroupProperty", args = { GroupTypeId.class, GroupPropertyId.class, PropertyDefinition.class })
	public void testDefineGroupProperty() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

			builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		GroupsPluginData groupInitialData = builder.build();

		// show that each property definition that was added is present in the
		// groupInitialData
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = groupInitialData.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}
	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(206512993284256660L);

		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		// add in the group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		// define the group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		// create a container to hold expected values
		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		/*
		 * Add a few groups and set about half of the property values, leaving
		 * the other half to be defined by the default values of the
		 * corresponding property definitions.
		 */
		TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		for (int i = 0; i < 10; i++) {
			GroupId groupId = new GroupId(i);
			builder.addGroup(groupId, testGroupTypeId);

			Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				if (randomGenerator.nextBoolean()) {
					Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setGroupPropertyValue(groupId, testGroupPropertyId, value);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				} else {
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), testGroupPropertyId.getPropertyDefinition().getDefaultValue().get());
				}
			}
			// move to the next group type id
			testGroupTypeId = testGroupTypeId.next();
		}

		// build the group initial data
		GroupsPluginData groupInitialData = builder.build();

		// show that the expected group property values are present
		for (MultiKey multiKey : expectedValues.keySet()) {
			GroupId groupId = multiKey.getKey(0);
			GroupPropertyId groupPropertyId = multiKey.getKey(1);
			Object expectedValue = expectedValues.get(multiKey);
			Object actualValue = groupInitialData.getGroupPropertyValue(groupId, groupPropertyId);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {

		Random random = new Random(7282493148489771700L);

		Map<GroupId, Set<PersonId>> expectedGroupAssignments = new LinkedHashMap<>();

		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		// add in the group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		// create some people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		/*
		 * Add a few groups and add to those groups 0 to 9 randomly selected
		 * people. Record the assignments in the expected data structure.
		 */
		TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		for (int i = 0; i < 20; i++) {
			// add the group
			GroupId groupId = new GroupId(i);
			builder.addGroup(groupId, testGroupTypeId);
			Set<PersonId> peopleInGroup = new LinkedHashSet<>();
			expectedGroupAssignments.put(groupId, peopleInGroup);
			testGroupTypeId = testGroupTypeId.next();

			// select some people and add them to the group
			Collections.shuffle(people, random);
			int count = random.nextInt(10);
			for (int j = 0; j < count; j++) {
				PersonId personId = people.get(j);
				builder.addPersonToGroup(groupId, personId);
				peopleInGroup.add(personId);
			}
		}

		// build the group initial data
		GroupsPluginData groupInitialData = builder.build();

		// show that the group memberships are as expected
		assertEquals(expectedGroupAssignments.keySet(), groupInitialData.getGroupIds());
		for (GroupId groupId : groupInitialData.getGroupIds()) {
			Set<PersonId> actualGroupMembers = groupInitialData.getGroupMembers(groupId);
			Set<PersonId> expectedGroupMembers = expectedGroupAssignments.get(groupId);
			assertEquals(expectedGroupMembers, actualGroupMembers);
		}

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyDefinition", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testGetGroupPropertyDefinition() {

		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

			builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		GroupsPluginData groupInitialData = builder.build();

		// show that each property definition that was added is present in the
		// groupInitialData
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
			PropertyDefinition actualPropertyDefinition = groupInitialData.getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
		}

		// precondition tests

		// if the group type id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> groupInitialData.getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// if the group type id is unknown
		contractException = assertThrows(ContractException.class,
				() -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		// if the group property id is null
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
		assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

		// if the group property id is not associated with the group type id via
		// a property definition
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.getUnknownGroupPropertyId()));
		assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		// if the group property id is not associated with the group type id via
		// a property definition
		contractException = assertThrows(ContractException.class,
				() -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
		assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyIds", args = { GroupTypeId.class })
	public void testGetGroupPropertyIds() {

		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		GroupsPluginData groupInitialData = builder.build();

		// show that the group properties for each group type match expectations
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			Set<GroupPropertyId> actualGroupPropertyIds = groupInitialData.getGroupPropertyIds(testGroupTypeId);
			Set<TestGroupPropertyId> expectedGroupPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
		}

		// precondition tests
		// if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyIds(null));
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// if the group type id is unknown
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyIds(TestGroupTypeId.getUnknownGroupTypeId()));
		assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class })
	public void testGetGroupPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8435308203966252001L);

		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		// add in the group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		// define the group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		// create a container to hold expected values
		Map<MultiKey, Object> expectedValues = new LinkedHashMap<>();

		/*
		 * Add a few groups and set about half of the property values, leaving
		 * the other half to be defined by the default values of the
		 * corresponding property definitions.
		 */
		TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		for (int i = 0; i < 10; i++) {
			GroupId groupId = new GroupId(i);
			builder.addGroup(groupId, testGroupTypeId);

			Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				if (randomGenerator.nextBoolean()) {
					Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
					builder.setGroupPropertyValue(groupId, testGroupPropertyId, value);
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), value);
				} else {
					expectedValues.put(new MultiKey(groupId, testGroupPropertyId), testGroupPropertyId.getPropertyDefinition().getDefaultValue().get());
				}
			}
			// move to the next group type id
			testGroupTypeId = testGroupTypeId.next();
		}

		// build the group initial data
		GroupsPluginData groupInitialData = builder.build();

		// show that the expected group property values are present
		for (MultiKey multiKey : expectedValues.keySet()) {
			GroupId groupId = multiKey.getKey(0);
			GroupPropertyId groupPropertyId = multiKey.getKey(1);
			Object expectedValue = expectedValues.get(multiKey);
			Object actualValue = groupInitialData.getGroupPropertyValue(groupId, groupPropertyId);
			assertEquals(expectedValue, actualValue);
		}

		// precondition tests

		// if the group id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> groupInitialData.getGroupPropertyValue(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyValue(new GroupId(10000), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		// if the group property id is null
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyValue(new GroupId(0), null));
		assertEquals(GroupError.NULL_GROUP_PROPERTY_ID, contractException.getErrorType());

		// if the group property id is not associated with the group type id via
		// a property definition
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyValue(new GroupId(0), TestGroupPropertyId.getUnknownGroupPropertyId()));
		assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
		assertEquals(GroupError.UNKNOWN_GROUP_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGroupTypeIds", args = {})
	public void testGetGroupTypeIds() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}
		GroupsPluginData groupInitialData = builder.build();

		// show that the group type ids exist in the groupInitialData
		assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupInitialData.getGroupTypeIds());
	}

	@Test
	@UnitTestMethod(name = "getGroupIds", args = {})
	public void testGetGroupIds() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		int masterGroupId = 0;
		Set<GroupId> expectedGroupIds = new LinkedHashSet<>();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);

			GroupId groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupIds.add(groupId);

			groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupIds.add(groupId);
		}
		GroupsPluginData groupInitialData = builder.build();

		// show that the group ids that were added are present in the
		// groupInitialData
		assertEquals(expectedGroupIds, groupInitialData.getGroupIds());

	}

	@Test
	@UnitTestMethod(name = "getGroupMembers", args = { GroupId.class })
	public void testGetGroupMembers() {

		Random random = new Random(4685636461674441597L);

		Map<GroupId, Set<PersonId>> expectedGroupAssignments = new LinkedHashMap<>();

		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		// add in the group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);
		}

		// create some people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			people.add(new PersonId(i));
		}

		/*
		 * Add a few groups and add to those groups 0 to 9 randomly selected
		 * people. Record the assignments in the expected data structure.
		 */
		TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		for (int i = 0; i < 20; i++) {
			// add the group
			GroupId groupId = new GroupId(i);
			builder.addGroup(groupId, testGroupTypeId);
			Set<PersonId> peopleInGroup = new LinkedHashSet<>();
			expectedGroupAssignments.put(groupId, peopleInGroup);
			testGroupTypeId = testGroupTypeId.next();

			// select some people and add them to the group
			Collections.shuffle(people, random);
			int count = random.nextInt(10);
			for (int j = 0; j < count; j++) {
				PersonId personId = people.get(j);
				builder.addPersonToGroup(groupId, personId);
				peopleInGroup.add(personId);
			}
		}

		// build the group initial data
		GroupsPluginData groupInitialData = builder.build();

		// show that the group memberships are as expected
		assertEquals(expectedGroupAssignments.keySet(), groupInitialData.getGroupIds());
		for (GroupId groupId : groupInitialData.getGroupIds()) {
			Set<PersonId> actualGroupMembers = groupInitialData.getGroupMembers(groupId);
			Set<PersonId> expectedGroupMembers = expectedGroupAssignments.get(groupId);
			assertEquals(expectedGroupMembers, actualGroupMembers);
		}

		// precondition tests

		// if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupMembers(null));
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupMembers(new GroupId(100000)));
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getGroupTypeId", args = { GroupId.class })
	public void testGetGroupTypeId() {

		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		int masterGroupId = 0;
		Map<GroupId, GroupTypeId> expectedGroupTypes = new LinkedHashMap<>();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			builder.addGroupTypeId(testGroupTypeId);

			GroupId groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupTypes.put(groupId, testGroupTypeId);

			groupId = new GroupId(masterGroupId++);
			builder.addGroup(groupId, testGroupTypeId);
			expectedGroupTypes.put(groupId, testGroupTypeId);
		}
		GroupsPluginData groupInitialData = builder.build();

		for (GroupId groupId : expectedGroupTypes.keySet()) {
			GroupTypeId expecctedGroupTypeId = expectedGroupTypes.get(groupId);
			GroupTypeId actualGroupTypeId = groupInitialData.getGroupTypeId(groupId);
			assertEquals(expecctedGroupTypeId, actualGroupTypeId);
		}

		// precondition tests

		// if the group id is null
		ContractException contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupTypeId(null));
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// if the group id is unknown
		contractException = assertThrows(ContractException.class, () -> groupInitialData.getGroupTypeId(new GroupId(1000000)));
		assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9130589441333999144L);
		
		GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
		}
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}
		int groupCount = 10;
		List<GroupId> groups = new ArrayList<>();
		for (int i = 0; i < groupCount; i++) {
			GroupId groupId = new GroupId(i);
			groups.add(groupId);
			TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
			groupPluginDataBuilder.addGroup(groupId, groupTypeId);
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.getTestGroupPropertyIds(groupTypeId)) {
				if (randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
					groupPluginDataBuilder.setGroupPropertyValue(groupId, testGroupPropertyId, randomPropertyValue);
				}
			}
		}

		int personCount = 100;

		for (int i = 0; i < personCount; i++) {
			PersonId personId = new PersonId(i);
			int numberOfGroups = randomGenerator.nextInt(5);
			Collections.sort(groups);
			for (int j = 0; j < numberOfGroups; j++) {
				GroupId groupId = groups.get(j);
				groupPluginDataBuilder.addPersonToGroup(groupId, personId);
			}
		}
		GroupsPluginData groupsPluginData = groupPluginDataBuilder.build();

		PluginData pluginData = groupsPluginData.getCloneBuilder().build();

		// show that the clone plugin data has the correct type
		assertTrue(pluginData instanceof GroupsPluginData);

		GroupsPluginData cloneGroupPluginData = (GroupsPluginData) pluginData;

		// show that the two plugin datas have the same groups
		assertEquals(groupsPluginData.getGroupIds(), cloneGroupPluginData.getGroupIds());

		// show that the two plugin datas have the same group types
		assertEquals(groupsPluginData.getGroupTypeIds(), cloneGroupPluginData.getGroupTypeIds());

		// show that the two plugin datas have the same group property ids
		for (GroupTypeId groupTypeId : groupsPluginData.getGroupTypeIds()) {
			assertEquals(groupsPluginData.getGroupPropertyIds(groupTypeId), cloneGroupPluginData.getGroupPropertyIds(groupTypeId));
			//show that the two plugin datas have the same group property definitions
			for(GroupPropertyId  groupPropertyId : groupsPluginData.getGroupPropertyIds(groupTypeId)) {
				PropertyDefinition expectedPropertyDefinition = groupsPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				PropertyDefinition actualPropertyDefinition = cloneGroupPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				assertEquals(expectedPropertyDefinition,actualPropertyDefinition);
			}
		}
		
		// show that the two plugin datas have the same groups
		assertEquals(groupsPluginData.getGroupIds(), cloneGroupPluginData.getGroupIds());
		
		//show that the groups have the same types
		for(GroupId  groupId : groupsPluginData.getGroupIds()) {
			GroupTypeId expectedGroupTypeId = groupsPluginData.getGroupTypeId(groupId);
			GroupTypeId actualGroupTypeId = cloneGroupPluginData.getGroupTypeId(groupId);
			assertEquals(expectedGroupTypeId, actualGroupTypeId);
			//show that the groups have the property values
			for(GroupPropertyId  groupPropertyId : groupsPluginData.getGroupPropertyIds(expectedGroupTypeId)) {
				Object expectedPropertyValue = groupsPluginData.getGroupPropertyValue(groupId, groupPropertyId);
				Object actualPropertyValue = cloneGroupPluginData.getGroupPropertyValue(groupId, groupPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);
			}
			//show that the groups have the members
			Set<PersonId> expectedGroupMembers = groupsPluginData.getGroupMembers(groupId);
			Set<PersonId> actualGroupMembers = cloneGroupPluginData.getGroupMembers(groupId);
			assertEquals(expectedGroupMembers, actualGroupMembers);
		}
	}

}