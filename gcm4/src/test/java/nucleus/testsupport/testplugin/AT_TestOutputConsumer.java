package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestOutputConsumer {

	@Test
	@UnitTestConstructor(target = TestOutputConsumer.class, args = {})
	public void testConstructor() {
		assertNotNull(new TestOutputConsumer());
	}

	@Test
	@UnitTestMethod(target = TestOutputConsumer.class, name = "accept", args = { Object.class })
	public void testAccept() {
		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		outputConsumer.accept(true);
		outputConsumer.accept(false);
		outputConsumer.accept(true);
		outputConsumer.accept(false);
		outputConsumer.accept(true);
		
		Map<Boolean, Integer> expectedOutput = new LinkedHashMap<>();
		expectedOutput.put(true, 3);
		expectedOutput.put(false, 2);
		

		ContractException contractException = assertThrows(ContractException.class, () -> outputConsumer.accept(null));
		assertEquals(TestError.NULL_OUTPUT_ITEM, contractException.getErrorType());

		Map<Boolean, Integer> actualOutput = outputConsumer.getOutputItemMap(Boolean.class);
		assertEquals(expectedOutput, actualOutput);


	}

	@Test
	@UnitTestMethod(target = TestOutputConsumer.class, name = "getOutputItems", args = { Class.class })
	public void testGetOutputItems() {
		TestOutputConsumer outputConsumer = new TestOutputConsumer();

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

		assertEquals(expectedIntValues, outputConsumer.getOutputItemMap(Integer.class));
		assertEquals(expectedDoubleValues, outputConsumer.getOutputItemMap(Double.class));
		assertEquals(expectedFloatValues, outputConsumer.getOutputItemMap(Float.class));
		assertEquals(expectedLongValues, outputConsumer.getOutputItemMap(Long.class));
	}
	
//	TestOutputConsumer	public java.util.Map nucleus.testsupport.testplugin.TestOutputConsumer.getOutputItemMap(java.lang.Class) 
//	TestOutputConsumer	public java.util.Optional nucleus.testsupport.testplugin.TestOutputConsumer.getOutputItem(java.lang.Class) 


}
