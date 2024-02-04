package gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsPluginData.GroupSpecification;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyValue;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_GroupsPluginData {

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GroupsPluginData.builder());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "build", args = {})
    public void testBuild() {
        // show that the builder does not return null
        assertNotNull(GroupsPluginData.builder().build());

        // show that the builder clears its state on build invocation

        GroupsPluginData groupInitialData = GroupsPluginData.builder() //
                .associatePersonToGroup(new GroupId(0), new PersonId(0))//
                .addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
                .addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)//
                .defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1,
                        TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                        TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition())//
                .setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                        true)//
                .build();//

        assertFalse(groupInitialData.getGroupIds().isEmpty());
        assertFalse(groupInitialData.getGroupTypeIds().isEmpty());

        groupInitialData = GroupsPluginData.builder().build();
        assertTrue(groupInitialData.getGroupIds().isEmpty());
        assertTrue(groupInitialData.getGroupTypeIds().isEmpty());

        // precondition test: if a person was added to a group that was not
        // defined
        ContractException contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.associatePersonToGroup(new GroupId(0), new PersonId(0)).build();
        });
        assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

        // precondition test: if a group was added with a group type id that was
        // not
        // defined
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1).build();
        });
        assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

        // precondition test: if a group property definition was defined for a
        // group
        // type id that
        // was not defined.
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, //
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, //
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition()//
            ).build();
        });

        assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

        // precondition test: if a group property value was set for a group id
        // that was
        // not
        // defined.
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
            builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
            builder.setGroupPropertyValue(new GroupId(0),
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
            builder.build();
        });
        assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

        // precondition test: if a group property value is added for a group
        // property id
        // that is
        // not associated with the group.
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
            builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
            builder.setGroupPropertyValue(new GroupId(0),
                    TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 15);
            builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
            builder.build();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if a group property value is added that is
        // incompatible
        // with the
        // corresponding property definition
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
            builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition());
            builder.setGroupPropertyValue(new GroupId(0),
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, 15);
            builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
            builder.build();
        });
        assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

        /*
         * precondition test: if a group does not have a group property value assigned
         * when the corresponding property definition lacks a default value.
         */
        contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.Builder builder = GroupsPluginData.builder();
            builder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1);
            builder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1,
                    TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK,
                    PropertyDefinition.builder().setType(Boolean.class).build());
            builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
            builder.build();
        });
        assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addGroupTypeId", args = { GroupTypeId.class })
    public void testAddGroupTypeId() {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
            builder.addGroupTypeId(testGroupTypeId);
        }
        GroupsPluginData groupInitialData = builder.build();

        // show that the group type ids exist in the groupInitialData
        assertEquals(EnumSet.allOf(TestGroupTypeId.class), groupInitialData.getGroupTypeIds());

        // precondition test: if the group type id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().addGroupTypeId(null));
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addGroup", args = { GroupId.class,
            GroupTypeId.class })
    public void testAddGroup() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6428717083105095287L);
        GroupsPluginData.Builder builder = GroupsPluginData.builder();
        Map<GroupTypeId, Set<GroupId>> expectedGroupIds = new LinkedHashMap<>();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            expectedGroupIds.put(testGroupTypeId, new LinkedHashSet<>());
            builder.addGroupTypeId(testGroupTypeId);
        }

        for (int i = 0; i < 100; i++) {
            TestGroupTypeId randomGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);

            GroupId groupId = new GroupId(i*2+1);
            builder.addGroup(groupId, randomGroupTypeId);
            // adding duplicate group data to show the last value persists
            randomGroupTypeId = randomGroupTypeId.next();
            builder.addGroup(groupId, randomGroupTypeId);
            expectedGroupIds.get(randomGroupTypeId).add(groupId);
        }

        Map<GroupTypeId, Set<GroupId>> actualGroupIds = new LinkedHashMap<>();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            actualGroupIds.put(testGroupTypeId, new LinkedHashSet<>());
        }

        GroupsPluginData groupsPluginData = builder.build();
        for (GroupId groupId : groupsPluginData.getGroupIds()) {
            GroupTypeId groupTypeId = groupsPluginData.getGroupTypeId(groupId);
            actualGroupIds.get(groupTypeId).add(groupId);
        }

        // show that the group ids that were added are present in the
        // groupInitialData
        assertEquals(expectedGroupIds, actualGroupIds);

        // precondition test: if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().addGroup(null, TestGroupTypeId.GROUP_TYPE_1));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // precondition test: if the group type id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().addGroup(new GroupId(0), null));
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "defineGroupProperty", args = { GroupTypeId.class,
            GroupPropertyId.class, PropertyDefinition.class })
    public void testDefineGroupProperty() {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // showing that duplicate values persist
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            PropertyDefinition propertyDefinition = testGroupPropertyId.next().getPropertyDefinition();
            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    propertyDefinition);
            propertyDefinition = testGroupPropertyId.getPropertyDefinition();
            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    propertyDefinition);
        }

        GroupsPluginData groupInitialData = builder.build();

        // show that each property definition that was added is present in the
        // groupInitialData
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = groupInitialData
                    .getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
        }

        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        TestGroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
        PropertyDefinition propertyDefinition = groupPropertyId.getPropertyDefinition();

        // precondition test: if the group type id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().defineGroupProperty(null, groupPropertyId, propertyDefinition));
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

        // precondition test: if the group property id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().defineGroupProperty(testGroupTypeId, null, propertyDefinition));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if the property definition is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().defineGroupProperty(testGroupTypeId, groupPropertyId, null));
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "setGroupPropertyValue", args = { GroupId.class,
            GroupPropertyId.class, Object.class })
    public void testSetGroupPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(206512993284256660L);

        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        // add in the group types
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // define the group properties
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        // create a container to hold expected values
        Set<MultiKey> expectedValues = new LinkedHashSet<>();

        /*
         * Add a few groups and set about half of the property values, leaving the other
         * half to be defined by the default values of the corresponding property
         * definitions.
         */
        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < 10; i++) {
            GroupId groupId = new GroupId(i);
            builder.addGroup(groupId, testGroupTypeId);

            Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId
                    .getTestGroupPropertyIds(testGroupTypeId);
            for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
                if (randomGenerator.nextBoolean()) {
                    Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
                    builder.setGroupPropertyValue(groupId, testGroupPropertyId, value);
                    value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
                    builder.setGroupPropertyValue(groupId, testGroupPropertyId, value);
                    expectedValues.add(new MultiKey(groupId, testGroupPropertyId, value));
                }
            }
            // move to the next group type id
            testGroupTypeId = testGroupTypeId.next();
        }

        // build the group initial data
        GroupsPluginData groupInitialData = builder.build();

        // show that the expected group property values are present
        Set<MultiKey> actualValues = new LinkedHashSet<>();
        for (GroupId groupId : groupInitialData.getGroupIds()) {
            for (GroupPropertyValue groupPropertyValue : groupInitialData.getGroupPropertyValues(groupId)) {
                MultiKey multiKey = new MultiKey(groupId, groupPropertyValue.groupPropertyId(),
                        groupPropertyValue.value());
                actualValues.add(multiKey);
            }
        }

        assertEquals(expectedValues, actualValues);

        TestGroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;

        // precondition test: if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().setGroupPropertyValue(null, groupPropertyId, 10));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // precondition test: if the group property id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().setGroupPropertyValue(new GroupId(0), null, 10));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if the group property value is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().setGroupPropertyValue(new GroupId(0), groupPropertyId, null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "setNextGroupIdValue", args = { int.class })
    public void testSetNextGroupIdValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2784010859295212357L);

        GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
        }
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        int nextGroupId = randomGenerator.nextInt(100);
        groupPluginDataBuilder.setNextGroupIdValue(nextGroupId);

        GroupsPluginData pluginData = groupPluginDataBuilder.build();

        assertEquals(nextGroupId, pluginData.getNextGroupIdValue());

        // preconditions:
        // value is negative
        ContractException contractException = assertThrows(ContractException.class, () -> {
            GroupsPluginData.builder().setNextGroupIdValue(-1);
        });

        assertEquals(GroupError.NEGATIVE_GROUP_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "associatePersonToGroup", args = { GroupId.class,
            PersonId.class })
    public void testAssociatePersonToGroup() {

        Random random = new Random(7282493148489771700L);

        Map<MultiKey, MutableInteger> expectedGroupAssignments = new LinkedHashMap<>();

        GroupsPluginData.Builder builder = GroupsPluginData.builder();
        // add in the group types
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // create some people
        List<Integer> personIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            personIds.add(i);
        }

        /*
         * Add a few groups and add to those groups 0 to 9 randomly selected people.
         * Record the assignments in the expected data structure.
         */
        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < 20; i++) {
            // add the group

            builder.addGroup(new GroupId(i), testGroupTypeId);
            testGroupTypeId = testGroupTypeId.next();
            // select some people and add them to the group
            Collections.shuffle(personIds, random);
            int count = random.nextInt(10);
            for (int j = 0; j < count; j++) {
                builder.associatePersonToGroup(new GroupId(i), new PersonId(personIds.get(j)));
                MultiKey multiKey = new MultiKey(new GroupId(i), new PersonId(personIds.get(j)));
                expectedGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                expectedGroupAssignments.get(multiKey).increment();
            }
        }

        // build the group initial data
        GroupsPluginData groupsPluginData = builder.build();

        // show that the group memberships are as expected
        Map<MultiKey, MutableInteger> actualGroupAssignments = new LinkedHashMap<>();

        for (int i = 0; i < groupsPluginData.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            for (GroupId groupId : groupsPluginData.getGroupsForPerson(personId)) {
                MultiKey multiKey = new MultiKey(groupId, personId);
                actualGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                actualGroupAssignments.get(multiKey).increment();
            }
        }

        assertEquals(expectedGroupAssignments, actualGroupAssignments);

        // precondition test: if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(null, new PersonId(0)));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // precondition test: if the person id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(new GroupId(0), null));
        assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupPropertyDefinition", args = { GroupTypeId.class,
            GroupPropertyId.class })
    public void testGetGroupPropertyDefinition() {

        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        GroupsPluginData groupInitialData = builder.build();

        // show that each property definition that was added is present in the
        // groupInitialData
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            PropertyDefinition expectedPropertyDefinition = testGroupPropertyId.getPropertyDefinition();
            PropertyDefinition actualPropertyDefinition = groupInitialData
                    .getGroupPropertyDefinition(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
        }

        // precondition tests

        // if the group type id is null
        ContractException contractException = assertThrows(ContractException.class, () -> groupInitialData
                .getGroupPropertyDefinition(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

        // if the group type id is unknown
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.getUnknownGroupTypeId(),
                        TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
        assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

        // if the group property id is null
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1, null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // if the group property id is not associated with the group type id via
        // a property definition
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1,
                        TestGroupPropertyId.getUnknownGroupPropertyId()));
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

        // if the group property id is not associated with the group type id via
        // a property definition
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyDefinition(TestGroupTypeId.GROUP_TYPE_1,
                        TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK));
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupPropertyIds", args = { GroupTypeId.class })
    public void testGetGroupPropertyIds() {

        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        GroupsPluginData groupInitialData = builder.build();

        // show that the group properties for each group type match expectations
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            Set<GroupPropertyId> actualGroupPropertyIds = groupInitialData.getGroupPropertyIds(testGroupTypeId);
            Set<TestGroupPropertyId> expectedGroupPropertyIds = TestGroupPropertyId
                    .getTestGroupPropertyIds(testGroupTypeId);
            assertEquals(expectedGroupPropertyIds, actualGroupPropertyIds);
        }

        // precondition tests
        // if the group type id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyIds(null));
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

        // if the group type id is unknown
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupPropertyIds(TestGroupTypeId.getUnknownGroupTypeId()));
        assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupPropertyValues", args = { GroupId.class })
    public void testGetGroupPropertyValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8435308203966252001L);

        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        // add in the group types
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // define the group properties
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            builder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        // create a container to hold expected values
        Set<MultiKey> expectedValues = new LinkedHashSet<>();

        /*
         * Add a few groups and set about half of the property values, leaving the other
         * half to be defined by the default values of the corresponding property
         * definitions.
         */
        int groupCount = 10;

        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < groupCount; i++) {
            GroupId groupId = new GroupId(i);
            builder.addGroup(groupId, testGroupTypeId);

            Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId
                    .getTestGroupPropertyIds(testGroupTypeId);
            for (TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
                if (randomGenerator.nextBoolean()) {
                    Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
                    builder.setGroupPropertyValue(groupId, testGroupPropertyId, value);
                    expectedValues.add(new MultiKey(groupId, testGroupPropertyId, value));
                }
            }
            // move to the next group type id
            testGroupTypeId = testGroupTypeId.next();
        }

        // build the group initial data
        GroupsPluginData groupsPluginData = builder.build();

        // show that the expected group property values are present
        Set<MultiKey> actualValues = new LinkedHashSet<>();
        for (GroupId groupId : groupsPluginData.getGroupIds()) {
            for (GroupPropertyValue groupPropertyValue : groupsPluginData.getGroupPropertyValues(groupId)) {
                MultiKey multiKey = new MultiKey(groupId, groupPropertyValue.groupPropertyId(),
                        groupPropertyValue.value());
                actualValues.add(multiKey);
            }
        }

        assertEquals(expectedValues, actualValues);

        // precondition tests

        // if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> groupsPluginData.getGroupPropertyValues(null));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // if the group id is unknown
        contractException = assertThrows(ContractException.class,
                () -> groupsPluginData.getGroupPropertyValues(new GroupId(10000)));
        assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupTypeIds", args = {})
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
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupIds", args = {})
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
        assertEquals(expectedGroupIds, new LinkedHashSet<>(groupInitialData.getGroupIds()));

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupsForPerson", args = { PersonId.class })
    public void testGetGroupsForPerson() {

        Random random = new Random(4685636461674441597L);

        Set<MultiKey> expectedGroupAssignments = new LinkedHashSet<>();

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
         * Add a few groups and add to those groups 0 to 9 randomly selected people.
         * Record the assignments in the expected data structure.
         */
        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < 20; i++) {
            // add the group
            GroupId groupId = new GroupId(i);
            builder.addGroup(groupId, testGroupTypeId);

            testGroupTypeId = testGroupTypeId.next();

            // select some people and add them to the group
            Collections.shuffle(people, random);
            int count = random.nextInt(10);
            for (int j = 0; j < count; j++) {
                PersonId personId = people.get(j);
                builder.associatePersonToGroup(groupId, personId);
                MultiKey multiKey = new MultiKey(groupId, personId);
                expectedGroupAssignments.add(multiKey);
            }
        }

        // build the group initial data
        GroupsPluginData groupInitialData = builder.build();

        // show that the group memberships are as expected
        Set<MultiKey> actualGroupAssignments = new LinkedHashSet<>();
        for (int i = 0; i < groupInitialData.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            for (GroupId groupId : groupInitialData.getGroupsForPerson(personId)) {
                MultiKey multiKey = new MultiKey(groupId, personId);
                actualGroupAssignments.add(multiKey);
            }
        }

        assertEquals(expectedGroupAssignments, actualGroupAssignments);

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupTypeId", args = { GroupId.class })
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
        ContractException contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupTypeId(null));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // if the group id is unknown
        contractException = assertThrows(ContractException.class,
                () -> groupInitialData.getGroupTypeId(new GroupId(1000000)));
        assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getCloneBuilder", args = {})
    public void testGetCloneBuilder() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9130589441333999144L);

        GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
        }
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
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
                groupPluginDataBuilder.associatePersonToGroup(groupId, personId);
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
            assertEquals(groupsPluginData.getGroupPropertyIds(groupTypeId),
                    cloneGroupPluginData.getGroupPropertyIds(groupTypeId));
            // show that the two plugin datas have the same group property
            // definitions
            for (GroupPropertyId groupPropertyId : groupsPluginData.getGroupPropertyIds(groupTypeId)) {
                PropertyDefinition expectedPropertyDefinition = groupsPluginData.getGroupPropertyDefinition(groupTypeId,
                        groupPropertyId);
                PropertyDefinition actualPropertyDefinition = cloneGroupPluginData
                        .getGroupPropertyDefinition(groupTypeId, groupPropertyId);
                assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
            }
        }

        // show that the two plugin datas have the same groups
        assertEquals(groupsPluginData.getGroupIds(), cloneGroupPluginData.getGroupIds());

        // show that the groups have the same types
        for (GroupId groupId : groupsPluginData.getGroupIds()) {
            GroupTypeId expectedGroupTypeId = groupsPluginData.getGroupTypeId(groupId);
            GroupTypeId actualGroupTypeId = cloneGroupPluginData.getGroupTypeId(groupId);
            assertEquals(expectedGroupTypeId, actualGroupTypeId);
            // show that the groups have the property values
            Map<GroupPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
            Map<GroupPropertyId, Object> actualPropertyValues = new LinkedHashMap<>();
            for (GroupPropertyValue groupPropertyValue : groupsPluginData.getGroupPropertyValues(groupId)) {
                GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
                Object value = groupPropertyValue.value();
                expectedPropertyValues.put(groupPropertyId, value);
            }
            for (GroupPropertyValue groupPropertyValue : cloneGroupPluginData.getGroupPropertyValues(groupId)) {
                GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
                Object value = groupPropertyValue.value();
                actualPropertyValues.put(groupPropertyId, value);
            }
            assertEquals(expectedPropertyValues, actualPropertyValues);

            // show that the groups have the members
            assertEquals(groupsPluginData.getPersonCount(), cloneGroupPluginData.getPersonCount());
            for (int i = 0; i < groupsPluginData.getPersonCount(); i++) {
                PersonId personId = new PersonId(i);
                Set<GroupId> expectedGroups = new LinkedHashSet<>(groupsPluginData.getGroupsForPerson(personId));
                Set<GroupId> actualGroups = new LinkedHashSet<>(cloneGroupPluginData.getGroupsForPerson(personId));
                assertEquals(expectedGroups, actualGroups);
            }
        }
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getPersonCount", args = {})
    public void testGetPersonCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7910883193674079461L);

        GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
        }
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
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

        int totalPeopleCount = 0;
        for (GroupId groupId : groups) {
            int peopleCount = randomGenerator.nextInt(100);
            int currTotalPeopleCount = totalPeopleCount;
            totalPeopleCount += peopleCount;
            for (int i = currTotalPeopleCount; i < totalPeopleCount; i++) {
                PersonId personId = new PersonId(i);
                groupPluginDataBuilder.associatePersonToGroup(groupId, personId);
            }
        }

        GroupsPluginData groupsPluginData = groupPluginDataBuilder.build();

        // show that the total people count is NOT 0
        assertTrue(totalPeopleCount > 0);

        // show that the group person count is equal to the total people count
        assertEquals(totalPeopleCount, groupsPluginData.getPersonCount());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getNextGroupIdValue", args = {})
    public void testGetNextGroupIdValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2784010859295212357L);

        GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
        }
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
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

        GroupsPluginData pluginData = groupPluginDataBuilder.build();

        assertEquals(groupCount, pluginData.getNextGroupIdValue());

        pluginData = groupPluginDataBuilder.setNextGroupIdValue(groupCount + 10).build();

        assertEquals(groupCount + 10, pluginData.getNextGroupIdValue());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupCount", args = {})
    public void testGetGroupCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(669154335225022584L);

        for (int i = 0; i < 10; i++) {
            GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();

            for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.getShuffledTestGroupTypeIds(randomGenerator)) {
                groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
            }

            int groupCount = randomGenerator.nextInt(50) + 10;
            List<GroupId> groups = new ArrayList<>();
            for (int j = 0; j < groupCount; j++) {
                GroupId groupId = new GroupId(j);
                groups.add(groupId);
                TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
                groupPluginDataBuilder.addGroup(groupId, groupTypeId);
            }

            int totalPeopleCount = 0;
            for (GroupId groupId : groups) {
                int peopleCount = randomGenerator.nextInt(100);
                int currTotalPeopleCount = totalPeopleCount;
                totalPeopleCount += peopleCount;
                for (int j = currTotalPeopleCount; j < totalPeopleCount; j++) {
                    PersonId personId = new PersonId(j);
                    groupPluginDataBuilder.associatePersonToGroup(groupId, personId);
                }
            }

            GroupsPluginData pluginData = groupPluginDataBuilder.build();

            assertEquals(groupCount, pluginData.getGroupCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getPeopleForGroup", args = { GroupId.class })
    public void testGetPeopleForGroup() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4825370554814301264L);

        for (int i = 0; i < 10; i++) {
            GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();

            List<List<PersonId>> groupToPeopleMemberships = new ArrayList<>();

            for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.getShuffledTestGroupTypeIds(randomGenerator)) {
                groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
            }

            int groupCount = 10;
            List<GroupId> groups = new ArrayList<>();
            for (int j = 0; j < groupCount; j++) {
                GroupId groupId = new GroupId(j);
                groups.add(groupId);
                TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
                groupPluginDataBuilder.addGroup(groupId, groupTypeId);
            }

            int totalPeopleCount = 0;
            for (GroupId groupId : groups) {
                int peopleCount = randomGenerator.nextInt(100);
                int currTotalPeopleCount = totalPeopleCount;
                totalPeopleCount += peopleCount;
                for (int j = currTotalPeopleCount; j < totalPeopleCount; j++) {
                    PersonId personId = new PersonId(j);
                    groupPluginDataBuilder.associatePersonToGroup(groupId, personId);

                    int groupIndex = groupId.getValue();

                    while (groupIndex >= groupToPeopleMemberships.size()) {
                        groupToPeopleMemberships.add(null);
                    }

                    List<PersonId> people = groupToPeopleMemberships.get(groupIndex);
                    if (people == null) {
                        people = new ArrayList<>();
                        groupToPeopleMemberships.set(groupIndex, people);
                    }

                    people.add(personId);
                }
            }

            GroupsPluginData pluginData = groupPluginDataBuilder.build();

            for (int j = 0; j < groupToPeopleMemberships.size(); j++) {
                List<PersonId> personIds = groupToPeopleMemberships.get(j);

                if (personIds == null) {
                    assertEquals(Collections.unmodifiableList(new ArrayList<>()),
                            pluginData.getPeopleForGroup(new GroupId(j)));
                } else {
                    assertEquals(personIds, pluginData.getPeopleForGroup(new GroupId(j)));
                }
            }
        }
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "getGroupPropertyDefinitions", args = {})
    public void testGetPropertyDefinitions() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9107021630953219230L);

        for (int i = 0; i < 10; i++) {
            GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();

            Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();

            for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.getShuffledTestGroupTypeIds(randomGenerator)) {
                groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);

                for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                        .getShuffledTestGroupPropertyIds(testGroupTypeId, randomGenerator)) {
                    groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(),
                            testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());

                    Map<GroupPropertyId, PropertyDefinition> propertyDefinitionsMap = groupPropertyDefinitions
                            .get(testGroupTypeId);
                    if (propertyDefinitionsMap == null) {
                        propertyDefinitionsMap = new LinkedHashMap<>();
                        groupPropertyDefinitions.put(testGroupTypeId, propertyDefinitionsMap);
                    }
                    propertyDefinitionsMap.put(testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
                }
            }

            GroupsPluginData pluginData = groupPluginDataBuilder.build();

            assertEquals(groupPropertyDefinitions, pluginData.getGroupPropertyDefinitions());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(386194196593528301L);

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        long sameSeed = randomGenerator.nextLong();
        GroupsPluginData groupsPluginData1 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                sameSeed);
        GroupsPluginData groupsPluginData2 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData3 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 5, people,
                sameSeed);
        GroupsPluginData groupsPluginData4 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 5, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData5 = GroupsTestPluginFactory.getStandardGroupsPluginData(1, 10, people,
                sameSeed);
        GroupsPluginData groupsPluginData6 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 10, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData7 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 5, people,
                sameSeed);
        GroupsPluginData groupsPluginData8 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 5, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData9 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                sameSeed);

        assertEquals(groupsPluginData1.hashCode(), groupsPluginData1.hashCode());

        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData2.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData3.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData4.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData5.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData6.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData3.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData4.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData5.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData6.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData1.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData3.hashCode(), groupsPluginData4.hashCode());
        assertNotEquals(groupsPluginData3.hashCode(), groupsPluginData5.hashCode());
        assertNotEquals(groupsPluginData3.hashCode(), groupsPluginData6.hashCode());
        assertNotEquals(groupsPluginData3.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData3.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData4.hashCode(), groupsPluginData5.hashCode());
        assertNotEquals(groupsPluginData4.hashCode(), groupsPluginData6.hashCode());
        assertNotEquals(groupsPluginData4.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData4.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData5.hashCode(), groupsPluginData6.hashCode());
        assertNotEquals(groupsPluginData5.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData5.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData6.hashCode(), groupsPluginData7.hashCode());
        assertNotEquals(groupsPluginData6.hashCode(), groupsPluginData8.hashCode());

        assertNotEquals(groupsPluginData7.hashCode(), groupsPluginData8.hashCode());

        assertEquals(groupsPluginData1.hashCode(), groupsPluginData9.hashCode());
    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "equals", args = { Object.class })
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1974882207275712576L);

        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        long sameSeed = randomGenerator.nextLong();
        GroupsPluginData groupsPluginData1 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                sameSeed);
        GroupsPluginData groupsPluginData2 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData3 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 5, people,
                sameSeed);
        GroupsPluginData groupsPluginData4 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 5, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData5 = GroupsTestPluginFactory.getStandardGroupsPluginData(1, 10, people,
                sameSeed);
        GroupsPluginData groupsPluginData6 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 10, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData7 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 5, people,
                sameSeed);
        GroupsPluginData groupsPluginData8 = GroupsTestPluginFactory.getStandardGroupsPluginData(3, 5, people,
                randomGenerator.nextLong());
        GroupsPluginData groupsPluginData9 = GroupsTestPluginFactory.getStandardGroupsPluginData(2, 10, people,
                sameSeed);

        assertEquals(groupsPluginData1, groupsPluginData1);

        assertNotEquals(groupsPluginData1, null);

        assertNotEquals(groupsPluginData1, new Object());

        assertNotEquals(groupsPluginData1, groupsPluginData2);
        assertNotEquals(groupsPluginData1, groupsPluginData3);
        assertNotEquals(groupsPluginData1, groupsPluginData4);
        assertNotEquals(groupsPluginData1, groupsPluginData5);
        assertNotEquals(groupsPluginData1, groupsPluginData6);
        assertNotEquals(groupsPluginData1, groupsPluginData7);
        assertNotEquals(groupsPluginData1, groupsPluginData8);

        assertNotEquals(groupsPluginData1, groupsPluginData3);
        assertNotEquals(groupsPluginData1, groupsPluginData4);
        assertNotEquals(groupsPluginData1, groupsPluginData5);
        assertNotEquals(groupsPluginData1, groupsPluginData6);
        assertNotEquals(groupsPluginData1, groupsPluginData7);
        assertNotEquals(groupsPluginData1, groupsPluginData8);

        assertNotEquals(groupsPluginData3, groupsPluginData4);
        assertNotEquals(groupsPluginData3, groupsPluginData5);
        assertNotEquals(groupsPluginData3, groupsPluginData6);
        assertNotEquals(groupsPluginData3, groupsPluginData7);
        assertNotEquals(groupsPluginData3, groupsPluginData8);

        assertNotEquals(groupsPluginData4, groupsPluginData5);
        assertNotEquals(groupsPluginData4, groupsPluginData6);
        assertNotEquals(groupsPluginData4, groupsPluginData7);
        assertNotEquals(groupsPluginData4, groupsPluginData8);

        assertNotEquals(groupsPluginData5, groupsPluginData6);
        assertNotEquals(groupsPluginData5, groupsPluginData7);
        assertNotEquals(groupsPluginData5, groupsPluginData8);

        assertNotEquals(groupsPluginData6, groupsPluginData7);
        assertNotEquals(groupsPluginData6, groupsPluginData8);

        assertNotEquals(groupsPluginData7, groupsPluginData8);

        assertEquals(groupsPluginData1, groupsPluginData9);

    }

    @Test
    @UnitTestMethod(target = GroupsPluginData.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(523034536833646355L);

        for (int i = 0; i < 10; i++) {
            GroupsPluginData.Builder groupPluginDataBuilder = GroupsPluginData.builder();

            int nextGroupIdValue = -1;
            Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();
            Set<GroupTypeId> groupTypeIds = new LinkedHashSet<>();
            List<List<GroupId>> personToGroupsMemberships = new ArrayList<>();
            List<List<PersonId>> groupToPeopleMemberships = new ArrayList<>();
            List<GroupSpecification> groupSpecifications = new ArrayList<>();

            for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.getShuffledTestGroupTypeIds(randomGenerator)) {
                groupPluginDataBuilder.addGroupTypeId(testGroupTypeId);
                groupTypeIds.add(testGroupTypeId);

                for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                        .getShuffledTestGroupPropertyIds(testGroupTypeId, randomGenerator)) {
                    groupPluginDataBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(),
                            testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());

                    Map<GroupPropertyId, PropertyDefinition> propertyDefinitionsMap = groupPropertyDefinitions
                            .get(testGroupTypeId);
                    if (propertyDefinitionsMap == null) {
                        propertyDefinitionsMap = new LinkedHashMap<>();
                        groupPropertyDefinitions.put(testGroupTypeId, propertyDefinitionsMap);
                    }
                    propertyDefinitionsMap.put(testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
                }
            }

            int groupCount = 10;
            List<GroupId> groups = new ArrayList<>();
            for (int j = 0; j < groupCount; j++) {
                GroupId groupId = new GroupId(j);
                groups.add(groupId);
                TestGroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
                groupPluginDataBuilder.addGroup(groupId, groupTypeId);

                while (j >= groupSpecifications.size()) {
                    groupSpecifications.add(null);
                }

                GroupSpecification groupSpecification = groupSpecifications.get(j);
                if (groupSpecification == null) {
                    groupSpecification = new GroupSpecification();
                    groupSpecification.groupId = groupId;
                    groupSpecifications.set(j, groupSpecification);
                }
                groupSpecification.groupTypeId = groupTypeId;

                for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                        .getTestGroupPropertyIds(groupTypeId)) {
                    if (randomGenerator.nextBoolean()) {
                        Object randomPropertyValue = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
                        groupPluginDataBuilder.setGroupPropertyValue(groupId, testGroupPropertyId, randomPropertyValue);

                        while (j >= groupSpecifications.size()) {
                            groupSpecifications.add(null);
                        }

                        List<GroupPropertyValue> groupPropertyValues = groupSpecification.groupPropertyValues;
                        if (groupPropertyValues == null) {
                            groupPropertyValues = new ArrayList<>();
                            groupSpecification.groupPropertyValues = groupPropertyValues;
                        }

                        Iterator<GroupPropertyValue> iterator = groupSpecification.groupPropertyValues.iterator();
                        while (iterator.hasNext()) {
                            GroupPropertyValue next = iterator.next();
                            if (next.groupPropertyId().equals(testGroupPropertyId)) {
                                iterator.remove();
                                break;
                            }
                        }

                        GroupPropertyValue groupPropertyValue = new GroupPropertyValue(testGroupPropertyId,
                                randomPropertyValue);
                        groupPropertyValues.add(groupPropertyValue);
                    }
                }
            }

            int totalPeopleCount = 0;
            for (GroupId groupId : groups) {
                int peopleCount = randomGenerator.nextInt(100);
                int currTotalPeopleCount = totalPeopleCount;
                totalPeopleCount += peopleCount;
                for (int j = currTotalPeopleCount; j < totalPeopleCount; j++) {
                    PersonId personId = new PersonId(j);
                    groupPluginDataBuilder.associatePersonToGroup(groupId, personId);

                    while (j >= personToGroupsMemberships.size()) {
                        personToGroupsMemberships.add(null);
                    }

                    List<GroupId> personToGroupMembership = personToGroupsMemberships.get(j);
                    if (personToGroupMembership == null) {
                        personToGroupMembership = new ArrayList<>();
                        personToGroupsMemberships.set(j, personToGroupMembership);
                    }

                    personToGroupMembership.add(groupId);

                    int groupIndex = groupId.getValue();

                    while (groupIndex >= groupToPeopleMemberships.size()) {
                        groupToPeopleMemberships.add(null);
                    }

                    List<PersonId> people = groupToPeopleMemberships.get(groupIndex);
                    if (people == null) {
                        people = new ArrayList<>();
                        groupToPeopleMemberships.set(groupIndex, people);
                    }

                    people.add(personId);
                }
            }

            for (GroupSpecification groupSpecification : groupSpecifications) {
                if (groupSpecification != null) {
                    nextGroupIdValue = FastMath.max(nextGroupIdValue, groupSpecification.groupId.getValue());
                }
            }
            nextGroupIdValue++;

            GroupsPluginData pluginData = groupPluginDataBuilder.build();

            StringBuilder dataSb = new StringBuilder();
            dataSb.append("Data [nextGroupIdValue=");
            dataSb.append(nextGroupIdValue);
            dataSb.append(", groupPropertyDefinitions=");
            dataSb.append(groupPropertyDefinitions);
            dataSb.append(", groupTypeIds=");
            dataSb.append(groupTypeIds);
            dataSb.append(", groupSpecifications=");
            dataSb.append(groupSpecifications);
            dataSb.append(", personToGroupsMemberships=");
            dataSb.append(personToGroupsMemberships);
            dataSb.append(", groupToPeopleMemberships=");
            dataSb.append(groupToPeopleMemberships);
            dataSb.append("]");

            StringBuilder pluginDataSb = new StringBuilder();
            pluginDataSb.append("GroupsPluginData [data=");
            pluginDataSb.append(dataSb.toString());
            pluginDataSb.append("]");

            assertEquals(pluginDataSb.toString(), pluginData.toString());
        }
    }
    
    

    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addGroupToPerson", args = {GroupId.class, PersonId.class })
    public void testAddGroupToPerson() {

        Random random = new Random(7282493148489771700L);

        Map<MultiKey, MutableInteger> expectedGroupAssignments = new LinkedHashMap<>();

        GroupsPluginData.Builder builder = GroupsPluginData.builder();
        // add in the group types
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // create some people
        List<Integer> personIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            personIds.add(i);
        }

        /*
         * Add a few groups and add to those groups 0 to 9 randomly selected people.
         * Record the assignments in the expected data structure.
         */
        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < 20; i++) {
            // add the group

            builder.addGroup(new GroupId(i), testGroupTypeId);
            testGroupTypeId = testGroupTypeId.next();
            // select some people and add them to the group
            Collections.shuffle(personIds, random);
            int count = random.nextInt(10);
            for (int j = 0; j < count; j++) {
                builder.addGroupToPerson(new GroupId(i), new PersonId(personIds.get(j)));
                builder.addPersonToGroup(new PersonId(personIds.get(j)), new GroupId(i));
                MultiKey multiKey = new MultiKey(new GroupId(i), new PersonId(personIds.get(j)));
                expectedGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                expectedGroupAssignments.get(multiKey).increment();
            }
        }

        // build the group initial data
        GroupsPluginData groupsPluginData = builder.build();

        // show that the group memberships are as expected
        Map<MultiKey, MutableInteger> actualGroupAssignments = new LinkedHashMap<>();

        for (int i = 0; i < groupsPluginData.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            for (GroupId groupId : groupsPluginData.getGroupsForPerson(personId)) {
                MultiKey multiKey = new MultiKey(groupId, personId);
                actualGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                actualGroupAssignments.get(multiKey).increment();
            }
        }

        assertEquals(expectedGroupAssignments, actualGroupAssignments);

        // precondition test: if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(null, new PersonId(0)));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // precondition test: if the person id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(new GroupId(0), null));
        assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
    }    
    
    @Test
    @UnitTestMethod(target = GroupsPluginData.Builder.class, name = "addPersonToGroup", args = {PersonId.class, GroupId.class })
    public void testAddPersonToGroup() {

        Random random = new Random(7282493148489771700L);

        Map<MultiKey, MutableInteger> expectedGroupAssignments = new LinkedHashMap<>();

        GroupsPluginData.Builder builder = GroupsPluginData.builder();
        // add in the group types
        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            builder.addGroupTypeId(testGroupTypeId);
        }

        // create some people
        List<Integer> personIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            personIds.add(i);
        }

        /*
         * Add a few groups and add to those groups 0 to 9 randomly selected people.
         * Record the assignments in the expected data structure.
         */
        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < 20; i++) {
            // add the group

            builder.addGroup(new GroupId(i), testGroupTypeId);
            testGroupTypeId = testGroupTypeId.next();
            // select some people and add them to the group
            Collections.shuffle(personIds, random);
            int count = random.nextInt(10);
            for (int j = 0; j < count; j++) {
                builder.addGroupToPerson(new GroupId(i), new PersonId(personIds.get(j)));
                builder.addPersonToGroup(new PersonId(personIds.get(j)), new GroupId(i));
                MultiKey multiKey = new MultiKey(new GroupId(i), new PersonId(personIds.get(j)));
                expectedGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                expectedGroupAssignments.get(multiKey).increment();
            }
        }

        // build the group initial data
        GroupsPluginData groupsPluginData = builder.build();

        // show that the group memberships are as expected
        Map<MultiKey, MutableInteger> actualGroupAssignments = new LinkedHashMap<>();

        for (int i = 0; i < groupsPluginData.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            for (GroupId groupId : groupsPluginData.getGroupsForPerson(personId)) {
                MultiKey multiKey = new MultiKey(groupId, personId);
                actualGroupAssignments.putIfAbsent(multiKey, new MutableInteger());
                actualGroupAssignments.get(multiKey).increment();
            }
        }

        assertEquals(expectedGroupAssignments, actualGroupAssignments);

        // precondition test: if the group id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(null, new PersonId(0)));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // precondition test: if the person id is null
        contractException = assertThrows(ContractException.class,
                () -> GroupsPluginData.builder().associatePersonToGroup(new GroupId(0), null));
        assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
    }    

}
