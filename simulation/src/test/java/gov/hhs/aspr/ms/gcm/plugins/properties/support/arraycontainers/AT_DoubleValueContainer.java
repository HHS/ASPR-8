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

public class AT_DoubleValueContainer {

	/**
	 * Tests {@link DoubleValueContainer#DoubleValueContainer(double)}
	 */
	@Test
	@UnitTestConstructor(target = DoubleValueContainer.class, args = { double.class, Supplier.class })
	public void testConstructor_Double() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0,this::getEmptyIndexIterator);
		assertNotNull(doubleValueContainer);
	}
	
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	
	

	/**
	 * Tests {@link DoubleValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "getCapacity", args = {})
	public void testGetCapacity() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0,this::getEmptyIndexIterator);

		assertTrue(doubleValueContainer.getCapacity() >= 0);

		doubleValueContainer.setValue(1, 123.4);
		assertTrue(doubleValueContainer.getCapacity() >= 1);

		doubleValueContainer.setValue(34, 36.4);
		assertTrue(doubleValueContainer.getCapacity() >= 34);

		doubleValueContainer.setValue(10, 15.4);
		assertTrue(doubleValueContainer.getCapacity() >= 20);

		doubleValueContainer.setValue(137, 25.26);
		assertTrue(doubleValueContainer.getCapacity() >= 137);

		doubleValueContainer.setValue(1000, 123.6345);
		assertTrue(doubleValueContainer.getCapacity() >= 1000);
	}

	/**
	 * Tests {@link DoubleValueContainer#getDefaultValue()}
	 */
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		double defaultValue = 0;
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(), 0);

		defaultValue = -10;
		doubleValueContainer = new DoubleValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(), 0);

		defaultValue = 10;
		doubleValueContainer = new DoubleValueContainer(defaultValue,this::getEmptyIndexIterator);
		assertEquals(defaultValue, doubleValueContainer.getDefaultValue(), 0);

	}

	/**
	 * Tests {@link DoubleValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		double defaultValue = -345.34;
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(defaultValue,this::getEmptyIndexIterator);
		int highIndex = 1000;
		double delta = 2.3452346;

		double[] doubles = new double[highIndex];
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = delta + i;
		}
		for (int i = 0; i < doubles.length; i++) {
			doubleValueContainer.setValue(i, doubles[i]);
		}

		for (int i = 0; i < doubles.length; i++) {
			assertEquals(doubles[i], doubleValueContainer.getValue(i), 0);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(doubleValueContainer.getValue(i + highIndex), defaultValue, 0);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> doubleValueContainer.getValue(-1));

	}

	/**
	 * Tests {@link DoubleValueContainer#setCapacity(int)}
	 */
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "setCapacity", args = { int.class })
	public void testSetCapacity() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0,this::getEmptyIndexIterator);

		int expectedCapacity = 5;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		doubleValueContainer.setCapacity(expectedCapacity);
		assertTrue(doubleValueContainer.getCapacity() >= expectedCapacity);
	}

	/**
	 * Tests {@link DoubleValueContainer#setValue(int, double)}
	 */
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "setValue", args = { int.class, double.class })
	public void testSetValue() {
		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0,this::getEmptyIndexIterator);

		// long value
		double value = 12123.234;
		doubleValueContainer.setValue(0, value);
		assertEquals(value, doubleValueContainer.getValue(0), 0);

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> doubleValueContainer.setValue(-1, 234.63));

	}
	
	@Test
	@UnitTestMethod(target = DoubleValueContainer.class, name = "toString", args = {})
	public void testToString() {

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(5);
		list.add(6);
		list.add(7);

		DoubleValueContainer doubleValueContainer = new DoubleValueContainer(0.0, () -> list.iterator());
		doubleValueContainer.setValue(5, 2.5);
		doubleValueContainer.setValue(7, 3.5);
		doubleValueContainer.setValue(1, 0.5);
		doubleValueContainer.setValue(8, 4);
		String actualValue = doubleValueContainer.toString();
				

		String expectedValue = "DoubleValueContainer [values=[1=0.5, 2=0.0, 5=2.5, 6=0.0, 7=3.5], defaultValue=0.0]";
		assertEquals(expectedValue, actualValue);

	}

}
