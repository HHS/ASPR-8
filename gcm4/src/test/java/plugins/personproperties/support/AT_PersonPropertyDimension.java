package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DimensionContext;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PersonPropertyDimension {

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.Builder.class, name = "addValue", args = { Object.class })
    public void testAddValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue(value);
            }
            PersonPropertyDimension personPropertyDimension = builder.build();

            List<Object> actualValues = personPropertyDimension.getValues();
            assertEquals(expectedValues, actualValues);
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimension.builder().addValue(null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.Builder.class, name = "build", args = {})
    public void testBuild() {
        PersonPropertyDimension personPropertyDimension = //
                PersonPropertyDimension.builder()//
                        .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
                        .build();
        assertNotNull(personPropertyDimension);

        // precondition test : if the global property id is not assigned
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimension.builder().build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.Builder.class, name = "setTrackTimes", args = {
            boolean.class })
    public void testSetTrackTimes() {
        for (int i = 0; i < 10; i++) {
            boolean trackTimes = i % 2 == 0;
            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
                    .setTrackTimes(trackTimes);

            PersonPropertyDimension personPropertyDimension = builder.build();
            assertEquals(trackTimes, personPropertyDimension.getTrackTimes());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.Builder.class, name = "setPersonPropertyId", args = {
            PersonPropertyId.class })
    public void testSetPersonPropertyId() {
        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(testPersonPropertyId);

            PersonPropertyDimension personPropertyDimension = builder.build();
            assertEquals(testPersonPropertyId, personPropertyDimension.getPersonPropertyId());
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimension.builder().setPersonPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(PersonPropertyDimension.builder());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
            int.class })
    public void testExecuteLevel() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

        // run several test cases
        for (int i = 0; i < 30; i++) {

            // select a random number of levels
            int levelCount = randomGenerator.nextInt(10);
            // select a random property id
            TestPersonPropertyId targetPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

            // generate random values for the level
            List<Object> expectedValues = new ArrayList<>();
            for (int j = 0; j < levelCount; j++) {
                expectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
            }

            // create a PersonPropertyDimension with the level values
            PersonPropertyDimension.Builder dimBuilder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(targetPropertyId);

            for (Object value : expectedValues) {
                dimBuilder.addValue(value);
            }

            PersonPropertyDimension personPropertyDimension = dimBuilder.build();

            // show that for each level the dimension properly assigns the value
            // to a global property data builder
            for (int level = 0; level < levelCount; level++) {
                /*
                 * Create a PersonPropertiesPluginData, filling it with the test
                 * property definitions and any values that are required
                 */
                PersonPropertiesPluginData.Builder pluginDataBuilder = PersonPropertiesPluginData.builder();
                for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
                    PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
                    pluginDataBuilder.definePersonProperty(propertyId, propertyDefinition, 0, false);
                }

                // Create a dimension context that contain the plugin data
                // builder
                DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

                pluginDataBuilder = (PersonPropertiesPluginData.Builder) dimensionContextBuilder
                        .add(pluginDataBuilder.build());
                DimensionContext dimensionContext = dimensionContextBuilder.build();

                // execute the dimension with the level
                personPropertyDimension.executeLevel(dimensionContext, level);

                /*
                 * get the PersonPropertiesPluginData from the corresponding
                 * builder
                 */
                PersonPropertiesPluginData personsPluginData = pluginDataBuilder.build();

                /*
                 * show that the PersonPropertiesPluginData has the value we
                 * expect for the given level
                 */

                PropertyDefinition actualDefinition = personsPluginData.getPersonPropertyDefinition(targetPropertyId);

                assertTrue(actualDefinition.getDefaultValue().isPresent());
                Object actualValue = actualDefinition.getDefaultValue().get();
                Object expectedValue = expectedValues.get(level);
                assertEquals(expectedValue, actualValue);
            }
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "getExperimentMetaData", args = {})
    public void testGetExperimentMetaData() {
        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

            List<String> expectedExperimentMetaData = new ArrayList<>();
            expectedExperimentMetaData.add(testPersonPropertyId.toString());

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(testPersonPropertyId);

            PersonPropertyDimension personPropertyDimension = builder.build();
            assertEquals(expectedExperimentMetaData, personPropertyDimension.getExperimentMetaData());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.Builder.class, name = "getTrackTimes", args = {})
    public void testGetTrackTimes() {
        for (int i = 0; i < 10; i++) {
            boolean trackTimes = i % 2 == 0;
            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
                    .setTrackTimes(trackTimes);

            PersonPropertyDimension personPropertyDimension = builder.build();
            assertEquals(trackTimes, personPropertyDimension.getTrackTimes());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "getPersonPropertyId", args = {})
    public void testGetPersonPropertyId() {
        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(testPersonPropertyId);

            PersonPropertyDimension personPropertyDimension = builder.build();
            assertEquals(testPersonPropertyId, personPropertyDimension.getPersonPropertyId());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "getValues", args = {})
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue(value);
            }
            PersonPropertyDimension personPropertyDimension = builder.build();

            List<Object> actualValues = personPropertyDimension.getValues();
            assertEquals(expectedValues, actualValues);
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimension.class, name = "levelCount", args = {})
    public void testLevelCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

        for (int i = 0; i < 50; i++) {

            PersonPropertyDimension.Builder builder = PersonPropertyDimension.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                builder.addValue(value);
            }
            PersonPropertyDimension personPropertyDimension = builder.build();

            assertEquals(n, personPropertyDimension.levelCount());
        }
    }
}
