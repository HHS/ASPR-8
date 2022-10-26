package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = MutableBoolean.class)
public class AT_MutableBoolean {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MutableBoolean mutableBoolean = new MutableBoolean();
		assertEquals(false, mutableBoolean.getValue());
	}

	@Test
	@UnitTestConstructor(args = { boolean.class })
	public void testConstructor_Double() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8591378582633844530L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableBoolean mutableBoolean = new MutableBoolean(value);
			assertEquals(value, mutableBoolean.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2338302122366053723L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableBoolean mutableBoolean = new MutableBoolean(value);
			assertEquals(value, mutableBoolean.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "setValue", args = { boolean.class })
	public void testSetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7814242037959487534L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableBoolean mutableBoolean = new MutableBoolean(value);
			boolean value2 = randomGenerator.nextBoolean();
			mutableBoolean.setValue(value2);
			assertEquals(value2, mutableBoolean.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4984973082700728337L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableBoolean mutableBoolean1 = new MutableBoolean(value);
			MutableBoolean mutableBoolean2 = new MutableBoolean(value);
			assertEquals(mutableBoolean1.getValue(), mutableBoolean2.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		// show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5490448525490135469L);
		for (int i = 0; i < 30; i++) {
			boolean value = randomGenerator.nextBoolean();
			MutableBoolean mutableBoolean1 = new MutableBoolean(value);
			MutableBoolean mutableBoolean2 = new MutableBoolean(value);
			assertEquals(mutableBoolean1.hashCode(), mutableBoolean2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MutableBoolean mutableBoolean = new MutableBoolean(true);
		String expectedValue = "MutableBoolean [value=true]";
		String actualValue = mutableBoolean.toString();
		assertEquals(expectedValue, actualValue);

		mutableBoolean = new MutableBoolean(false);
		expectedValue = "MutableBoolean [value=false]";
		actualValue = mutableBoolean.toString();
		assertEquals(expectedValue, actualValue);
	}

}
