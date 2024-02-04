package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

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
	@UnitTestMethod(target = TestOutputConsumer.class, name = "getOutputItemMap", args = { Class.class })
	public void testGetOutputItemMap() {
		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7830499412883085962L);

		Map<Integer, MutableInteger> expectedIntValuesM = new LinkedHashMap<>();
		Map<Double, MutableInteger> expectedDoubleValuesM = new LinkedHashMap<>();
		Map<Float, MutableInteger> expectedFloatValuesM = new LinkedHashMap<>();
		Map<Long, MutableInteger> expectedLongValuesM = new LinkedHashMap<>();

		for (int i = 0; i < 1; i++) {
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

	@Test
	@UnitTestMethod(target = TestOutputConsumer.class, name = "getOutputItems", args = { Class.class })
	public void testGetOutputItems() {
		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3616113008200960520L);

		List<Integer> expectedIntegers = new ArrayList<>();
		List<Double> expectedDoubles = new ArrayList<>();
		List<String> expectedStrings = new ArrayList<>();
		List<Boolean> expectedBooleans = new ArrayList<>();
		List<Boolean> expectedFloats = new ArrayList<>();

		for (int i = 0; i < 100; i++) {

			int type = randomGenerator.nextInt(4);
			
			switch (type) {
			case 0:
				Integer ivalue = randomGenerator.nextInt();
				expectedIntegers.add(ivalue);
				outputConsumer.accept(ivalue);
				break;
			case 1:
				Double dvalue = randomGenerator.nextDouble();
				expectedDoubles.add(dvalue);
				outputConsumer.accept(dvalue);
				break;
			case 2:
				String svalue = Integer.toString(randomGenerator.nextInt());
				expectedStrings.add(svalue);
				outputConsumer.accept(svalue);
				break;
			case 3:
				Boolean bvalue = randomGenerator.nextBoolean();
				expectedBooleans.add(bvalue);
				outputConsumer.accept(bvalue);
				break;
			default:
				throw new RuntimeException("unhandled case");
			}
		}
		
		assertEquals(expectedIntegers, outputConsumer.getOutputItems(Integer.class));
		assertEquals(expectedDoubles, outputConsumer.getOutputItems(Double.class));
		assertEquals(expectedFloats, outputConsumer.getOutputItems(Float.class));
		assertEquals(expectedStrings, outputConsumer.getOutputItems(String.class));
		assertEquals(expectedBooleans, outputConsumer.getOutputItems(Boolean.class));

	}


	@Test
	@UnitTestMethod(target = TestOutputConsumer.class, name = "getOutputItem", args = { Class.class })
	public void testGetOutputItem() {
		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		
		Optional<Integer> optional = outputConsumer.getOutputItem(Integer.class);
		assertTrue(optional.isEmpty());
		
		outputConsumer.accept(5);
		optional = outputConsumer.getOutputItem(Integer.class);
		assertTrue(optional.isPresent());
		assertEquals(5, optional.get());
		
		outputConsumer.accept(5);
		ContractException contractException = assertThrows(ContractException.class,()->outputConsumer.getOutputItem(Integer.class));
		assertEquals(TestError.MULTIPLE_MATCHING_ITEMS, contractException.getErrorType());
	}

}
