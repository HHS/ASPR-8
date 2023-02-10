package plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_SimpleGlobalPropertyId {

	@Test
	@UnitTestConstructor(target = SimpleGlobalPropertyId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleGlobalPropertyId(5));

		assertThrows(RuntimeException.class, () -> new SimpleGlobalPropertyId(null));
	}

	@Test
	@UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleGlobalPropertyId equals its
		 * input's toString
		 */

		assertEquals(Integer.toString(5), new SimpleGlobalPropertyId(5).toString());
		assertEquals("table", new SimpleGlobalPropertyId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleGlobalPropertyId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		SimpleGlobalPropertyId id_1 = new SimpleGlobalPropertyId(2);
		SimpleGlobalPropertyId id_2 = new SimpleGlobalPropertyId(5);
		SimpleGlobalPropertyId id_3 = new SimpleGlobalPropertyId(2);
		SimpleGlobalPropertyId id_4 = new SimpleGlobalPropertyId("A");
		SimpleGlobalPropertyId id_5 = new SimpleGlobalPropertyId("A");
		SimpleGlobalPropertyId id_6 = new SimpleGlobalPropertyId("B");
		SimpleGlobalPropertyId id_7 = new SimpleGlobalPropertyId("A");

		assertEquals(id_1, id_1); // testing reflexive property
		assertNotEquals(id_1, id_2);
		assertEquals(id_1, id_3); // part of reflective property test
		assertNotEquals(id_1, id_4);
		assertNotEquals(id_1, id_5);
		assertNotEquals(id_1, id_6);

		assertNotEquals(id_2, id_1);
		assertEquals(id_2, id_2);
		assertNotEquals(id_2, id_3);
		assertNotEquals(id_2, id_4);
		assertNotEquals(id_2, id_5);
		assertNotEquals(id_2, id_6);

		assertEquals(id_3, id_1); // part of reflective property test
		assertNotEquals(id_3, id_2);
		assertEquals(id_3, id_3);
		assertNotEquals(id_3, id_4);
		assertNotEquals(id_3, id_5);
		assertNotEquals(id_3, id_6);

		assertNotEquals(id_4, id_1);
		assertNotEquals(id_4, id_2);
		assertNotEquals(id_4, id_3);
		assertEquals(id_4, id_4);
		assertEquals(id_4, id_5); // part of transitive property test
		assertNotEquals(id_4, id_6);
		assertEquals(id_4, id_7); // part of transitive property test

		assertNotEquals(id_5, id_1);
		assertNotEquals(id_5, id_2);
		assertNotEquals(id_5, id_3);
		assertEquals(id_5, id_4);
		assertEquals(id_5, id_5);
		assertNotEquals(id_5, id_6);
		assertEquals(id_5, id_7); // part of transitive property test

		assertNotEquals(id_6, id_1);
		assertNotEquals(id_6, id_2);
		assertNotEquals(id_6, id_3);
		assertNotEquals(id_6, id_4);
		assertNotEquals(id_6, id_5);
		assertEquals(id_6, id_6);

		// null tests
		assertNotEquals(id_1, null);
		assertNotEquals(id_2, null);
		assertNotEquals(id_3, null);
		assertNotEquals(id_4, null);
		assertNotEquals(id_5, null);
		assertNotEquals(id_6, null);

	}

	@Test
	@UnitTestMethod(target = SimpleGlobalPropertyId.class, name = "hashCode", args = {})
	public void testHashCode() {

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleGlobalPropertyId s1 = new SimpleGlobalPropertyId(i);
			SimpleGlobalPropertyId s2 = new SimpleGlobalPropertyId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleGlobalPropertyId(i).hashCode());
			assertTrue(unique);
		}

	}

}
