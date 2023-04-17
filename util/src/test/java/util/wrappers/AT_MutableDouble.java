package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_MutableDouble {

	@Test
	@UnitTestConstructor(target = MutableDouble.class, args = {})
	public void testConstructor() {
		MutableDouble mutableDouble = new MutableDouble();
		assertEquals(0.0, mutableDouble.getValue());
	}

	@Test
	@UnitTestConstructor(target = MutableDouble.class, args = { double.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8225050097744068353L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			assertEquals(value, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "decrement", args = {})
	public void testDecrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1707576220414487656L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			mutableDouble.decrement();
			assertEquals(value - 1, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "decrement", args = { double.class })
	public void testDecrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6764243104145521113L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.decrement(value2);
			assertEquals(value - value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "increment", args = {})
	public void testIncrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1599603941820866111L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			mutableDouble.increment();
			assertEquals(value + 1, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "increment", args = { double.class })
	public void testIncrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7574093515830044644L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.increment(value2);
			assertEquals(value + value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6556525952353208885L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			assertEquals(value, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "setValue", args = { double.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5537659412858832599L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble = new MutableDouble(value);
			double value2 = (randomGenerator.nextDouble() - 0.5) * 1000;
			mutableDouble.setValue(value2);
			assertEquals(value2, mutableDouble.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1088491726743804976L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble1 = new MutableDouble(value);
			MutableDouble mutableDouble2 = new MutableDouble(value);
			assertEquals(mutableDouble1.getValue(), mutableDouble2.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "hashCode", args = {})
	public void testHashCode() {

		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(681314648353227104L);
		for (int i = 0; i < 30; i++) {
			double value = (randomGenerator.nextDouble() - 0.5) * 1000;
			MutableDouble mutableDouble1 = new MutableDouble(value);
			MutableDouble mutableDouble2 = new MutableDouble(value);
			assertEquals(mutableDouble1.hashCode(), mutableDouble2.hashCode());
		}

	}

	@Test
	@UnitTestMethod(target = MutableDouble.class, name = "toString", args = {})
	public void testToString() {
		MutableDouble mutableDouble = new MutableDouble(2.5);
		String expectedValue = "MutableDouble [value=2.5]";
		String actualValue = mutableDouble.toString();
		assertEquals(expectedValue, actualValue);

		mutableDouble = new MutableDouble(62.598);
		expectedValue = "MutableDouble [value=62.598]";
		actualValue = mutableDouble.toString();
		assertEquals(expectedValue, actualValue);

	}

}
