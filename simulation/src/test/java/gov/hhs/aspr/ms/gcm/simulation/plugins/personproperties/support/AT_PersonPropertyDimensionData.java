package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PersonPropertyDimensionData {

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.Builder.class, name = "addValue", args = { Object.class })
    public void testAddValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue("Level_" + j, value);
            }
            PersonPropertyDimensionData personPropertyDimensionData = builder.build();

            List<Object> actualValues = personPropertyDimensionData.getValues();
            assertEquals(expectedValues, actualValues);
        }

        // precondition test : if the level is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimensionData.builder().addValue(null, "testValue"));
        assertEquals(NucleusError.NULL_DIMENSION_LEVEL_NAME, contractException.getErrorType());

        // precondition test : if the value is null
        contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimensionData.builder().addValue("Level_0", null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE,
                contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.Builder.class, name = "build", args = {})
    public void testBuild() {
        PersonPropertyDimensionData personPropertyDimensionData = PersonPropertyDimensionData.builder()//
                .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)//
                .build();
        assertNotNull(personPropertyDimensionData);

        // precondition test : if the person property id is not assigned
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimensionData.builder().build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if the dimension data contains duplicate level names
        contractException = assertThrows(ContractException.class, () -> {
            PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK)//
                    ._addLevelName("bad")//
                    ._addLevelName("bad")//
                    .build();
        });

        assertEquals(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.Builder.class, name = "setTrackTimes", args = {
            boolean.class })
    public void testSetTrackTimes() {
        for (int i = 0; i < 10; i++) {
            boolean trackTimes = i % 2 == 0;
            PersonPropertyDimensionData dimData = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)//
                    .setTrackTimes(trackTimes)//
                    .build();

            assertEquals(trackTimes, dimData.getTrackTimes());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.Builder.class, name = "setPersonPropertyId", args = {
            PersonPropertyId.class })
    public void testSetPersonPropertyId() {
        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

            PersonPropertyDimensionData dimData = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(testPersonPropertyId)//
                    .build();

            assertEquals(testPersonPropertyId, dimData.getPersonPropertyId());
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertyDimensionData.builder().setPersonPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(PersonPropertyDimensionData.builder());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "getValue", args = { int.class })
    public void testGetValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        List<Object> expectedValues = new ArrayList<>();
        List<String> expectedLevelNames = new ArrayList<>();

        TestPersonPropertyId targetPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

        PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
                .setPersonPropertyId(targetPropertyId);

        int levels = randomGenerator.nextInt();

        for (int i = 0; i < levels; i++) {
            Object expectedValue = targetPropertyId.getRandomPropertyValue(randomGenerator);
            expectedValues.add(expectedValue);
            expectedLevelNames.add("Level_" + i);
            builder.addValue("Level_" + i, expectedValue);
        }

        PersonPropertyDimensionData personPropertyDimensionData = builder.build();

        assertEquals(expectedLevelNames.size(), expectedValues.size());

        for (int i = 0; i < expectedValues.size(); i++) {
            Object expectedValue = expectedValues.get(i);
            Object actualValue = personPropertyDimensionData.getValue(i);
            assertEquals(expectedValue, actualValue);
        }

        // preconditions: negative level
        ContractException contractException = assertThrows(ContractException.class, () -> {
            personPropertyDimensionData.getValue(-1);
        });
        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

        // preconditions: level greater than total levels
        contractException = assertThrows(ContractException.class, () -> {
            personPropertyDimensionData.getValue(personPropertyDimensionData.getLevelCount() + 2);
        });

        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "getValues", args = {})
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue("Level_" + j, value);
            }
            PersonPropertyDimensionData personPropertyDimensionData = builder.build();

            List<Object> actualValues = personPropertyDimensionData.getValues();
            assertEquals(expectedValues, actualValues);
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "getTrackTimes", args = {})
    public void testGetTrackTimes() {
        for (int i = 0; i < 10; i++) {
            boolean trackTimes = i % 2 == 0;
            PersonPropertyDimensionData dimData = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)//
                    .setTrackTimes(trackTimes)//
                    .build();

            assertEquals(trackTimes, dimData.getTrackTimes());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "getPersonPropertyId", args = {})
    public void testGetPersonPropertyId() {
        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

            PersonPropertyDimensionData dimData = PersonPropertyDimensionData.builder()//
                    .setPersonPropertyId(testPersonPropertyId)//
                    .build();

            assertEquals(testPersonPropertyId, dimData.getPersonPropertyId());
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "getVersion", args = {})
    public void testGetVersion() {

        PersonPropertyDimensionData dimData = PersonPropertyDimensionData.builder()//
                .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)//
                .build();

        assertEquals(StandardVersioning.VERSION, dimData.getVersion());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "checkVersionSupported", args = { String.class })
    public void testCheckVersionSupported() {
        List<String> versions = Arrays.asList("", StandardVersioning.VERSION);

        for (String version : versions) {
            assertTrue(StandardVersioning.checkVersionSupported(version));
            assertFalse(StandardVersioning.checkVersionSupported(version + "badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported("badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported(version + "0"));
            assertFalse(StandardVersioning.checkVersionSupported(version + ".0.0"));
        }
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "hashCode", args = {})
    public void testHashCode() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6008834354417928205L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            PersonPropertyDimensionData dimensionData1 = getRandomPersonPropertyDimensionData(seed);
            PersonPropertyDimensionData dimensionData2 = getRandomPersonPropertyDimensionData(seed);
            assertEquals(dimensionData1, dimensionData2);
            assertEquals(dimensionData1.hashCode(), dimensionData2.hashCode());
        }

        // hash codes are reasonably distributed. There are 2790 possible generated
        // values, so the collision probability is very low
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            PersonPropertyDimensionData dimensionData = getRandomPersonPropertyDimensionData(
                    randomGenerator.nextLong());
            hashCodes.add(dimensionData.hashCode());
        }

        assertTrue(hashCodes.size() > 90);
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "equals", args = { Object.class })
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5592194423075711575L);

        // is never equal to null;
        for (int i = 0; i < 30; i++) {
            PersonPropertyDimensionData dimensionData = getRandomPersonPropertyDimensionData(
                    randomGenerator.nextLong());
            assertFalse(dimensionData.equals(null));
        }

        // reflexive
        for (int i = 0; i < 30; i++) {
            PersonPropertyDimensionData dimensionData = getRandomPersonPropertyDimensionData(
                    randomGenerator.nextLong());
            assertTrue(dimensionData.equals(dimensionData));
        }

        // symmetric, transitive, consistent
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            PersonPropertyDimensionData dimensionData1 = getRandomPersonPropertyDimensionData(seed);
            PersonPropertyDimensionData dimensionData2 = getRandomPersonPropertyDimensionData(seed);

            for (int j = 0; j < 5; j++) {
                assertTrue(dimensionData1.equals(dimensionData2));
                assertTrue(dimensionData2.equals(dimensionData1));
            }
        }

        // different inputs yield non-equal objects. There are 2790 possible generated
        // values, so the collision probability is very low
        Set<PersonPropertyDimensionData> personPropertyDimensionData = new LinkedHashSet<>();

        for (int i = 0; i < 100; i++) {
            PersonPropertyDimensionData dimensionData = getRandomPersonPropertyDimensionData(
                    randomGenerator.nextLong());
            personPropertyDimensionData.add(dimensionData);
        }

        assertTrue(personPropertyDimensionData.size() > 90);
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "toString", args = {})
    public void testToString() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

        TestPersonPropertyId targetPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

        Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Boolean targetTrackTime = randomGenerator.nextBoolean();

        PersonPropertyDimensionData personPropertyDimensionData = PersonPropertyDimensionData.builder()
                .setPersonPropertyId(targetPropertyId)//
                .setTrackTimes(targetTrackTime)//
                .addValue("Level_0", targetValue1)//
                .addValue("Level_1", targetValue2)//
                .build();

        StringBuilder builder = new StringBuilder();
        builder.append("PersonPropertyDimensionData [data=");
        builder.append("Data [levelNames=[");
        builder.append("Level_0, Level_1]");
        builder.append(", values=[");
        builder.append(targetValue1.toString() + ", ");
        builder.append(targetValue2.toString() + "]");
        builder.append(", personPropertyId=");
        builder.append(targetPropertyId.toString());
        builder.append(", trackTimes=");
        builder.append(targetTrackTime.toString());
        builder.append("]");
        builder.append("]");

        String expectedString = builder.toString();

        assertEquals(expectedString, personPropertyDimensionData.toString());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionData.class, name = "toBuilder", args = {})
    public void testToBuilder() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

        TestPersonPropertyId targetPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

        Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Boolean targetTrackTime = false;

        PersonPropertyDimensionData dimensionData = PersonPropertyDimensionData.builder()//
                .setPersonPropertyId(targetPropertyId)//
                .addValue("Level_0", targetValue1)//
                .addValue("Level_1", targetValue2)//
                .setTrackTimes(targetTrackTime)//
                .build();

        // show that the returned clone builder will build an identical instance if no
        // mutations are made
        PersonPropertyDimensionData.Builder cloneBuilder = dimensionData.toBuilder();
        assertNotNull(cloneBuilder);
        assertEquals(dimensionData, cloneBuilder.build());

        // show that the clone builder builds a distinct instance if any mutation is
        // made

        // setTrackTimes
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.setTrackTimes(true);
        assertNotEquals(dimensionData, cloneBuilder.build());

        // setPersonPropertyId
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.setPersonPropertyId(TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator));
        assertNotEquals(dimensionData, cloneBuilder.build());

        // addValue
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.addValue("Level_2", "newValue");
        assertNotEquals(dimensionData, cloneBuilder.build());
    }

    /*
     * Generates a random PersonPropertyDimensionData with 2790 possible outcomes
     */
    private PersonPropertyDimensionData getRandomPersonPropertyDimensionData(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

        PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
                .setPersonPropertyId(testPersonPropertyId)//
                .setTrackTimes(randomGenerator.nextBoolean());

        int count = randomGenerator.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            builder.addValue("Level_" + i, testPersonPropertyId.getRandomPropertyValue(randomGenerator));
        }

        return builder.build();
    }
}
