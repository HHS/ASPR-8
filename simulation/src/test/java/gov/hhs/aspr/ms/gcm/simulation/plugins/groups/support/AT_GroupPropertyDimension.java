package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupPropertyDimension {

    @Test
    @UnitTestConstructor(target = GroupPropertyDimension.class, args = { GroupPropertyDimensionData.class })
    public void testConstructor() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224759L);

        GroupId groupId = new GroupId(0);
        TestGroupPropertyId targetPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);

        GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
                .setGroupId(groupId)//
                .setGroupPropertyId(targetPropertyId)//
                .addValue("Level_0", targetPropertyId.getRandomPropertyValue(randomGenerator))//
                .build();

        GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

        assertNotNull(groupPropertyDimension);
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

            // create a GroupPropertyDimensionData with the level values
            GroupPropertyDimensionData.Builder dimDataBuilder = GroupPropertyDimensionData.builder()//
                    .setGroupId(groupId)//
                    .setGroupPropertyId(targetPropertyId);

            for (int k = 0; k < expectedValues.size(); k++) {
                dimDataBuilder.addValue("Level_" + k, expectedValues.get(k));
            }

            GroupPropertyDimensionData groupPropertyDimensionData = dimDataBuilder.build();
            GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

            // show that for each level the dimension properly assigns the value
            // to a group property data builder
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
                dimensionContextBuilder.setSimulationState(SimulationState.builder().build());
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
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getDimensionData", args = {})
    public void testGetDimensionData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 30; i++) {

            GroupId groupId = new GroupId(i);

            TestGroupPropertyId randomTestGroupPropertyId = TestGroupPropertyId
                    .getRandomTestGroupPropertyId(randomGenerator);

            GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
                    .setGroupId(groupId)//
                    .setGroupPropertyId(randomTestGroupPropertyId)//
                    .addValue("Level_" + i, randomTestGroupPropertyId.getRandomPropertyValue(randomGenerator))//
                    .build();

            GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

            assertEquals(groupPropertyDimensionData, groupPropertyDimension.getDimensionData());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "getExperimentMetaData", args = {})
    public void testGetExperimentMetaData() {
        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

            List<String> expectedExperimentMetaData = new ArrayList<>();
            expectedExperimentMetaData.add(testGroupPropertyId.toString());

            GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
                    .setGroupId(new GroupId(0))//
                    .setGroupPropertyId(testGroupPropertyId)//
                    .build();

            GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

            assertEquals(expectedExperimentMetaData, groupPropertyDimension.getExperimentMetaData());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "levelCount", args = {})
    public void testLevelCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 50; i++) {

            GroupPropertyDimensionData.Builder builder = GroupPropertyDimensionData.builder()//
                    .setGroupId(new GroupId(0))//
                    .setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                builder.addValue("Level_" + j, value);
            }
            GroupPropertyDimensionData groupPropertyDimensionData = builder.build();

            GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

            assertEquals(n, groupPropertyDimension.levelCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupPropertyDimension.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        TestGroupPropertyId randomTestGroupPropertyId = TestGroupPropertyId
                .getRandomTestGroupPropertyId(randomGenerator);

        GroupPropertyDimensionData.Builder builder = GroupPropertyDimensionData.builder()//
                .setGroupId(new GroupId(0))//
                .setGroupPropertyId(randomTestGroupPropertyId);

        for (int i = 0; i < 10; i++) {
            builder.addValue("Level_" + i, randomTestGroupPropertyId.getRandomPropertyValue(randomGenerator));
        }

        GroupPropertyDimensionData groupPropertyDimensionData = builder.build();
        GroupPropertyDimension groupPropertyDimension = new GroupPropertyDimension(groupPropertyDimensionData);

        String actualValue = groupPropertyDimension.toString();

        String expectedValue = "GroupPropertyDimension [groupPropertyDimensionData="
                + groupPropertyDimensionData + "]";

        assertEquals(expectedValue, actualValue);
    }
}
