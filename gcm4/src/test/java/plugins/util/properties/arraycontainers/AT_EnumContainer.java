package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link EnumContainer}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = EnumContainer.class)
public class AT_EnumContainer {
	public enum Animal {
		CAT, PIG, SHEEP, DOG, HORSE;
	}

	/**
	 * Tests {@link EnumContainer#EnumContainer(Class, Object)}
	 */
	@Test
	@UnitTestConstructor(args = { Class.class, Object.class })
	public void testConstructor_ClassObject() {
		assertNotNull(new EnumContainer(Animal.class, Animal.DOG));

		// Test preconditions

		// if the class is null
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(null, Animal.DOG));

		// if the class is not an enumeration
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Integer.class, Animal.DOG));

		// if the default is not a member of the enum
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Animal.class, 234));

	}

	/**
	 * Tests {@link EnumContainer#EnumContainer(Class, Object, int)}
	 */
	@Test
	@UnitTestConstructor(args = { Class.class, Object.class, int.class })
	public void testConstructor_ClassObjectInt() {
		assertNotNull(new EnumContainer(Animal.class, Animal.DOG));

		// Test preconditions

		// if the class is null
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(null, Animal.DOG, 100));

		// if the class is not an enumeration
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Integer.class, Animal.DOG, 100));

		// if the default is not a member of the enum
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Animal.class, 234, 100));

		// if the capacity is negative
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Animal.class, Animal.DOG, -1));

	}

	/**
	 * Tests {@link EnumContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = { int.class })
	public void testGetValue() {
		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG);
		enumContainer.setValue(3, Animal.CAT);
		enumContainer.setValue(5, Animal.CAT);
		enumContainer.setValue(2, Animal.SHEEP);
		enumContainer.setValue(0, Animal.HORSE);
		enumContainer.setValue(2, Animal.PIG);

		assertEquals(Animal.HORSE, enumContainer.getValue(0));
		assertEquals(Animal.DOG, enumContainer.getValue(1));
		assertEquals(Animal.PIG, enumContainer.getValue(2));
		assertEquals(Animal.CAT, enumContainer.getValue(3));
		assertEquals(Animal.DOG, enumContainer.getValue(4));
		assertEquals(Animal.CAT, enumContainer.getValue(5));
		assertEquals(Animal.DOG, enumContainer.getValue(6));

		enumContainer = new EnumContainer(Animal.class, Animal.DOG, 100);
		enumContainer.setValue(3, Animal.CAT);
		enumContainer.setValue(5, Animal.CAT);
		enumContainer.setValue(2, Animal.SHEEP);
		enumContainer.setValue(0, Animal.HORSE);
		enumContainer.setValue(2, Animal.PIG);

		assertEquals(Animal.HORSE, enumContainer.getValue(0));
		assertEquals(Animal.DOG, enumContainer.getValue(1));
		assertEquals(Animal.PIG, enumContainer.getValue(2));
		assertEquals(Animal.CAT, enumContainer.getValue(3));
		assertEquals(Animal.DOG, enumContainer.getValue(4));
		assertEquals(Animal.CAT, enumContainer.getValue(5));
		assertEquals(Animal.DOG, enumContainer.getValue(6));

		// Test pre-conditions

		EnumContainer preConditionEnumContainer = enumContainer;
		// if the index is negative
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> preConditionEnumContainer.getValue(-1));

	}

	/**
	 * Test {@link EnumContainer#setValue(int, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setValue", args = { int.class, Object.class })
	public void testSetValue() {

		Map<Integer, Animal> animalMap = new LinkedHashMap<>();

		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG);
		Random random = new Random(4545456567994423L);
		for (int i = 0; i < 1000; i++) {
			int index = random.nextInt(6);
			int ord = random.nextInt(Animal.values().length);
			Animal animal = Animal.values()[ord];
			animalMap.put(index, animal);
			enumContainer.setValue(index, animal);
			assertEquals(animal, enumContainer.getValue(index));
		}

		enumContainer = new EnumContainer(Animal.class, Animal.DOG, 100);
		for (int i = 0; i < 1000; i++) {
			int index = random.nextInt(6);
			int ord = random.nextInt(Animal.values().length);
			Animal animal = Animal.values()[ord];
			animalMap.put(index, animal);
			enumContainer.setValue(index, animal);
			assertEquals(animal, enumContainer.getValue(index));
		}

		// Test pre-conditions
		EnumContainer preConditionEnumContainer = enumContainer;
		// if the index is negative
		assertThrows(IllegalArgumentException.class, () -> preConditionEnumContainer.setValue(-1, Animal.HORSE));

		// if the value is null
		assertThrows(IllegalArgumentException.class, () -> preConditionEnumContainer.setValue(1, null));

		// if the value is not a member of the enumeration
		assertThrows(IllegalArgumentException.class, () -> preConditionEnumContainer.setValue(1, 45));
	}

	@Test
	@UnitTestMethod(name = "getCapacity", args = {}, tags = {UnitTag.INCOMPLETE})
	public void testGetCapacity() {
		// requires a manual performance test
	}

	@Test
	@UnitTestMethod(name = "setCapacity", args = {int.class}, tags = {UnitTag.INCOMPLETE})
	public void testSetCapacity() {
		// requires a manual performance test
	}

}
