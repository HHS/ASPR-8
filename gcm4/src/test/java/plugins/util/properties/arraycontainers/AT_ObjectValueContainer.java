package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_ObjectValueContainer {

	/**
	 * Tests {@link ObjectValueContainer#ObjectValueContainer(Object, int)}
	 */
	@Test
	@UnitTestConstructor(target = ObjectValueContainer.class,args = { Object.class, int.class })
	public void testConstructor() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
		assertNotNull(objectValueContainer);

		objectValueContainer = new ObjectValueContainer(null, 20);
		assertNotNull(objectValueContainer);

		// pre-condition tests
		assertThrows(IllegalArgumentException.class, () -> new ObjectValueContainer(null, -4));

	}

	/**
	 * Tests {@link ObjectValueContainer#setValue(int, Object)}
	 */
	@Test
	@UnitTestMethod(target = ObjectValueContainer.class,name = "setValue", args = { int.class, Object.class })
	public void testSetValue() {
		String defaultValue = "default";
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
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
		ObjectValueContainer objectValueContainer = new ObjectValueContainer(defaultValue, 20);
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

}
