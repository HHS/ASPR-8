package gov.hhs.aspr.ms.gcm.plugins.properties.support.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_FloatValueContainer {
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	
	/**
	 * Tests {@link FloatValueContainer#FloatValueContainer(float)}
	 */
	@Test
	@UnitTestConstructor(target = FloatValueContainer.class, args = { float.class, Supplier.class })
	public void testConstructor_Float() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::getEmptyIndexIterator);
		assertNotNull(floatValueContainer);
	}

	/**
	 * Tests {@link FloatValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "getCapacity", args = {})
	public void testGetCapacity() {
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::getEmptyIndexIterator);

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
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = -10;
		floatValueContainer = new FloatValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

		defaultValue = 10;
		floatValueContainer = new FloatValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, floatValueContainer.getDefaultValue(), 0);

	}

	/**
	 * Test {@link FloatValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		float defaultValue = -345.34f;
		FloatValueContainer floatValueContainer = new FloatValueContainer(defaultValue,this::getEmptyIndexIterator);
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
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::getEmptyIndexIterator);

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
		FloatValueContainer floatValueContainer = new FloatValueContainer(0,this::getEmptyIndexIterator);

		// long value
		float value = 12123.234f;
		floatValueContainer.setValue(0, value);
		assertEquals(value, floatValueContainer.getValue(0), 0);

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> floatValueContainer.setValue(-1, 234.63f));

	}

	@Test
	@UnitTestMethod(target = FloatValueContainer.class, name = "toString", args = {})
	public void testToString() {

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(5);
		list.add(6);
		list.add(7);

		FloatValueContainer floatValueContainer = new FloatValueContainer(0.0F, () -> list.iterator());
		floatValueContainer.setValue(5, 2.5F);
		floatValueContainer.setValue(7, 3.5F);
		floatValueContainer.setValue(1, 0.5F);
		floatValueContainer.setValue(8, 4.0F);
		String actualValue = floatValueContainer.toString();

		String expectedValue = "FloatValueContainer [values=[1=0.5, 2=0.0, 5=2.5, 6=0.0, 7=3.5], defaultValue=0.0]";
		assertEquals(expectedValue, actualValue);

	}
	
}
