package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = IdentifiableFunction.class)
public class AT_IdentifiableFunction {

	@Test
	@UnitTestConstructor(args = { Object.class, Function.class })
	public void testConstructor() {
		for (int i = 0; i < 30; i++) {
			int input = i;
			String expectedValue = Integer.toString(input);
			IdentifiableFunction<Integer> f = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
			Object actualValue = f.getFunction().apply(input);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		// identifiable functions are equal if and only if their internal id
		// values are equal
		IdentifiableFunction<Integer> a1 = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
		IdentifiableFunction<Integer> a2 = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
		IdentifiableFunction<Integer> a3 = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));

		IdentifiableFunction<Integer> b = new IdentifiableFunction<>("B", (n) -> Integer.toString(n));

		// reflexive
		assertEquals(a1, a1);

		// symmetric
		assertEquals(a1, a2);
		assertEquals(a2, a1);

		// transitive
		assertEquals(a2, a3);
		assertEquals(a1, a3);

		// non-equal ids

		assertNotEquals(a1, b);
		assertNotEquals(a2, b);
		assertNotEquals(a3, b);

	}

	@Test
	@UnitTestMethod(name = "getFunction", args = {})
	public void testGetFunction() {
		/*
		 * Show that the event function is retrievable by executing that
		 * function against some input
		 */

		for (int i = 0; i < 30; i++) {
			int input = i;
			String expectedValue = Integer.toString(input);
			IdentifiableFunction<Integer> f = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
			Object actualValue = f.getFunction().apply(input);
			assertEquals(expectedValue, actualValue);
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		IdentifiableFunction<Integer> a1 = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));
		IdentifiableFunction<Integer> a2 = new IdentifiableFunction<>("A", (n) -> Integer.toString(n));

		IdentifiableFunction<Integer> b1 = new IdentifiableFunction<>("B", (n) -> Integer.toString(n));
		IdentifiableFunction<Integer> b2 = new IdentifiableFunction<>("B", (n) -> Integer.toString(n));

		// show equal objects have equal hash codes
		assertEquals(a1.hashCode(), a2.hashCode());
		assertEquals(b1.hashCode(), b2.hashCode());

	}

}
