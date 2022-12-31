package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class MT_MutableObject {
	
	@Test
	@UnitTestConstructor(target = MutableObject.class,args = {})
	public void testConstructor() {
		MutableObject<String> mutableObject = new MutableObject<>();
		assertNull(mutableObject.getValue());
	}

	@Test
	@UnitTestConstructor(target = MutableObject.class,args = { Object.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3527814400767115920L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableObject<Boolean> mutableObject = new MutableObject<>(value);
			assertEquals(value, mutableObject.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableObject.class,name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1262778239913204399L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(10000);
			MutableObject<Integer> mutableObject = new MutableObject<>(value);
			assertEquals(value, mutableObject.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableObject.class,name = "setValue", args = { Object.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3426888451617179735L);
		for (int i = 0; i < 30; i++) {
			double value = randomGenerator.nextDouble();
			MutableObject<Double> mutableObject = new MutableObject<>(value);
			double value2 = randomGenerator.nextDouble();
			mutableObject.setValue(value2);
			assertEquals(value2, mutableObject.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableObject.class,name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8850768319450625485L);
		for (int i = 0; i < 30; i++) {
			int value = randomGenerator.nextInt(1000);
			MutableObject<Integer> mutableObject1 = new MutableObject<>(value);
			MutableObject<Integer> mutableObject2 = new MutableObject<>(value);
			assertEquals(mutableObject1.getValue(), mutableObject2.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = MutableObject.class,name = "hashCode", args = {})
	public void testHashCode() {
		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4349805918351369003L);
		for (int i = 0; i < 30; i++) {
			String value = Double.toString(randomGenerator.nextDouble());
			MutableObject<String> mutableObject1 = new MutableObject<>(value);
			MutableObject<String> mutableObject2 = new MutableObject<>(value);
			assertEquals(mutableObject1.hashCode(), mutableObject2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(target = MutableObject.class,name = "toString", args = {})
	public void testToString() {
		MutableObject<String> mutableObject = new MutableObject<>("test value 1");
		String expectedValue = "MutableObject [value=test value 1]";
		String actualValue = mutableObject.toString();
		assertEquals(expectedValue, actualValue);

		mutableObject = new MutableObject<>("value2");
		expectedValue = "MutableObject [value=value2]";
		actualValue = mutableObject.toString();
		assertEquals(expectedValue, actualValue);
	}

}
