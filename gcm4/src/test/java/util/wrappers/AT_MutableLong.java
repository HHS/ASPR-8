package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_MutableLong {

	@Test
	@UnitTestConstructor(target = MutableLong.class, args = {})
	public void testConstructor() {
		MutableLong mutableLong = new MutableLong();
		assertEquals(0.0, mutableLong.getValue());
	}

	@Test
	@UnitTestConstructor(target = MutableLong.class, args = { long.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3824074981405252870L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			assertEquals(value, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "decrement", args = {})
	public void testDecrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1556136528392901493L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			mutableLong.decrement();
			assertEquals(value - 1, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "decrement", args = { long.class })
	public void testDecrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6417530085366013967L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			long value2 = randomGenerator.nextInt(1000);
			mutableLong.decrement(value2);
			assertEquals(value - value2, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "increment", args = {})
	public void testIncrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5989612764991268700L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			mutableLong.increment();
			assertEquals(value + 1, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "increment", args = { long.class })
	public void testIncrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7024132741444760192L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			long value2 = randomGenerator.nextInt(1000);
			mutableLong.increment(value2);
			assertEquals(value + value2, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6346882071250399307L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			assertEquals(value, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "setValue", args = { long.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6410956163545832731L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong = new MutableLong(value);
			long value2 = randomGenerator.nextInt(1000);
			mutableLong.setValue(value2);
			assertEquals(value2, mutableLong.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(715063174794811583L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong1 = new MutableLong(value);
			MutableLong mutableLong2 = new MutableLong(value);
			assertEquals(mutableLong1.getValue(), mutableLong2.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "hashCode", args = {})
	public void testHashCode() {

		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5255250189313941474L);
		for (int i = 0; i < 30; i++) {
			long value = randomGenerator.nextInt(1000);
			MutableLong mutableLong1 = new MutableLong(value);
			MutableLong mutableLong2 = new MutableLong(value);
			assertEquals(mutableLong1.hashCode(), mutableLong2.hashCode());
		}

	}

	@Test
	@UnitTestMethod(target = MutableLong.class, name = "toString", args = {})
	public void testToString() {
		MutableLong mutableLong = new MutableLong(2);
		String expectedValue = "MutableLong [value=2]";
		String actualValue = mutableLong.toString();
		assertEquals(expectedValue, actualValue);

		mutableLong = new MutableLong(62);
		expectedValue = "MutableLong [value=62]";
		actualValue = mutableLong.toString();
		assertEquals(expectedValue, actualValue);

	}

}
