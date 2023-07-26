package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

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

public class AT_SimpleAttributeId {

	@Test
	@UnitTestConstructor(target = SimpleAttributeId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleAttributeId(5));

		assertThrows(RuntimeException.class, () -> new SimpleAttributeId(null));
	}

	@Test
	@UnitTestMethod(target = SimpleAttributeId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleAttributeId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleAttributeId(5).toString());
		assertEquals("table", new SimpleAttributeId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleAttributeId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleAttributeId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		SimpleAttributeId id_1 = new SimpleAttributeId(2);
		SimpleAttributeId id_2 = new SimpleAttributeId(5);
		SimpleAttributeId id_3 = new SimpleAttributeId(2);
		SimpleAttributeId id_4 = new SimpleAttributeId("A");
		SimpleAttributeId id_5 = new SimpleAttributeId("A");
		SimpleAttributeId id_6 = new SimpleAttributeId("B");

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
	@UnitTestMethod(target = SimpleAttributeId.class, name = "hashCode", args = {})
	public void testHashCode() {

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleAttributeId s1 = new SimpleAttributeId(i);
			SimpleAttributeId s2 = new SimpleAttributeId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleAttributeId(i).hashCode());
			assertTrue(unique);
		}

	}

}
