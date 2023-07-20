package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_BooleanContainer {
	
	
	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();				
	}

	/**
	 * Test {@link BooleanContainer#BooleanContainer(boolean)}
	 */
	@Test
	@UnitTestConstructor(target = BooleanContainer.class, args = { boolean.class, Supplier.class })
	public void testConstructor_Boolean() {
		BooleanContainer booleanContainer = new BooleanContainer(true,this::getEmptyIndexIterator);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(false,this::getEmptyIndexIterator);

		for (int i = 0; i < 10; i++) {
			assertFalse(booleanContainer.get(i));
		}

	}

	

	/**
	 * Test {@link BooleanContainer#get(int)}
	 */
	@Test
	@UnitTestMethod(target = BooleanContainer.class, name = "get", args = { int.class })
	public void testGet() {
		Random random = new Random(53463457457456456L);
		int n = 1000;
		boolean[] array = new boolean[n];
		for (int i = 0; i < n; i++) {
			array[i] = random.nextBoolean();
		}

		BooleanContainer booleanContainer = new BooleanContainer(true,this::getEmptyIndexIterator);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(true,this::getEmptyIndexIterator);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

	}

	/**
	 * Test {@link BooleanContainer#set(int, boolean)}
	 */
	@Test
	@UnitTestMethod(target = BooleanContainer.class, name = "set", args = { int.class, boolean.class })
	public void testSet() {
		// proxy via testGet()
	}

	@Test
	@UnitTestMethod(target = BooleanContainer.class, name = "expandCapacity", args = { int.class }, tags = { UnitTag.INCOMPLETE })
	public void testExpandCapacity() {
		// requires a manual performance test
	}
}
