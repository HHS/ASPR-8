package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

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
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyDimensionData {

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.Builder.class, name = "addValue", args = { String.class, Object.class })
    public void testAddValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                Double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue("Level_" + j, value);
            }
            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();

            List<Object> actualValues = globalPropertyDimensionData.getValues();
            assertEquals(expectedValues, actualValues);
        }

        // precondition test : if the level is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertyDimensionData.builder().addValue(null, "testValue"));
        assertEquals(NucleusError.NULL_DIMENSION_LEVEL_NAME, contractException.getErrorType());

        // precondition test : if the value is null
        ContractException contractException2 = assertThrows(ContractException.class,
                () -> GlobalPropertyDimensionData.builder().addValue("Level_0", null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException2.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.Builder.class, name = "build", args = {})
    public void testBuild() {
        GlobalPropertyDimensionData globalPropertyDimensionData = GlobalPropertyDimensionData.builder()//
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE)//
                .build();
        assertNotNull(globalPropertyDimensionData);

        // precondition test : if the global property id is not assigned
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertyDimensionData.builder().build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if the dimension data contains duplicate level names
        ContractException contractException2 = assertThrows(ContractException.class, () -> {
            GlobalPropertyDimensionData.builder()
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE)
                    ._addLevelName("bad")
                    ._addLevelName("bad")
                    .build();
        });

        assertEquals(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME, contractException2.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.Builder.class, name = "setAssignmentTime", args = {
            double.class })
    public void testSetAssignmentTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7384717734933740607L);

        for (int i = 0; i < 50; i++) {
            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
            double assignmentTime = randomGenerator.nextDouble();
            builder.setAssignmentTime(assignmentTime);
            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();

            assertEquals(assignmentTime, globalPropertyDimensionData.getAssignmentTime());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.Builder.class, name = "setGlobalPropertyId", args = {
            GlobalPropertyId.class })
    public void testSetGlobalPropertyId() {
        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(testGlobalPropertyId);

            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();
            assertEquals(testGlobalPropertyId, globalPropertyDimensionData.getGlobalPropertyId());
        }

        // precondition test : if the value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GlobalPropertyDimensionData.builder().setGlobalPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GlobalPropertyDimensionData.builder());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "getGlobalPropertyId", args = {})
    public void testGetGlobalPropertyId() {
        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(testGlobalPropertyId);

            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();
            assertEquals(testGlobalPropertyId, globalPropertyDimensionData.getGlobalPropertyId());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "getValue", args = { int.class })
    public void testGetValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        List<Object> expectedValues = new ArrayList<>();
        List<String> expectedLevelNames = new ArrayList<>();

        TestGlobalPropertyId targetPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
        GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                .setGlobalPropertyId(targetPropertyId)
                .setAssignmentTime(0);

        int levels = randomGenerator.nextInt();

        for (int i = 0; i < levels; i++) {
            Object expectedValue = targetPropertyId.getRandomPropertyValue(randomGenerator);
            expectedValues.add(expectedValue);
            expectedLevelNames.add("Level_" + i);
            builder.addValue("Level_" + i, expectedValue);
        }

        GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();

        assertEquals(expectedLevelNames.size(), expectedValues.size());

        for (int i = 0; i < expectedValues.size(); i++) {
            Object expectedValue = expectedValues.get(i);
            Object actualValue = globalPropertyDimensionData.getValue(i);
            assertEquals(expectedValue, actualValue);
        }

        // preconditions: negative level
        ContractException contractException = assertThrows(ContractException.class, () -> {
            globalPropertyDimensionData.getValue(-1);
        });
        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

        // preconditions: level greater than total levels
        contractException = assertThrows(ContractException.class, () -> {
            globalPropertyDimensionData.getValue(globalPropertyDimensionData.getLevelCount() + 2);
        });

        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "getValues", args = {})
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 50; i++) {
            List<Object> expectedValues = new ArrayList<>();

            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                double value = randomGenerator.nextDouble();
                expectedValues.add(value);
                builder.addValue("Level_" + j, value);
            }
            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();

            List<Object> actualValues = globalPropertyDimensionData.getValues();
            assertEquals(expectedValues, actualValues);
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "getAssignmentTime", args = {})
    public void testGetAssignmentTime() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

        for (int i = 0; i < 50; i++) {
            GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder()//
                    .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
            double assignmentTime = randomGenerator.nextDouble();
            builder.setAssignmentTime(assignmentTime);
            GlobalPropertyDimensionData globalPropertyDimensionData = builder.build();

            assertEquals(assignmentTime, globalPropertyDimensionData.getAssignmentTime());
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "getVersion", args = {})
    public void testGetVersion() {

        GlobalPropertyDimensionData dimData = GlobalPropertyDimensionData.builder()//
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE)
                .build();

        assertEquals(StandardVersioning.VERSION, dimData.getVersion());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "checkVersionSupported", args = { String.class })
    public void testCheckVersionSupported() {
        List<String> versions = Arrays.asList(StandardVersioning.VERSION);

        for (String version : versions) {
            assertTrue(StandardVersioning.checkVersionSupported(version));
            assertFalse(StandardVersioning.checkVersionSupported(version + "badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported("badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported(version + "0"));
            assertFalse(StandardVersioning.checkVersionSupported(version + ".0.0"));
        }
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8266930019491275913L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertyDimensionData globalPropertyDimensionData1 = getRandomGlobalPropertyDimensionData(seed);
			GlobalPropertyDimensionData globalPropertyDimensionData2 = getRandomGlobalPropertyDimensionData(seed);

			assertEquals(globalPropertyDimensionData1, globalPropertyDimensionData2);
			assertEquals(globalPropertyDimensionData1.hashCode(), globalPropertyDimensionData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertyDimensionData globalPropertyDimensionData = getRandomGlobalPropertyDimensionData(randomGenerator.nextLong());
			hashCodes.add(globalPropertyDimensionData.hashCode());
		}
		
		assertEquals(100, hashCodes.size());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "equals", args = { Object.class })
    public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980205493557306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
            GlobalPropertyDimensionData globalPropertyDimensionData = getRandomGlobalPropertyDimensionData(randomGenerator.nextLong());
            assertFalse(globalPropertyDimensionData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
            GlobalPropertyDimensionData globalPropertyDimensionData = getRandomGlobalPropertyDimensionData(randomGenerator.nextLong());
            assertFalse(globalPropertyDimensionData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
            GlobalPropertyDimensionData globalPropertyDimensionData = getRandomGlobalPropertyDimensionData(randomGenerator.nextLong());
            assertTrue(globalPropertyDimensionData.equals(globalPropertyDimensionData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertyDimensionData globalPropertyDimensionData1 = getRandomGlobalPropertyDimensionData(seed);
			GlobalPropertyDimensionData globalPropertyDimensionData2 = getRandomGlobalPropertyDimensionData(seed);
			assertFalse(globalPropertyDimensionData1 == globalPropertyDimensionData2);
			for (int j = 0; j < 10; j++) {				
				assertTrue(globalPropertyDimensionData1.equals(globalPropertyDimensionData2));
				assertTrue(globalPropertyDimensionData2.equals(globalPropertyDimensionData1));
			}
		}

		// different inputs yield unequal GlobalPropertyDimensionDatas
		Set<GlobalPropertyDimensionData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertyDimensionData globalPropertyDimensionData = getRandomGlobalPropertyDimensionData(randomGenerator.nextLong());
			set.add(globalPropertyDimensionData);
		}
		assertEquals(100, set.size());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "toString", args = {})
    public void testToString() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

        TestGlobalPropertyId targetPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
        Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Double targetAssignDouble = randomGenerator.nextDouble();

        GlobalPropertyDimensionData dimensionData = GlobalPropertyDimensionData.builder()
                .setGlobalPropertyId(targetPropertyId)
                .addValue("Level_0", targetValue1)
                .addValue("Level_1", targetValue2)
                .setAssignmentTime(targetAssignDouble)
                .build();

        StringBuilder builder = new StringBuilder();
        builder.append("GlobalPropertyDimensionData [data=");
        builder.append("Data [levelNames=[");
        builder.append("Level_0, Level_1]");
        builder.append(", values=[");
        builder.append(targetValue1.toString() + ", ");
        builder.append(targetValue2.toString() + "]");
        builder.append(", globalPropertyId=");
        builder.append(targetPropertyId.toString());
        builder.append(", assignmentTime=");
        builder.append(targetAssignDouble.toString());
        builder.append("]");
        builder.append("]");

        String expectedString = builder.toString();

        assertEquals(expectedString, dimensionData.toString());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyDimensionData.class, name = "toBuilder", args = {})
    public void testToBuilder() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

        TestGlobalPropertyId targetPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
        Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
        Double targetAssignDouble = randomGenerator.nextDouble();

        GlobalPropertyDimensionData dimensionData = GlobalPropertyDimensionData.builder()
                .setGlobalPropertyId(targetPropertyId)
                .addValue("Level_0", targetValue1)
                .addValue("Level_1", targetValue2)
                .setAssignmentTime(targetAssignDouble)
                .build();

        // show that the returned clone builder will build an identical instance if no
        // mutations are made
        GlobalPropertyDimensionData.Builder cloneBuilder = dimensionData.toBuilder();
        assertNotNull(cloneBuilder);
        assertEquals(dimensionData, cloneBuilder.build());

        // show that the clone builder builds a distinct instance if any mutation is
        // made

        // setAssignmentTime
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.setAssignmentTime(99);
        assertNotEquals(dimensionData, cloneBuilder.build());

        // setGlobalPropertyId
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.setGlobalPropertyId(TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator));
        assertNotEquals(dimensionData, cloneBuilder.build());

        // setGlobalPropertyId
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.addValue("Level_2", "newValue");
        assertNotEquals(dimensionData, cloneBuilder.build());
    }

    private GlobalPropertyDimensionData getRandomGlobalPropertyDimensionData(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        GlobalPropertyDimensionData.Builder builder = GlobalPropertyDimensionData.builder();

        TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
        builder.setGlobalPropertyId(testGlobalPropertyId);

        Double testAssignmentTime = randomGenerator.nextDouble();
        builder.setAssignmentTime(testAssignmentTime);

        int n = randomGenerator.nextInt(10) + 1;
        for (int i = 0; i < n; i++) {
            Object testPropertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
            builder.addValue("Level_" + i, testPropertyValue);
        }

        return builder.build();
    }
}
