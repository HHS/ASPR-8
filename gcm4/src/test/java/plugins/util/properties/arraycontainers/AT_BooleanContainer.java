package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link BooleanContainer}
 * 
 * @author Shawn Hatch
 *
 */
public class AT_BooleanContainer {

	/**
	 * Test {@link BooleanContainer#BooleanContainer(boolean)}
	 */
	@Test
	@UnitTestConstructor(target = BooleanContainer.class, args = { boolean.class })
	public void testConstructor_Boolean() {
		BooleanContainer booleanContainer = new BooleanContainer(true);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(false);

		for (int i = 0; i < 10; i++) {
			assertFalse(booleanContainer.get(i));
		}

	}

	/**
	 * Test {@link BooleanContainer#BooleanContainer(boolean, int)}
	 */
	@Test
	@UnitTestConstructor(target = BooleanContainer.class, args = { boolean.class, int.class })
	public void testConstructor_BooleanInt() {

		BooleanContainer booleanContainer = new BooleanContainer(true, 100);

		for (int i = 0; i < 10; i++) {
			assertTrue(booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(false, 100);

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

		BooleanContainer booleanContainer = new BooleanContainer(true);

		for (int i = 0; i < n; i++) {
			booleanContainer.set(i, array[i]);
		}

		for (int i = 0; i < n; i++) {
			assertEquals(array[i], booleanContainer.get(i));
		}

		booleanContainer = new BooleanContainer(true, n);

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
