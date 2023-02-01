package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestSimulationOutputConsumer {

    @Test
    @UnitTestConstructor(target = TestSimulationOutputConsumer.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestSimulationOutputConsumer());
    }

    @Test
    @UnitTestMethod(target = TestSimulationOutputConsumer.class, name = "accept", args = { Object.class })
    public void testAccept() {
        TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

        assertDoesNotThrow(() -> outputConsumer.accept(true));
        assertDoesNotThrow(() -> outputConsumer.accept(new TestScenarioReport(true)));
        ContractException contractException = assertThrows(ContractException.class,
                () -> outputConsumer.accept(new TestScenarioReport(true)));
        assertEquals(TestError.DUPLICATE_TEST_SCENARIO_REPORTS, contractException.getErrorType());

        assertEquals(1, outputConsumer.getOutputItems(Boolean.class).size());
        assertEquals(1, outputConsumer.getOutputItems(TestScenarioReport.class).size());
        assertTrue(outputConsumer.isComplete());
    }

    @Test
    @UnitTestMethod(target = TestSimulationOutputConsumer.class, name = "getOutputItems", args = { Class.class })
    public void testGetOutputItems() {
        TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7830499412883085962L);

        Map<Integer, MutableInteger> expectedIntValuesM = new LinkedHashMap<>();
        Map<Double, MutableInteger> expectedDoubleValuesM = new LinkedHashMap<>();
        Map<Float, MutableInteger> expectedFloatValuesM = new LinkedHashMap<>();
        Map<Long, MutableInteger> expectedLongValuesM = new LinkedHashMap<>();

        for (int i = 0; i < 100; i++) {
            boolean shouldAdd = randomGenerator.nextBoolean();

            if (shouldAdd) {
                int intVal = randomGenerator.nextInt(100);
                double doubleVal = randomGenerator.nextDouble() * 100;
                float floatVal = randomGenerator.nextFloat() * 100;
                long longVal = randomGenerator.nextLong();

                outputConsumer.accept(intVal);
                outputConsumer.accept(doubleVal);
                outputConsumer.accept(floatVal);
                outputConsumer.accept(longVal);

                expectedIntValuesM.putIfAbsent(intVal, new MutableInteger());
                expectedDoubleValuesM.putIfAbsent(doubleVal, new MutableInteger());
                expectedFloatValuesM.putIfAbsent(floatVal, new MutableInteger());
                expectedLongValuesM.putIfAbsent(longVal, new MutableInteger());

                expectedIntValuesM.get(intVal).increment();
                expectedDoubleValuesM.get(doubleVal).increment();
                expectedFloatValuesM.get(floatVal).increment();
                expectedLongValuesM.get(longVal).increment();
            }
        }

        Map<Integer, Integer> expectedIntValues = new LinkedHashMap<>();
        Map<Double, Integer> expectedDoubleValues = new LinkedHashMap<>();
        Map<Float, Integer> expectedFloatValues = new LinkedHashMap<>();
        Map<Long, Integer> expectedLongValues = new LinkedHashMap<>();

        for (Integer i : expectedIntValuesM.keySet()) {
            expectedIntValues.put(i, expectedIntValuesM.get(i).getValue());
        }

        for (Double i : expectedDoubleValuesM.keySet()) {
            expectedDoubleValues.put(i, expectedDoubleValuesM.get(i).getValue());
        }

        for (Float i : expectedFloatValuesM.keySet()) {
            expectedFloatValues.put(i, expectedFloatValuesM.get(i).getValue());
        }

        for (Long i : expectedLongValuesM.keySet()) {
            expectedLongValues.put(i, expectedLongValuesM.get(i).getValue());
        }

        assertEquals(expectedIntValues, outputConsumer.getOutputItems(Integer.class));
        assertEquals(expectedDoubleValues, outputConsumer.getOutputItems(Double.class));
        assertEquals(expectedFloatValues, outputConsumer.getOutputItems(Float.class));
        assertEquals(expectedLongValues, outputConsumer.getOutputItems(Long.class));
    }

    @Test
    @UnitTestMethod(target = TestSimulationOutputConsumer.class, name = "isComplete", args = {})
    public void testIsComplete() {
        TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

        assertFalse(outputConsumer.isComplete());

        outputConsumer.accept(new TestScenarioReport(false));
        assertFalse(outputConsumer.isComplete());

        outputConsumer.accept(new TestScenarioReport(true));
        assertTrue(outputConsumer.isComplete());
    }
}
