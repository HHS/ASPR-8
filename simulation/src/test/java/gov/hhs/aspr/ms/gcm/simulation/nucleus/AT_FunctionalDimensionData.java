package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_FunctionalDimensionData {

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.Builder.class, name = "addValue", args = { String.class,
            Function.class })
    public void testAddValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565056L);

        for (int i = 0; i < 50; i++) {
            List<Function<DimensionContext, List<String>>> expectedValues = new ArrayList<>();

            FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                List<String> randomTestData = getRandomTestData(randomGenerator);
                Function<DimensionContext, List<String>> randomTestFunction = ((c) -> {
                    return randomTestData;
                });

                dimDataBuilder.addValue("Level_" + j, randomTestFunction);
                expectedValues.add(randomTestFunction);
            }

            FunctionalDimensionData dimensionData = dimDataBuilder.build();
            List<Function<DimensionContext, List<String>>> actualValues = dimensionData.getValues();

            assertEquals(expectedValues, actualValues);
        }

        // precondition test : if the level is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> FunctionalDimensionData.builder().addValue(null, (context) -> {
                    return new ArrayList<>();
                }));
        assertEquals(NucleusError.NULL_DIMENSION_LEVEL_NAME, contractException.getErrorType());

        // precondition test : if the value is null
        contractException = assertThrows(ContractException.class,
                () -> FunctionalDimensionData.builder().addValue("Level_0", null));
        assertEquals(NucleusError.NULL_FUNCTION, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.Builder.class, name = "build", args = {})
    public void testBuild() {
        assertNotNull(FunctionalDimensionData.builder().build());

        // precondition test: if the dimension data contains duplicate level names
        ContractException contractException = assertThrows(ContractException.class, () -> {
            FunctionalDimensionData.builder()
                    ._addLevelName("bad")
                    ._addLevelName("bad")
                    .build();
        });

        assertEquals(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.Builder.class, name = "addMetaDatum", args = { String.class })
    public void testAddMetaDatum() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 10; i++) {
            FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();
            List<String> expectedValues = new ArrayList<>();

            List<String> randomTestData = getRandomTestData(randomGenerator);
            for (String randomTestDatum : randomTestData) {
                dimDataBuilder.addMetaDatum(randomTestDatum);
                expectedValues.add(randomTestDatum);
            }

            FunctionalDimensionData functionalDimensionData = dimDataBuilder.build();

            assertEquals(expectedValues, functionalDimensionData.getMetaData());
        }

        // precondition test : if the metaDatum is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> FunctionalDimensionData.builder().addMetaDatum(null));
        assertEquals(NucleusError.NULL_META_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(FunctionalDimensionData.builder());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "getMetaData", args = {})
    public void testGetMetaData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        for (int i = 0; i < 10; i++) {
            FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();
            List<String> expectedValues = new ArrayList<>();

            List<String> randomTestData = getRandomTestData(randomGenerator);
            for (String randomTestDatum : randomTestData) {
                dimDataBuilder.addMetaDatum(randomTestDatum);
                expectedValues.add(randomTestDatum);
            }

            FunctionalDimensionData functionalDimensionData = dimDataBuilder.build();

            assertEquals(expectedValues, functionalDimensionData.getMetaData());
        }
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "getValue", args = { int.class })
    public void testGetValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

        List<String> expectedLevelNames = new ArrayList<>();
        List<Function<DimensionContext, List<String>>> expectedValues = new ArrayList<>();

        FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

        int levels = randomGenerator.nextInt(10);
        for (int i = 0; i < levels; i++) {
            List<String> randomTestData = getRandomTestData(randomGenerator);
            Function<DimensionContext, List<String>> expectedValue = ((c) -> {
                return randomTestData;
            });

            dimDataBuilder.addValue("Level_" + i, expectedValue);
            expectedValues.add(expectedValue);
            expectedLevelNames.add("Level_" + i);
        }

        FunctionalDimensionData functionalDimensionData = dimDataBuilder.build();

        assertEquals(expectedLevelNames.size(), expectedValues.size());

        for (int i = 0; i < expectedValues.size(); i++) {
            Function<DimensionContext, List<String>> expectedValue = expectedValues.get(i);
            Function<DimensionContext, List<String>> actualValue = functionalDimensionData.getValue(i);
            assertEquals(expectedValue, actualValue);
        }

        // preconditions: negative level
        ContractException contractException = assertThrows(ContractException.class, () -> {
            functionalDimensionData.getValue(-1);
        });
        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

        // preconditions: level greater than total levels
        contractException = assertThrows(ContractException.class, () -> {
            functionalDimensionData.getValue(functionalDimensionData.getLevelCount() + 2);
        });

        assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "getValues", args = {})
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565058L);

        for (int i = 0; i < 50; i++) {
            List<Function<DimensionContext, List<String>>> expectedValues = new ArrayList<>();

            FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

            int n = randomGenerator.nextInt(10);
            for (int j = 0; j < n; j++) {
                List<String> randomTestData = getRandomTestData(randomGenerator);
                Function<DimensionContext, List<String>> randomTestFunction = ((c) -> {
                    return randomTestData;
                });

                dimDataBuilder.addValue("Level_" + j, randomTestFunction);
                expectedValues.add(randomTestFunction);
            }

            FunctionalDimensionData dimensionData = dimDataBuilder.build();
            List<Function<DimensionContext, List<String>>> actualValues = dimensionData.getValues();

            assertEquals(expectedValues, actualValues);

        }
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "getVersion", args = {})
    public void testGetVersion() {
        FunctionalDimensionData dimData = FunctionalDimensionData.builder()//
                .build();
        assertEquals(StandardVersioning.VERSION, dimData.getVersion());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "checkVersionSupported", args = { String.class })
    public void testCheckVersionSupported() {
        List<String> versions = Arrays.asList(StandardVersioning.VERSION);

        for (String version : versions) {
            assertTrue(FunctionalDimensionData.checkVersionSupported(version));
            assertFalse(FunctionalDimensionData.checkVersionSupported(version + "badVersion"));
            assertFalse(FunctionalDimensionData.checkVersionSupported("badVersion"));
            assertFalse(FunctionalDimensionData.checkVersionSupported(version + "0"));
            assertFalse(FunctionalDimensionData.checkVersionSupported(version + ".0.0"));
        }
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "hashCode", args = {})
    public void testHashCode() {

        Function<DimensionContext, List<String>> commonValue1 = ((c) -> {
            return new ArrayList<>();
        });

        Function<DimensionContext, List<String>> commonValue2 = ((c) -> {
            List<String> result = new ArrayList<>();
            result.add("same");
            return result;
        });

        FunctionalDimensionData dimData1 = FunctionalDimensionData.builder()
                .addMetaDatum("0")
                .addValue("Level_0", commonValue1)
                .build();

        FunctionalDimensionData dimData2 = FunctionalDimensionData.builder()
                .addMetaDatum("1")
                .addValue("Level_0", (c) -> {
                    return new ArrayList<>();
                })
                .build();

        FunctionalDimensionData dimData3 = FunctionalDimensionData.builder()
                .addMetaDatum("1")
                .addValue("Level_0", (c) -> {
                    List<String> result = new ArrayList<>();
                    result.add("1");
                    return result;
                })
                .build();

        FunctionalDimensionData dimData4 = FunctionalDimensionData.builder()
                .addMetaDatum("2")
                .addValue("Level_0", commonValue2)
                .build();

        FunctionalDimensionData dimData5 = FunctionalDimensionData.builder()
                .addMetaDatum("3")
                .addValue("Level_0", commonValue2)
                .build();

        FunctionalDimensionData dimData6 = FunctionalDimensionData.builder()
                .addMetaDatum("0")
                .addValue("Level_0", commonValue1)
                .build();

        assertEquals(dimData1.hashCode(), dimData1.hashCode());

        assertNotEquals(dimData1.hashCode(), dimData2.hashCode());
        assertNotEquals(dimData1.hashCode(), dimData3.hashCode());
        assertNotEquals(dimData1.hashCode(), dimData4.hashCode());
        assertNotEquals(dimData1.hashCode(), dimData5.hashCode());

        assertNotEquals(dimData2.hashCode(), dimData3.hashCode());
        assertNotEquals(dimData2.hashCode(), dimData4.hashCode());
        assertNotEquals(dimData2.hashCode(), dimData5.hashCode());
        assertNotEquals(dimData2.hashCode(), dimData6.hashCode());

        assertNotEquals(dimData3.hashCode(), dimData4.hashCode());
        assertNotEquals(dimData3.hashCode(), dimData5.hashCode());
        assertNotEquals(dimData3.hashCode(), dimData6.hashCode());

        assertNotEquals(dimData4.hashCode(), dimData5.hashCode());
        assertNotEquals(dimData4.hashCode(), dimData6.hashCode());

        assertNotEquals(dimData5.hashCode(), dimData6.hashCode());

        assertEquals(dimData1.hashCode(), dimData6.hashCode());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "equals", args = { Object.class })
    public void testEquals() {

        Function<DimensionContext, List<String>> commonValue1 = ((c) -> {
            return new ArrayList<>();
        });

        Function<DimensionContext, List<String>> commonValue2 = ((c) -> {
            List<String> result = new ArrayList<>();
            result.add("same");
            return result;
        });

        FunctionalDimensionData dimData1 = FunctionalDimensionData.builder()
                .addMetaDatum("0")
                .addValue("Level_0", commonValue1)
                .build();

        FunctionalDimensionData dimData2 = FunctionalDimensionData.builder()
                .addMetaDatum("1")
                .addValue("Level_0", (c) -> {
                    return new ArrayList<>();
                })
                .build();

        FunctionalDimensionData dimData3 = FunctionalDimensionData.builder()
                .addMetaDatum("1")
                .addValue("Level_0", (c) -> {
                    List<String> result = new ArrayList<>();
                    result.add("1");
                    return result;
                })
                .build();

        FunctionalDimensionData dimData4 = FunctionalDimensionData.builder()
                .addMetaDatum("2")
                .addValue("Level_0", commonValue2)
                .build();

        FunctionalDimensionData dimData5 = FunctionalDimensionData.builder()
                .addMetaDatum("3")
                .addValue("Level_0", commonValue2)
                .build();

        FunctionalDimensionData dimData6 = FunctionalDimensionData.builder()
                .addMetaDatum("0")
                .addValue("Level_0", commonValue1)
                .build();

        assertEquals(dimData1, dimData1);
        assertNotEquals(dimData1, null);
        assertNotEquals(dimData1, new Object());

        assertNotEquals(dimData1, dimData2);
        assertNotEquals(dimData1, dimData3);
        assertNotEquals(dimData1, dimData4);
        assertNotEquals(dimData1, dimData5);

        assertNotEquals(dimData2, dimData3);
        assertNotEquals(dimData2, dimData4);
        assertNotEquals(dimData2, dimData5);
        assertNotEquals(dimData2, dimData6);

        assertNotEquals(dimData3, dimData4);
        assertNotEquals(dimData3, dimData5);
        assertNotEquals(dimData3, dimData6);

        assertNotEquals(dimData4, dimData5);
        assertNotEquals(dimData4, dimData6);

        assertNotEquals(dimData5, dimData6);

        assertEquals(dimData1, dimData6);
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "toString", args = {})
    public void testToString() {

        FunctionalDimensionData.Builder dimensionDataBuilder = FunctionalDimensionData.builder();

        Function<DimensionContext, List<String>> targetValue1 = new Function<>() {
            @Override
            public List<String> apply(DimensionContext dimensionContext) {
                return new ArrayList<>();
            }
        };

        Function<DimensionContext, List<String>> targetValue2 = new Function<>() {
            @Override
            public List<String> apply(DimensionContext dimensionContext) {
                return new ArrayList<>();
            }
        };

        dimensionDataBuilder
                .addMetaDatum("A")
                .addMetaDatum("B")
                .addValue("Level_0", targetValue1)
                .addValue("Level_1", targetValue2);

        FunctionalDimensionData dimensionData = dimensionDataBuilder.build();

        StringBuilder builder = new StringBuilder();
        builder.append("FunctionalDimensionData [data=");
        builder.append("Data [levelNames=[");
        builder.append("Level_0, Level_1]");
        builder.append(", values=[");
        builder.append(targetValue1.toString() + ", ");
        builder.append(targetValue2.toString() + "]");
        builder.append(", metaData=[");
        builder.append("A, B");
        builder.append("]");
        builder.append("]]");

        String expectedString = builder.toString();

        assertEquals(expectedString, dimensionData.toString());
    }

    @Test
    @UnitTestMethod(target = FunctionalDimensionData.class, name = "toBuilder", args = {})
    public void testToBuilder() {

        FunctionalDimensionData dimensionData = FunctionalDimensionData.builder()
                .addMetaDatum("A")
                .addValue("Level_0", (c) -> {
                    return new ArrayList<>();
                })
                .build();

        // show that the returned clone builder will build an identical instance if no
        // mutations are made
        FunctionalDimensionData.Builder cloneBuilder = dimensionData.toBuilder();
        assertNotNull(cloneBuilder);
        assertEquals(dimensionData, cloneBuilder.build());

        // show that the clone builder builds a distinct instance if any mutation is
        // made

        // addMetaDatum
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.addMetaDatum("B");
        assertNotEquals(dimensionData, cloneBuilder.build());

        // addValue
        cloneBuilder = dimensionData.toBuilder();
        cloneBuilder.addValue("Level_1", (c) -> {
            return new ArrayList<>();
        });
        assertNotEquals(dimensionData, cloneBuilder.build());
    }

    private List<String> getRandomTestData(RandomGenerator randomGenerator) {
        String[] dataOptions = { "A", "B", "C", "D", "E" };
        List<String> result = new ArrayList<>();

        int n = randomGenerator.nextInt(10);
        for (int i = 0; i < n; i++) {
            String selectedDatum = dataOptions[randomGenerator.nextInt(5)];
            result.add(selectedDatum);
        }

        return result;
    }
}
