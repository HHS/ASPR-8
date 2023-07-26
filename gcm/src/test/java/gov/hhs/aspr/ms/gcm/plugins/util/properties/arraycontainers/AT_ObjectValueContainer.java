package gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_ObjectValueContainer {
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	/**
	 * Tests {@link ObjectValueContainer#ObjectValueContainer(Object, int)}
	 */
	@Test
	@UnitTestConstructor(target = ObjectValueContainer.class,args = { Object.class, Supplier.class })
	public void testConstructor() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, this::getEmptyIndexIterator);
		assertNotNull(objectValueContainer);

		objectValueContainer = new ObjectValueContainer(null, this::getEmptyIndexIterator);
		assertNotNull(objectValueContainer);


	}

	/**
	 * Tests {@link ObjectValueContainer#setValue(int, Object)}
	 */
	@Test
	@UnitTestMethod(target = ObjectValueContainer.class,name = "setValue", args = { int.class, Object.class })
	public void testSetValue() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue,this::getEmptyIndexIterator);
		objectValueContainer.setValue(3, "dog");
		objectValueContainer.setValue(1, "cat");
		objectValueContainer.setValue(4, "pig");
		objectValueContainer.setValue(7, "cow");
		objectValueContainer.setValue(3, "bat");
		objectValueContainer.setValue(5, null);

		assertEquals(defaultValue, objectValueContainer.getValue(0));
		assertEquals("cat", objectValueContainer.getValue(1));
		assertEquals(defaultValue, objectValueContainer.getValue(2));
		assertEquals("bat", objectValueContainer.getValue(3));
		assertEquals("pig", objectValueContainer.getValue(4));
		assertNull(objectValueContainer.getValue(5));
		assertEquals(defaultValue, objectValueContainer.getValue(6));
		assertEquals("cow", objectValueContainer.getValue(7));
		assertEquals(defaultValue, objectValueContainer.getValue(8));
		assertEquals(defaultValue, objectValueContainer.getValue(9));

		// test pre-conditions
		assertThrows(IllegalArgumentException.class, () -> objectValueContainer.setValue(-1, "frog"));
	}

	/**
	 * Tests {@link ObjectValueContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(target = ObjectValueContainer.class,name = "getValue", args = { int.class })
	public void testGetValue() {

		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, this::getEmptyIndexIterator);
		objectValueContainer.setValue(3, "dog");
		objectValueContainer.setValue(1, "cat");
		objectValueContainer.setValue(4, "pig");
		objectValueContainer.setValue(7, "cow");
		objectValueContainer.setValue(3, "bat");
		objectValueContainer.setValue(5, null);

		assertEquals(defaultValue, objectValueContainer.getValue(0));
		assertEquals("cat", objectValueContainer.getValue(1));
		assertEquals(defaultValue, objectValueContainer.getValue(2));
		assertEquals("bat", objectValueContainer.getValue(3));
		assertEquals("pig", objectValueContainer.getValue(4));
		assertNull(objectValueContainer.getValue(5));
		assertEquals(defaultValue, objectValueContainer.getValue(6));
		assertEquals("cow", objectValueContainer.getValue(7));
		assertEquals(defaultValue, objectValueContainer.getValue(8));
		assertEquals(defaultValue, objectValueContainer.getValue(9));

		// test pre-conditions
		assertThrows(IllegalArgumentException.class, () -> objectValueContainer.getValue(-1));
	}

	@Test
	@UnitTestMethod(target = ObjectValueContainer.class,name = "setCapacity", args = {int.class}, tags = {UnitTag.INCOMPLETE})
	public void testSetCapacity() {
		// requires a manual performance test
	}

	@Test
	@UnitTestMethod(target = ObjectValueContainer.class,name = "getCapacity", args = {}, tags = {UnitTag.INCOMPLETE})
	public void testGetCapacity() {
		// requires a manual performance test
	}
	
	@Test
	@UnitTestMethod(target = ObjectValueContainer.class, name = "toString", args = {})
	public void testToString() {

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(5);
		list.add(6);
		list.add(7);

		ObjectValueContainer objectValueContainer = new ObjectValueContainer(0, () -> list.iterator());
		objectValueContainer.setValue(5, 2);
		objectValueContainer.setValue(7, "A");
		objectValueContainer.setValue(1, 3.8);
		objectValueContainer.setValue(8, "B");
		String actualValue = objectValueContainer.toString();
		

		String expectedValue = "ObjectValueContainer [elements=[1=3.8, 2=0, 5=2, 6=0, 7=A], defaultValue=0]";
		assertEquals(expectedValue, actualValue);
	}

}
