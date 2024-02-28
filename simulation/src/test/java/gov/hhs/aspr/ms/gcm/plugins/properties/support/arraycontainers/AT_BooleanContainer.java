package gov.hhs.aspr.ms.gcm.plugins.properties.support.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_BooleanContainer {

	private Iterator<Integer> getEmptyIndexIterator() {
		return Collections.emptyIterator();
	}

	@Test
	@UnitTestConstructor(target = BooleanContainer.class, args = { boolean.class, Supplier.class })
	public void testBooleanContainer() {
		BooleanContainer booleanContainer = new BooleanContainer(true, this::getEmptyIndexIterator);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(false, this::getEmptyIndexIterator);

		for (int i = 0; i < 10; i++) {
			assertFalse(booleanContainer.get(i));
		}

	}

	@Test
	@UnitTestMethod(target = BooleanContainer.class, name = "get", args = { int.class })
	public void testGet() {
		Random random = new Random(53463457457456456L);
		int n = 1000;
		boolean[] array = new boolean[n];
		for (int i = 0; i < n; i++) {
			array[i] = random.nextBoolean();
		}

		BooleanContainer booleanContainer = new BooleanContainer(true, this::getEmptyIndexIterator);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(true, this::getEmptyIndexIterator);

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
	@UnitTestMethod(target = BooleanContainer.class, name = "expandCapacity", args = { int.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testExpandCapacity() {
		// requires a manual performance test
	}

	@Test
	@UnitTestMethod(target = BooleanContainer.class, name = "toString", args = {})
	public void testToString() {

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);
		list.add(5);
		list.add(6);
		list.add(7);

		BooleanContainer booleanContainer = new BooleanContainer(false, () -> list.iterator());
		booleanContainer.set(5, true);
		booleanContainer.set(7, true);
		booleanContainer.set(1, true);
		booleanContainer.set(8, true);
		String actualValue = booleanContainer.toString();

		String expectedValue = "BooleanContainer [defaultValue=false, bitSet=[1=true, 2=false, 5=true, 6=false, 7=true]]";
		assertEquals(expectedValue, actualValue);

	}
}
