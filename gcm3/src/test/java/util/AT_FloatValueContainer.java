package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.arraycontainers.FloatValueContainer;

/**
 * Test class for {@link FloatValueContainer}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = FloatValueContainer.class)
public class AT_FloatValueContainer {

	/**
	 * Tests {@link FloatValueContainer#FloatValueContainer(float, int)}
	 */
	@Test
	@UnitTestConstructor(args = { float.class, int.class })
	public void testConstructor_FloatInt() {

		FloatValueContainer floatValueContainer = new FloatValueContainer(0, 1000);
		assertNotNull(floatValueContainer);
		assertTrue(floatValueContainer.getCapacity() >= 1000);

		// pre conditions
		assertThrows(NegativeArraySizeException.class, () -> new FloatValueContainer(0, -1));
	}

	/**
	 * Tests {@link FloatValueContainer#FloatValueContainer(float)}
	 */
	@Test
	@UnitTestConstructor(args = { float.class })
	public void testConstructor_Float() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);
		assertNotNull(floatValueContainer);
	}

	/**
	 * Tests {@link FloatValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(name = "getCapacity", args = {})
	public void testGetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(1, 123.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(34, 36.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(10, 15.4f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(137, 25.26f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());

		floatValueContainer.setValue(1000, 123.6345f);
		assertTrue(floatValueContainer.getCapacity() >= floatValueContainer.size());
	}

	/**
	 * Tests {@link FloatValueContainer#getDefaultValue()}
	 */
	@Test
	@UnitTestMethod(name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		float defaultValue = 0;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = -10;
		floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = 10;
		floatValueContainer = new FloatValueContainer(defaultValue);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

	}

	/**
	 * Test {@link FloatValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = { int.class })
	public void testGetValue() {
		float defaultValue = -345.34f;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue);
		int highIndex = 1000;
		float delta = 2.3452346f;

		float[] floats = new float[highIndex];
		for (int i = 0; i < floats.length; i++) {
			floats[i] = delta + i;
		}
		for (int i = 0; i < floats.length; i++) {
			floatValueContainer.setValue(i, floats[i]);
		}

		for (int i = 0; i < floats.length; i++) {
			assertEquals(floats[i], floatValueContainer.getValue(i), 0);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(floatValueContainer.getValue(i + highIndex), defaultValue, 0);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> floatValueContainer.getValue(-1));

	}

	/**
	 * Tests {@link FloatValueContainer#setCapacity(int)}
	 */
	@Test
	@UnitTestMethod(name = "setCapacity", args = { int.class })
	public void testSetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		int expectedCapacity = 5;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		floatValueContainer.setCapacity(expectedCapacity);
		assertTrue(floatValueContainer.getCapacity() >= expectedCapacity);
	}

	/**
	 * Test {@link FloatValueContainer#setValue(int, float)}
	 */
	@Test
	@UnitTestMethod(name = "setValue", args = { int.class, float.class })
	public void testSetValue() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0);

		// long value
		float value = 12123.234f;
		floatValueContainer.setValue(0, value);
		assertEquals(value, floatValueContainer.getValue(0), 0);

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> floatValueContainer.setValue(-1, 234.63f));

	}

	/**
	 * Tests {@link FloatValueContainer#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0, 100);
		assertEquals(0, floatValueContainer.size());

		floatValueContainer.setValue(3, 352.2345f);
		assertEquals(4, floatValueContainer.size());

		floatValueContainer.setValue(1, 7456.63f);
		assertEquals(4, floatValueContainer.size());

		floatValueContainer.setValue(15, 99.1576f);
		assertEquals(16, floatValueContainer.size());

		floatValueContainer.setValue(300, 247.989762f);
		assertEquals(301, floatValueContainer.size());
	}

}
