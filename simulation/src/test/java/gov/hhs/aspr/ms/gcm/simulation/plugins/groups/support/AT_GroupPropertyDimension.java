package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupPropertyDimension {

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.Builder.class, name = "addValue", args = { Object.class })
    public void testAddValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(i))
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue(value);
            }
            GroupPropertyDimension groupPropertyDimension = builder.build();

            List<Object> actualValues = groupPropertyDimension.getValues();
            assertEquals(expectedValues, actualValues);
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupPropertyDimension.builder().addValue(null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.Builder.class, name = "build", args = {})
    public void testBuild() {
        GroupPropertyDimension groupPropertyDimension = //
                GroupPropertyDimension.builder()//
                        .setGroupId(new GroupId(0))
                        .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK).build();
        assertNotNull(groupPropertyDimension);

        // precondition test : if the global property id is not assigned
        ContractException contractException = assertThrows(ContractException.class,
                () -> gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyDimension.builder().setGroupId(new GroupId(0)).build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // if the groupId was not assigned
        contractException = assertThrows(ContractException.class, () -> gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyDimension
                .builder().setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK).build());
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.Builder.class, name = "setGroupId", args = { GroupId.class })
    public void testSetGroupId() {
        for (int i = 0; i < 10; i++) {
            GroupId groupId = new GroupId(i);

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK)
                    .setGroupId(groupId);

            GroupPropertyDimension groupPropertyDimension = builder.build();
            assertEquals(groupId, groupPropertyDimension.getGroupId());
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupPropertyDimension.builder().setGroupId(null));
        assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.Builder.class, name = "setGroupPropertyId", args = {
            GroupPropertyId.class })
    public void testSetGroupPropertyId() {
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(0)).setGroupPropertyId(testGroupPropertyId);

            GroupPropertyDimension groupPropertyDimension = builder.build();
            assertEquals(testGroupPropertyId, groupPropertyDimension.getGroupPropertyId());
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupPropertyDimension.builder().setGroupPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GroupPropertyDimension.builder());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
            int.class })
    public void testExecuteLevel() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

        // run several test cases
        for (int i = 0; i < 30; i++) {

            GroupId groupId = new GroupId(0);
            // select a random number of levels
            int levelCount = randomGenerator.nextInt(10);
            // select a random property id
            TestGroupPropertyId targetPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);

            // generate random values for the level
            List<Object> expectedValues = new ArrayList<>();
            for (int j = 0; j < levelCount; j++) {
                expectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
            }

            // create a GroupPropertyDimension with the level values
            GroupPropertyDimension.Builder dimBuilder = GroupPropertyDimension.builder()//
                    .setGroupId(groupId).setGroupPropertyId(targetPropertyId);

            for (Object value : expectedValues) {
                dimBuilder.addValue(value);
            }

            GroupPropertyDimension groupPropertyDimension = dimBuilder.build();

            // show that for each level the dimension properly assigns the value
            // to a global property data builder
            for (int level = 0; level < levelCount; level++) {
                /*
                 * Create a GroupsPluginData, filling it with the test property definitions and
                 * any values that are required
                 */
                GroupsPluginData.Builder pluginDataBuilder = GroupsPluginData.builder();
                for (TestGroupPropertyId propertyId : TestGroupPropertyId.values()) {
                    PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
                    pluginDataBuilder.defineGroupProperty(TestGroupTypeId.GROUP_TYPE_1, propertyId, propertyDefinition);

                    if (propertyDefinition.getDefaultValue().isEmpty()) {
                        pluginDataBuilder.setGroupPropertyValue(groupId, propertyId,
                                propertyId.getRandomPropertyValue(randomGenerator));
                    }
                }

                pluginDataBuilder.addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1).addGroup(groupId,
                        TestGroupTypeId.GROUP_TYPE_1);
                // Create a dimension context that contain the plugin data
                // builder
                DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

                pluginDataBuilder = (GroupsPluginData.Builder) dimensionContextBuilder.add(pluginDataBuilder.build());
                DimensionContext dimensionContext = dimensionContextBuilder.build();

                // execute the dimension with the level
                groupPropertyDimension.executeLevel(dimensionContext, level);

                /*
                 * get the GroupsPluginData from the corresponding builder
                 */
                GroupsPluginData groupsPluginData = pluginDataBuilder.build();

                /*
                 * show that the GroupsPluginData has the value we expect for the given level
                 */

                List<GroupPropertyValue> groupPropertyValues = groupsPluginData.getGroupPropertyValues(groupId);

                boolean found = false;
                for (GroupPropertyValue groupPropertyValue : groupPropertyValues) {
                    if (groupPropertyValue.groupPropertyId().equals(targetPropertyId)) {
                        Object actualValue = groupPropertyValue.value();
                        Object expectedValue = expectedValues.get(level);
                        assertEquals(expectedValue, actualValue);
                        found = true;
                        break;
                    }
                }

                assertTrue(found);
            }
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getExperimentMetaData", args = {})
    public void testGetExperimentMetaData() {
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

            List<String> expectedExperimentMetaData = new ArrayList<>();
            expectedExperimentMetaData.add(testGroupPropertyId.toString());

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(0)).setGroupPropertyId(testGroupPropertyId);

            GroupPropertyDimension groupPropertyDimension = builder.build();
            assertEquals(expectedExperimentMetaData, groupPropertyDimension.getExperimentMetaData());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getGroupId", args = {})
    public void testGetGroupId() {
        for (int i = 0; i < 10; i++) {
            GroupId groupId = new GroupId(i);
            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(groupId)
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

            GroupPropertyDimension groupPropertyDimension = builder.build();
            assertEquals(groupId, groupPropertyDimension.getGroupId());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getGroupPropertyId", args = {})
    public void testGetGroupPropertyId() {
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(0)).setGroupPropertyId(testGroupPropertyId);

            GroupPropertyDimension groupPropertyDimension = builder.build();
            assertEquals(testGroupPropertyId, groupPropertyDimension.getGroupPropertyId());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getValues", args = {})
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(0))
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue(value);
            }
            GroupPropertyDimension groupPropertyDimension = builder.build();

            List<Object> actualValues = groupPropertyDimension.getValues();
            assertEquals(expectedValues, actualValues);
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "levelCount", args = {})
    public void testLevelCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 50; i++) {

            GroupPropertyDimension.Builder builder = GroupPropertyDimension.builder()//
                    .setGroupId(new GroupId(0))
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                builder.addValue(value);
            }
            GroupPropertyDimension groupPropertyDimension = builder.build();

            assertEquals(n, groupPropertyDimension.levelCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "hashCode", args = {})
    public void testHashCode() {
        GroupPropertyDimension dimension1 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        GroupPropertyDimension dimension2 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension3 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        GroupPropertyDimension dimension4 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension5 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension6 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        assertEquals(dimension1.hashCode(), dimension1.hashCode());

        assertNotEquals(dimension1.hashCode(), dimension2.hashCode());
        assertNotEquals(dimension1.hashCode(), dimension3.hashCode());
        assertNotEquals(dimension1.hashCode(), dimension4.hashCode());
        assertNotEquals(dimension1.hashCode(), dimension5.hashCode());

        assertNotEquals(dimension2.hashCode(), dimension3.hashCode());
        assertNotEquals(dimension2.hashCode(), dimension4.hashCode());
        assertNotEquals(dimension2.hashCode(), dimension5.hashCode());
        assertNotEquals(dimension2.hashCode(), dimension6.hashCode());

        assertNotEquals(dimension3.hashCode(), dimension4.hashCode());
        assertNotEquals(dimension3.hashCode(), dimension5.hashCode());
        assertNotEquals(dimension3.hashCode(), dimension6.hashCode());

        assertNotEquals(dimension4.hashCode(), dimension5.hashCode());
        assertNotEquals(dimension4.hashCode(), dimension6.hashCode());

        assertNotEquals(dimension5.hashCode(), dimension6.hashCode());

        assertEquals(dimension1.hashCode(), dimension6.hashCode());
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "equals", args = { Object.class })
    public void testEquals() {
        GroupPropertyDimension dimension1 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        GroupPropertyDimension dimension2 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension3 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        GroupPropertyDimension dimension4 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension5 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)
                .setGroupId(new GroupId(1)).build();

        GroupPropertyDimension dimension6 = GroupPropertyDimension.builder()
                .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)
                .setGroupId(new GroupId(0)).build();

        assertEquals(dimension1, dimension1);

        assertNotEquals(dimension1, null);
        assertNotEquals(dimension1, new Object());

        assertNotEquals(dimension1, dimension2);
        assertNotEquals(dimension1, dimension3);
        assertNotEquals(dimension1, dimension4);
        assertNotEquals(dimension1, dimension5);

        assertNotEquals(dimension2, dimension3);
        assertNotEquals(dimension2, dimension4);
        assertNotEquals(dimension2, dimension5);
        assertNotEquals(dimension2, dimension6);

        assertNotEquals(dimension3, dimension4);
        assertNotEquals(dimension3, dimension5);
        assertNotEquals(dimension3, dimension6);

        assertNotEquals(dimension4, dimension5);
        assertNotEquals(dimension4, dimension6);

        assertNotEquals(dimension5, dimension6);

        assertEquals(dimension1, dimension6);
    }
}
