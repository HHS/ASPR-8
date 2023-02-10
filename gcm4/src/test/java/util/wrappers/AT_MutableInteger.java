package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_MutableInteger {
	@Test
	@UnitTestConstructor(target = MutableInteger.class, args = {})
	public void testConstructor() {
		MutableInteger mutableInteger = new MutableInteger();
		assertEquals(0.0, mutableInteger.getValue());
	}

	@Test
	@UnitTestConstructor(target = MutableInteger.class, args = { int.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2425763952527863898L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			assertEquals(value, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "decrement", args = {})
	public void testDecrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2140415989642505862L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			mutableInteger.decrement();
			assertEquals(value - 1, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "decrement", args = { int.class })
	public void testDecrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3822082757204031187L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			int value2 = randomGenerator.nextInt(1000);
			mutableInteger.decrement(value2);
			assertEquals(value - value2, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "increment", args = {})
	public void testIncrement() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4329402035984439237L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			mutableInteger.increment();
			assertEquals(value + 1, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "increment", args = { int.class })
	public void testIncrement_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5369197584271833059L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			int value2 = randomGenerator.nextInt(1000);
			mutableInteger.increment(value2);
			assertEquals(value + value2, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7622823941392079489L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			assertEquals(value, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "setValue", args = { int.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5421290049195575337L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger = new MutableInteger(value);
			int value2 = randomGenerator.nextInt(1000);
			mutableInteger.setValue(value2);
			assertEquals(value2, mutableInteger.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8177269948678825053L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger1 = new MutableInteger(value);
			MutableInteger mutableInteger2 = new MutableInteger(value);
			assertEquals(mutableInteger1.getValue(), mutableInteger2.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "hashCode", args = {})
	public void testHashCode() {

		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6089141682640358001L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableInteger mutableInteger1 = new MutableInteger(value);
			MutableInteger mutableInteger2 = new MutableInteger(value);
			assertEquals(mutableInteger1.hashCode(), mutableInteger2.hashCode());
		}

	}

	@Test
	@UnitTestMethod(target = MutableInteger.class, name = "toString", args = {})
	public void testToString() {
		MutableInteger mutableInteger = new MutableInteger(2);
		String expectedValue = "MutableInteger [value=2]";
		String actualValue = mutableInteger.toString();
		assertEquals(expectedValue, actualValue);

		mutableInteger = new MutableInteger(62);
		expectedValue = "MutableInteger [value=62]";
		actualValue = mutableInteger.toString();
		assertEquals(expectedValue, actualValue);

	}

}
