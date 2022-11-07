package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupPropertyDefinitionInitialization.class)
public class AT_GroupPropertyDefinitionInitialization {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        // Show that builder doesn't return null
        assertNotNull(GroupPropertyDefinitionInitialization.builder());
    }

    @Test
    @UnitTestMethod(name = "getPropertyDefinition", args = {})
    public void testGetPropertyDefinition() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();
        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());
    }

    @Test
    @UnitTestMethod(name = "getGroupTypeId", args = {})
    public void testGetGroupTypeId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();
        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(groupTypeId, definitionInitialization.getGroupTypeId());
    }

    @Test
    @UnitTestMethod(name = "getPropertyId", args = {})
    public void testGetPropertyId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();
        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(groupPropertyId, definitionInitialization.getPropertyId());
    }

    @Test
    @UnitTestMethod(name = "getPropertyValues", args = {})
    public void testGetPropertyValues() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();
        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
        GroupId groupId = new GroupId(0);

        Pair<GroupId, String> propertyValue1 = new Pair<GroupId, String>(groupId, "foo");
        Pair<GroupId, String> propertyValue2 = new Pair<GroupId, String>(groupId, "bar");
        Pair<GroupId, String> propertyValue3 = new Pair<GroupId, String>(groupId, "foobar");

        List<Pair<GroupId, String>> expectedListOfPropertyValues = new ArrayList<>();
        expectedListOfPropertyValues.add(propertyValue1);
        expectedListOfPropertyValues.add(propertyValue2);
        expectedListOfPropertyValues.add(propertyValue3);

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .addPropertyValue(groupId, "foo")
                .addPropertyValue(groupId, "bar")
                .addPropertyValue(groupId, "foobar")
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(expectedListOfPropertyValues, definitionInitialization.getPropertyValues());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
    public void testBuild() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();

        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
        GroupId groupId = new GroupId(0);

        GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

        // preconditions
        // null property definition
        ContractException contractException = assertThrows(ContractException.class,
                () -> builder
                        .setGroupTypeId(groupTypeId)
                        .setPropertyId(groupPropertyId)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

        // incompatible property definition value
        contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setPropertyId(groupPropertyId)
                        .setGroupTypeId(groupTypeId)
                        .addPropertyValue(groupId, 1)
                        .build());
        assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

        // null groupTypeId
        contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setPropertyId(groupPropertyId)
                        .build());
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

        // null propertyId
        contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setGroupTypeId(groupTypeId)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

    }

    @Test
    @UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = {
            PropertyDefinition.class })
    public void testSetPropertyDefinition() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();

        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

        // preconditions
        // null property definition
        ContractException contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(null)
                        .setGroupTypeId(groupTypeId)
                        .setPropertyId(groupPropertyId)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

    }

    @Test
    @UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setGroupTypeId", args = {
            GroupTypeId.class })
    public void testSetGroupTypeId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();

        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

        // preconditions
        // null group type id
        ContractException contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setGroupTypeId(null)
                        .setPropertyId(groupPropertyId)
                        .build());
        assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(groupTypeId, definitionInitialization.getGroupTypeId());

    }

    @Test
    @UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "setPropertyId", args = {
            GroupPropertyId.class })
    public void testSetPropertyId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();

        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;

        GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

        // preconditions
        // null property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setGroupTypeId(groupTypeId)
                        .setPropertyId(null)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);

        assertEquals(groupPropertyId, definitionInitialization.getPropertyId());

    }

    @Test
    @UnitTestMethod(target = GroupPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = {
            GroupId.class, Object.class })
    public void testAddPropertyValue() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("foo")
                .setType(String.class)
                .build();

        GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
        GroupId groupId = new GroupId(0);

        Pair<GroupId, String> propertyValue1 = new Pair<GroupId, String>(groupId, "foo");
        Pair<GroupId, String> propertyValue2 = new Pair<GroupId, String>(groupId, "bar");
        Pair<GroupId, String> propertyValue3 = new Pair<GroupId, String>(groupId, "foobar");

        List<Pair<GroupId, String>> expectedListOfPropertyValues = new ArrayList<>();
        expectedListOfPropertyValues.add(propertyValue1);
        expectedListOfPropertyValues.add(propertyValue2);
        expectedListOfPropertyValues.add(propertyValue3);

        GroupPropertyDefinitionInitialization.Builder builder = GroupPropertyDefinitionInitialization.builder();

        // preconditions
        // null group id
        ContractException contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setGroupTypeId(groupTypeId)
                        .setPropertyId(groupPropertyId)
                        .addPropertyValue(null, "foo")
                        .build());
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        // null property value
        contractException = assertThrows(ContractException.class,
                () -> builder
                        .setPropertyDefinition(propertyDefinition)
                        .setGroupTypeId(groupTypeId)
                        .setPropertyId(groupPropertyId)
                        .addPropertyValue(groupId, null)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

        GroupPropertyDefinitionInitialization definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .addPropertyValue(groupId, "foo")
                .addPropertyValue(groupId, "bar")
                .addPropertyValue(groupId, "foobar")
                .build();

        assertNotNull(definitionInitialization);

        assertNotNull(definitionInitialization.getPropertyValues());
        assertFalse(definitionInitialization.getPropertyValues().isEmpty());
        assertEquals(expectedListOfPropertyValues, definitionInitialization.getPropertyValues());

        List<Pair<GroupId, Object>> actualListOfPropertyValues = definitionInitialization.getPropertyValues();
        for (int i = 0; i < actualListOfPropertyValues.size(); i++) {
            assertEquals(expectedListOfPropertyValues.get(i), actualListOfPropertyValues.get(i));
            assertEquals(String.class, actualListOfPropertyValues.get(i).getSecond().getClass());
            assertEquals(actualListOfPropertyValues.get(i).getSecond(), actualListOfPropertyValues.get(i).getSecond());
            assertEquals(groupId, actualListOfPropertyValues.get(i).getFirst());
        }

        definitionInitialization = GroupPropertyDefinitionInitialization.builder()
                .setGroupTypeId(groupTypeId)
                .setPropertyDefinition(propertyDefinition)
                .setPropertyId(groupPropertyId)
                .build();

        assertNotNull(definitionInitialization);
        assertNotNull(definitionInitialization.getPropertyValues());
        assertTrue(definitionInitialization.getPropertyValues().isEmpty());

    }
}
