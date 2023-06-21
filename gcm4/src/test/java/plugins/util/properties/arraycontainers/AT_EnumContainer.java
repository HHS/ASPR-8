package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


public class AT_EnumContainer {
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}
	
	public enum Animal {
		CAT, PIG, SHEEP, DOG, HORSE;
	}

	/**
	 * Tests {@link EnumContainer#EnumContainer(Class, Object)}
	 */
	@Test
	@UnitTestConstructor(target = EnumContainer.class, args = { Class.class, Object.class })
	public void testConstructor_ClassObject() {
		assertNotNull(new EnumContainer(Animal.class, Animal.DOG,this::getEmptyIndexIterator));

		// Test preconditions

		// if the class is null
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(null, Animal.DOG,this::getEmptyIndexIterator));

		// if the class is not an enumeration
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Integer.class, Animal.DOG,this::getEmptyIndexIterator));

		// if the default is not a member of the enum
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Animal.class, 234,this::getEmptyIndexIterator));

	}

	/**
	 * Tests {@link EnumContainer#EnumContainer(Class, Object, int)}
	 */
	@Test
	@UnitTestConstructor(target = EnumContainer.class, args = { Class.class, Object.class, int.class })
	public void testConstructor_ClassObjectInt() {
		assertNotNull(new EnumContainer(Animal.class, Animal.DOG,this::getEmptyIndexIterator));

		// Test preconditions

		// if the class is null
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(null, Animal.DOG, this::getEmptyIndexIterator));

		// if the class is not an enumeration
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Integer.class, Animal.DOG,this::getEmptyIndexIterator));

		// if the default is not a member of the enum
		assertThrows(IllegalArgumentException.class, () -> new EnumContainer(Animal.class, 234,this::getEmptyIndexIterator));


	}

	/**
	 * Tests {@link EnumContainer#getValue(int)}
	 */
	@Test
	@UnitTestMethod(target = EnumContainer.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG,this::getEmptyIndexIterator);
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

		enumContainer = new EnumContainer(Animal.class, Animal.DOG, this::getEmptyIndexIterator);
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

	}

	/**
	 * Test {@link EnumContainer#setValue(int, Object)}
	 */
	@Test
	@UnitTestMethod(target = EnumContainer.class, name = "setValue", args = { int.class, Object.class })
	public void testSetValue() {

		Map<Integer, Animal> animalMap = new LinkedHashMap<>();

		EnumContainer enumContainer = new EnumContainer(Animal.class, Animal.DOG,this::getEmptyIndexIterator);
		Random random = new Random(4545456567994423L);
		for (int i = 0; i < 1000; i++) {
			int index = random.nextInt(6);
			int ord = random.nextInt(Animal.values().length);
			Animal animal = Animal.values()[ord];
			animalMap.put(index, animal);
			enumContainer.setValue(index, animal);
			assertEquals(animal, enumContainer.getValue(index));
		}

		enumContainer = new EnumContainer(Animal.class, Animal.DOG, this::getEmptyIndexIterator);
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
	@UnitTestMethod(target = EnumContainer.class, name = "getCapacity", args = {}, tags = { UnitTag.INCOMPLETE })
	public void testGetCapacity() {
		// requires a manual performance test
	}

	@Test
	@UnitTestMethod(target = EnumContainer.class, name = "setCapacity", args = { int.class }, tags = { UnitTag.INCOMPLETE })
	public void testSetCapacity() {
		// requires a manual performance test
	}

}
