package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.arraycontainers.ArrayIntSet;
import util.arraycontainers.IdValued;

/**
 * Test class for {@link ArrayIntSet}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ArrayIntSet.class)
public class AT_ArrayIntSet {

	private static Set<IdValued> getIdValueds(Integer... values) {
		Set<IdValued> result = new LinkedHashSet<>();
		for (Integer value : values) {
			result.add(new IdValued(value));
		}
		return result;
	}

	/**
	 * Test for {@link ArrayIntSet#add(T t)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { IdValued.class })
	public void testAdd() {

		Set<IdValued> IdValueds = getIdValueds(45, 18, 23, 66);

		/*
		 * Test an IntSet that tolerates duplicates
		 */
		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>(100, true);
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		intSet.add(new IdValued(45));
		assertEquals(5, intSet.size());
		assertEquals(IdValueds, new LinkedHashSet<>(intSet.getValues()));

		/*
		 * Test an IntSet that does not tolerate duplicates
		 */
		intSet = new ArrayIntSet<>(100, false);
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		intSet.add(new IdValued(45));
		assertEquals(4, intSet.size());
		assertEquals(IdValueds, new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#remove(T t)}
	 */
	@Test
	@UnitTestMethod(name = "remove", args = { IdValued.class })
	public void testRemove() {
		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>();
		intSet.add(new IdValued(300));
		intSet.add(new IdValued(-67));
		intSet.add(new IdValued(-4));
		intSet.add(new IdValued(687));
		intSet.add(new IdValued(213));
		assertEquals(getIdValueds(300, -67, -4, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		// nothing should change since 100 is not contained
		intSet.remove(new IdValued(100));
		assertEquals(getIdValueds(300, -67, -4, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new IdValued(-4));
		assertEquals(getIdValueds(300, -67, 687, 213), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new IdValued(213));
		assertEquals(getIdValueds(300, -67, 687), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new IdValued(300));
		assertEquals(getIdValueds(-67, 687), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new IdValued(687));
		assertEquals(getIdValueds(-67), new LinkedHashSet<>(intSet.getValues()));

		intSet.remove(new IdValued(-67));
		assertEquals(getIdValueds(), new LinkedHashSet<>(intSet.getValues()));

		intSet = new ArrayIntSet<>(10);
		for (int i = 0; i < 100; i++) {
			intSet.add(new IdValued(8 * i));
		}
		for (int i = 0; i < 800; i++) {
			intSet.remove(new IdValued(i));
		}

		intSet = new ArrayIntSet<>(5);

		// force the array int set to attempt a remove when empty
		intSet.remove(new IdValued(0));

		// force the array int set to shrink
		for (int i = 0; i < 1000; i++) {
			intSet.add(new IdValued(i));
		}
		for (int i = 0; i < 1000; i++) {
			intSet.remove(new IdValued(i));
		}
		assertEquals(getIdValueds(), new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#getValues()}
	 */
	@Test
	@UnitTestMethod(name = "getValues", args = {})
	public void testGetValues() {

		// Select 500 random values from 0..999
		Random random = new Random(3453763452345345L);
		List<IdValued> IdValueds = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			IdValueds.add(new IdValued(i));
		}
		Collections.shuffle(IdValueds, random);
		List<IdValued> selectedIdValueds = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			selectedIdValueds.add(IdValueds.get(i));
		}

		// Add the selected values to an IntSet
		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>();
		for (IdValued selectedValue : selectedIdValueds) {

			intSet.add(selectedValue);
		}

		assertEquals(new LinkedHashSet<>(selectedIdValueds), new LinkedHashSet<>(intSet.getValues()));

	}

	/**
	 * Test for {@link ArrayIntSet#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {

		Random random = new Random(3453763452345345L);
		List<IdValued> IdValueds = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			IdValueds.add(new IdValued(i));
		}
		Collections.shuffle(IdValueds, random);

		// first test that when there are no duplicates that the size of the
		// IntSet matches the number of values.
		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>();
		for (IdValued value : IdValueds) {
			intSet.add(value);
		}
		assertEquals(IdValueds.size(), intSet.size());

		// now test that when there are duplicates that the size of the
		// IntSet matches the number of entries
		intSet = new ArrayIntSet<>();
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		assertEquals(IdValueds.size() * 2, intSet.size());

		// finally test that when there are duplicates, but the IntSet does not
		// tolerate duplicates, that the size of the
		// IntSet matches the number of values
		intSet = new ArrayIntSet<>(false);
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		assertEquals(IdValueds.size(), intSet.size());

	}

	/**
	 * Test for {@link ArrayIntSet#ArrayIntSet()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor_Empty() {
		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>();
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(false);
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(100);
		assertEquals(0, intSet.size());

		intSet = new ArrayIntSet<>(100, true);
		assertEquals(0, intSet.size());

		/*
		 * Test pre-conditions
		 */
		assertThrows(Exception.class, () -> new ArrayIntSet<>(0));
		assertThrows(Exception.class, () -> new ArrayIntSet<>(0, true));
	}

	/**
	 * Test for {@link ArrayIntSet#ArrayIntSet(boolean)}
	 */
	@Test
	@UnitTestConstructor(args = { boolean.class })
	public void testConstructor_Boolean() {
		// covered by empty constructor test
	}

	/**
	 * Test for {@link ArrayIntSet#ArrayIntSet(float)}
	 */
	@Test
	@UnitTestConstructor(args = { float.class })
	public void testConstructor_Float() {
		// covered by empty constructor test }
	}

	/**
	 * Test for {@link ArrayIntSet#ArrayIntSet(float, boolean)}
	 */
	@Test
	@UnitTestConstructor(args = { float.class, boolean.class })
	public void testConstructor_FloatBoolean() {
		// covered by empty constructor test }
	}

	/**
	 * Test for {@link ArrayIntSet#contains(T t)}
	 */
	@Test
	@UnitTestMethod(name = "contains", args = { IdValued.class })
	public void testContains() {

		Set<IdValued> IdValueds = getIdValueds(1, 4, 5, 7, 12, 14, 16, 17, 22, 23, 28);

		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>(100, true);
		for (IdValued IdValued : IdValueds) {
			intSet.add(IdValued);
		}
		for (int i = 0; i < 30; i++) {
			IdValued IdValued = new IdValued(i);
			assertEquals(intSet.contains(IdValued), IdValueds.contains(IdValued));
		}

	}

	/**
	 * Test for {@link ArrayIntSet#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		ArrayIntSet<IdValued> intSet = new ArrayIntSet<>();

		Set<Integer> expectedContents = new LinkedHashSet<>();
		expectedContents.add(45);
		expectedContents.add(37);
		expectedContents.add(66);
		expectedContents.add(99);
		expectedContents.add(32);
		expectedContents.add(14);

		for (Integer value : expectedContents) {
			intSet.add(new IdValued(value));
		}
		String stringRepresentaion = intSet.toString();
		String commaSeparatedNumbers = stringRepresentaion.substring(7, stringRepresentaion.length() - 1);
		String[] strings = commaSeparatedNumbers.split(",");
		Set<Integer> actualContents = new LinkedHashSet<>();
		for (String string : strings) {
			int value = Integer.parseInt(string.trim());
			actualContents.add(value);
		}
		assertEquals(expectedContents, actualContents);

	}

}
