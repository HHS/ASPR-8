package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = SimpleResolverId.class)
public class AT_SimpleResolverId {

	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertThrows(RuntimeException.class, () -> new SimpleResolverId(null));
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		/*
		 * SimpleResolverIds are equal if and only if their contained values are
		 * equal
		 */

		SimpleResolverId simpleResolverId_1 = new SimpleResolverId("A");
		SimpleResolverId simpleResolverId_2 = new SimpleResolverId("B");
		SimpleResolverId simpleResolverId_3 = new SimpleResolverId("A");

		assertEquals(simpleResolverId_1, simpleResolverId_3);
		assertNotEquals(simpleResolverId_1, simpleResolverId_2);

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * Equal objects have equal hash codes
		 */
		for (int i = 0; i < 20; i++) {
			SimpleResolverId simpleResolverId_1 = new SimpleResolverId(i);
			SimpleResolverId simpleResolverId_2 = new SimpleResolverId(i);
			assertEquals(simpleResolverId_1.hashCode(), simpleResolverId_2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		assertEquals("SimpleResolverId [value=A]",new SimpleResolverId("A").toString());
		assertEquals("SimpleResolverId [value=ASDF]",new SimpleResolverId("ASDF").toString());
		assertEquals("SimpleResolverId [value=12]",new SimpleResolverId(12).toString());
	}
}
