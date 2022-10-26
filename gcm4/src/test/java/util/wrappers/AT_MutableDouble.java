package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = MutableDouble.class)
public class AT_MutableDouble {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MutableDouble mutableDouble = new MutableDouble();
		assertEquals(0.0, mutableDouble.getValue());
	}

	@Test
	@UnitTestConstructor(args = { double.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3824074981405252870L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			assertEquals(value, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "decrement", args = {})
	public void testDecrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1556136528392901493L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			mutableDouble.decrement();
			assertEquals(value - 1, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "decrement", args = { double.class })
	public void testDecrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6417530085366013967L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.decrement(value2);
			assertEquals(value - value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "increment", args = {})
	public void testIncrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5989612764991268700L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			mutableDouble.increment();
			assertEquals(value + 1, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "increment", args = { double.class })
	public void testIncrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7024132741444760192L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.increment(value2);
			assertEquals(value + value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6346882071250399307L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			assertEquals(value, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "setValue", args = { double.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6410956163545832731L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.setValue(value2);
			assertEquals(value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5255250189313941474L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble1 = new MutableDouble(value);
			MutableDouble mutableDouble2 = new MutableDouble(value);
			assertEquals(mutableDouble1.getValue(), mutableDouble2.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5255250189313941474L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble1 = new MutableDouble(value);
			MutableDouble mutableDouble2 = new MutableDouble(value);
			assertEquals(mutableDouble1.hashCode(), mutableDouble2.hashCode());
		}

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MutableDouble mutableDouble = new MutableDouble(2.5);
		String expectedValue = "MutableDouble [value=2.5]";
		String actualValue = mutableDouble.toString();
		assertEquals(actualValue, expectedValue);
		
		mutableDouble = new MutableDouble(62.598);
		expectedValue = "MutableDouble [value=62.598]";
		actualValue = mutableDouble.toString();
		assertEquals(actualValue, expectedValue);

	}

}
