package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link BooleanContainer}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = BooleanContainer.class)
public class AT_BooleanContainer {

	/**
	 * Test {@link BooleanContainer#BooleanContainer(boolean)}
	 */
	@Test
	@UnitTestConstructor(args = { boolean.class })
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
	@UnitTestConstructor(args = { boolean.class, int.class })
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
	@UnitTestMethod(name = "get", args = { int.class })
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
	@UnitTestMethod(name = "set", args = { int.class, boolean.class })
	public void testSet() {
		// proxy via testGet()
	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = {int.class}, tags = {UnitTag.INCOMPLETE})
	public void testExpandCapacity() {
		// requires a manual performance test
	}
}
