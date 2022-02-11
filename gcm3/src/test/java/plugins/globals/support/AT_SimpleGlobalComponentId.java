package plugins.globals.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = SimpleGlobalComponentId.class)
public class AT_SimpleGlobalComponentId {

	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleGlobalComponentId(5));

		assertThrows(RuntimeException.class, () -> new SimpleGlobalComponentId(null));
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleGlobalComponentId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleGlobalComponentId(5).toString());
		assertEquals("table", new SimpleGlobalComponentId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleGlobalComponentId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testEquals() {
		SimpleGlobalComponentId id_1 = new SimpleGlobalComponentId(2);
		SimpleGlobalComponentId id_2 = new SimpleGlobalComponentId(5);
		SimpleGlobalComponentId id_3 = new SimpleGlobalComponentId(2);
		SimpleGlobalComponentId id_4 = new SimpleGlobalComponentId("A");
		SimpleGlobalComponentId id_5 = new SimpleGlobalComponentId("A");
		SimpleGlobalComponentId id_6 = new SimpleGlobalComponentId("B");

		assertEquals(id_1, id_1);
		assertNotEquals(id_1, id_2);
		assertEquals(id_1, id_3);
		assertNotEquals(id_1, id_4);
		assertNotEquals(id_1, id_5);
		assertNotEquals(id_1, id_6);

		assertNotEquals(id_2, id_1);
		assertEquals(id_2, id_2);
		assertNotEquals(id_2, id_3);
		assertNotEquals(id_2, id_4);
		assertNotEquals(id_2, id_5);
		assertNotEquals(id_2, id_6);

		assertNotEquals(id_2, id_1);
		assertEquals(id_2, id_2);
		assertNotEquals(id_2, id_3);
		assertNotEquals(id_2, id_4);
		assertNotEquals(id_2, id_5);
		assertNotEquals(id_2, id_6);

		assertEquals(id_3, id_1);
		assertNotEquals(id_3, id_2);
		assertEquals(id_3, id_3);
		assertNotEquals(id_3, id_4);
		assertNotEquals(id_3, id_5);
		assertNotEquals(id_3, id_6);

		assertNotEquals(id_4, id_1);
		assertNotEquals(id_4, id_2);
		assertNotEquals(id_4, id_3);
		assertEquals(id_4, id_4);
		assertEquals(id_4, id_5);
		assertNotEquals(id_4, id_6);

		assertNotEquals(id_5, id_1);
		assertNotEquals(id_5, id_2);
		assertNotEquals(id_5, id_3);
		assertEquals(id_5, id_4);
		assertEquals(id_5, id_5);
		assertNotEquals(id_5, id_6);

		assertNotEquals(id_6, id_1);
		assertNotEquals(id_6, id_2);
		assertNotEquals(id_6, id_3);
		assertNotEquals(id_6, id_4);
		assertNotEquals(id_6, id_5);
		assertEquals(id_6, id_6);

		assertNotEquals(id_1, null);
		assertNotEquals(id_2, null);
		assertNotEquals(id_3, null);
		assertNotEquals(id_4, null);
		assertNotEquals(id_5, null);
		assertNotEquals(id_6, null);

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleGlobalComponentId s1 = new SimpleGlobalComponentId(i);
			SimpleGlobalComponentId s2 = new SimpleGlobalComponentId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}
		
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleGlobalComponentId(i).hashCode());
			assertTrue(unique);
		}

	}

}
