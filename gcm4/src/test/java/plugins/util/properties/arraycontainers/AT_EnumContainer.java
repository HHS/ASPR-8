package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

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
	@UnitTestConstructor(target = EnumContainer.class, args = { Class.class, Object.class, Supplier.class})
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
	
	
	@Test
	@UnitTestMethod(target = EnumContainer.class, name = "toString", args = {})
	public void testToString() {

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(5);
		list.add(6);
		list.add(7);

		EnumContainer enumContainer = new EnumContainer(Animal.class,Animal.HORSE, () -> list.iterator());
		enumContainer.setValue(5, Animal.DOG);
		enumContainer.setValue(7, Animal.CAT);
		enumContainer.setValue(1, Animal.PIG);
		enumContainer.setValue(8, Animal.SHEEP);
		String actualValue = enumContainer.toString();

		String expectedValue = "EnumContainer [values=[1=PIG, 2=HORSE, 5=DOG, 6=HORSE, 7=CAT], enumClass=class plugins.util.properties.arraycontainers.AT_EnumContainer$Animal]";
		assertEquals(expectedValue, actualValue);
	}

}
