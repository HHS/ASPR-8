package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_FloatValueContainer {
	
	
	private boolean validateIndex(int index) {
		return true;
	}

	/**
	 * Tests {@link FloatValueContainer#FloatValueContainer(float, int)}
	 */
	@Test
	@UnitTestConstructor(target = FloatValueContainer.class, args = { float.class, int.class })
	public void testConstructor_FloatInt() {

		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::validateIndex);
		assertNotNull(floatValueContainer);
	}

	/**
	 * Tests {@link FloatValueContainer#FloatValueContainer(float)}
	 */
	@Test
	@UnitTestConstructor(target = FloatValueContainer.class, args = { float.class })
	public void testConstructor_Float() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::validateIndex);
		assertNotNull(floatValueContainer);
	}

	/**
	 * Tests {@link FloatValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "getCapacity", args = {})
	public void testGetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::validateIndex);

		assertTrue(floatValueContainer.getCapacity() >= 0);

		floatValueContainer.setValue(1, 123.4f);
		assertTrue(floatValueContainer.getCapacity() >= 1);

		floatValueContainer.setValue(34, 36.4f);
		assertTrue(floatValueContainer.getCapacity() >= 34);

		floatValueContainer.setValue(10, 15.4f);
		assertTrue(floatValueContainer.getCapacity() >= 10);

		floatValueContainer.setValue(137, 25.26f);
		assertTrue(floatValueContainer.getCapacity() >= 137);

		floatValueContainer.setValue(1000, 123.6345f);
		assertTrue(floatValueContainer.getCapacity() >= 1000);
	}

	/**
	 * Tests {@link FloatValueContainer#getDefaultValue()}
	 */
	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		float defaultValue = 0;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue,this::validateIndex);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = -10;
		floatValueContainer = new FloatValueContainer(defaultValue,this::validateIndex);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = 10;
		floatValueContainer = new FloatValueContainer(defaultValue,this::validateIndex);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

	}

	/**
	 * Test {@link FloatValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		float defaultValue = -345.34f;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue,this::validateIndex);
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
	@UnitTestMethod(target = FloatValueContainer.class, name = "setCapacity", args = { int.class })
	public void testSetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::validateIndex);

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
	@UnitTestMethod(target = FloatValueContainer.class, name = "setValue", args = { int.class, float.class })
	public void testSetValue() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::validateIndex);

		// long value
		float value = 12123.234f;
		floatValueContainer.setValue(0, value);
		assertEquals(value, floatValueContainer.getValue(0), 0);

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> floatValueContainer.setValue(-1, 234.63f));

	}

}
