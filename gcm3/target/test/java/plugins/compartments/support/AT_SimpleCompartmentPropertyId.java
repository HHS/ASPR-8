package plugins.compartments.support;

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


public class AT_SimpleCompartmentPropertyId{

	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleCompartmentPropertyId(5));

		assertThrows(RuntimeException.class, () -> new SimpleCompartmentPropertyId(null));
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleCompartmentPropertyId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleCompartmentPropertyId(5).toString());
		assertEquals("table", new SimpleCompartmentPropertyId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleCompartmentPropertyId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testEquals() {
		SimpleCompartmentPropertyId id_1 = new SimpleCompartmentPropertyId(2);
		SimpleCompartmentPropertyId id_2 = new SimpleCompartmentPropertyId(5);
		SimpleCompartmentPropertyId id_3 = new SimpleCompartmentPropertyId(2);
		SimpleCompartmentPropertyId id_4 = new SimpleCompartmentPropertyId("A");
		SimpleCompartmentPropertyId id_5 = new SimpleCompartmentPropertyId("A");
		SimpleCompartmentPropertyId id_6 = new SimpleCompartmentPropertyId("B");

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
			SimpleCompartmentPropertyId s1 = new SimpleCompartmentPropertyId(i);
			SimpleCompartmentPropertyId s2 = new SimpleCompartmentPropertyId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}
		
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleCompartmentPropertyId(i).hashCode());
			assertTrue(unique);
		}

	}
}
