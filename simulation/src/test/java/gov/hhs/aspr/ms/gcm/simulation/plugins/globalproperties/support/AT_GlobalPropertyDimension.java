package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyDimension {

    @Test
    @UnitTestConstructor(target = GlobalPropertyDimension.class, args = { GlobalPropertyDimensionData.class })
    public void testConstructor() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224759L);

        TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);

        GlobalPropertyDimensionData globalPropertyDimensionData = GlobalPropertyDimensionData.builder()//
                .setGlobalPropertyId(testGlobalPropertyId)//
                .setAssignmentTime(randomGenerator.nextDouble())//
                .addValue("Level_0", testGlobalPropertyId.getRandomPropertyValue(randomGenerator))
                .build();

        GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

        assertNotNull(globalPropertyDimension);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
            int.class })
    public void testExecuteLevel() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

        // run several test cases
        for (int i = 0; i < 30; i++) {

            // select a random number of levels
            int levelCount = randomGenerator.nextInt(10);
            // select a random property id
            TestGlobalPropertyId targetPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);

            // generate random values for the level
            List<Object> expectedValues = new ArrayList<>();
            for (int j = 0; j < levelCount; j++) {
                expectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
            }

            // create a GlobalPropertyDimensionData with the level values
            GlobalPropertyDimensionData.Builder dimDataBuilder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(targetPropertyId);

            for (int k = 0; k < expectedValues.size(); k++) {
                dimDataBuilder.addValue("Level_" + k, expectedValues.get(k));
            }

            GlobalPropertyDimensionData globalPropertyDimensionData = dimDataBuilder.build();
            GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

            // show that for each level the dimension properly assigns the value
            // to a global property data builder
            for (int level = 0; level < levelCount; level++) {
                /*
                 * Create a GlobalPropertiesPluginData, filling it with the test property
                 * definitions and any values that are required
                 */
                GlobalPropertiesPluginData.Builder pluginDataBuilder = GlobalPropertiesPluginData.builder();
                for (TestGlobalPropertyId propertyId : TestGlobalPropertyId.values()) {
                    PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
                    pluginDataBuilder.defineGlobalProperty(propertyId, propertyDefinition, 0.0);
                    if (propertyDefinition.getDefaultValue().isEmpty()) {
                        pluginDataBuilder.setGlobalPropertyValue(propertyId,
                                propertyId.getRandomPropertyValue(randomGenerator), 0.0);
                    }
                }

                // Create a dimension context that contain the plugin data
                // builder
                DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

                pluginDataBuilder = (GlobalPropertiesPluginData.Builder) dimensionContextBuilder
                        .add(pluginDataBuilder.build());
                dimensionContextBuilder.setSimulationState(SimulationState.builder().build());
                DimensionContext dimensionContext = dimensionContextBuilder.build();

                // execute the dimension with the level
                globalPropertyDimension.executeLevel(dimensionContext, level);

                /*
                 * get the GlobalPropertiesPluginData from the corresponding builder
                 */
                GlobalPropertiesPluginData globalPropertiesPluginData = pluginDataBuilder.build();

                /*
                 * show that the GlobalPropertiesPluginData has the value we expect for the
                 * given level
                 */
                Optional<Object> optionalValue = globalPropertiesPluginData.getGlobalPropertyValue(targetPropertyId);
                assertTrue(optionalValue.isPresent());
                Object actualValue = optionalValue.get();
                Object expectedValue = expectedValues.get(level);
                assertEquals(expectedValue, actualValue);
            }
        }

    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimension.class, name = "getDimensionData", args = {})
    public void testGetDimensionData() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 30; i++) {

            TestGlobalPropertyId randomGlobalPropertyId = TestGlobalPropertyId
                    .getRandomGlobalPropertyId(randomGenerator);

            GlobalPropertyDimensionData globalPropertyDimensionData = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(randomGlobalPropertyId)//
                    .setAssignmentTime(randomGenerator.nextDouble())
                    .addValue("Level_" + i, randomGlobalPropertyId.getRandomPropertyValue(randomGenerator))
                    .build();

            GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

            assertEquals(globalPropertyDimensionData, globalPropertyDimension.getDimensionData());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimension.class, name = "getExperimentMetaData", args = {})
    public void testGetExperimentMetaData() {
        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

            List<String> expectedExperimentMetaData = new ArrayList<>();
            expectedExperimentMetaData.add(testGlobalPropertyId.toString());

            GlobalPropertyDimensionData globalPropertyDimensionData = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(testGlobalPropertyId)
                    .build();

            GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

            assertEquals(expectedExperimentMetaData, globalPropertyDimension.getExperimentMetaData());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimension.class, name = "levelCount", args = {})
    public void testLevelCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 50; i++) {

            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                builder.addValue("Level_" + j, value);
            }
            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();
            GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

            assertEquals(n, globalPropertyDimension.levelCount());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimension.class, name = "toString", args = {})
    public void testToString() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);
        TestGlobalPropertyId randomGlobalPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);

        GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                .setGlobalPropertyId(randomGlobalPropertyId)//
                .setAssignmentTime(randomGenerator.nextDouble());

        for (int i = 0; i < 10; i++) {
            builder.addValue("Level_" + i, randomGlobalPropertyId.getRandomPropertyValue(randomGenerator));
        }

        GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();
        GlobalPropertyDimension globalPropertyDimension = new GlobalPropertyDimension(globalPropertyDimensionData);

        String actualValue = globalPropertyDimension.toString();

        String expectedValue = "GlobalPropertyDimension [globalPropertyDimensionData="
                + globalPropertyDimensionData + "]";

        assertEquals(expectedValue, actualValue);
    }
}
