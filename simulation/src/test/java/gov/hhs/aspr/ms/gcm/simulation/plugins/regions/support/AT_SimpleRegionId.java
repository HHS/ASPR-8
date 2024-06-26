package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_SimpleRegionId {

	@Test
	@UnitTestConstructor(target = SimpleRegionId.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleRegionId(5));

		assertThrows(NullPointerException.class, () -> new SimpleRegionId(null));
	}
	
	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "getValue", args = {})
	public void testGetValue() {

		Object value = "some value";
		SimpleRegionId simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		value = 678;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		
		value = false;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		
		value = 2.98;
		simpleRegionId = new SimpleRegionId(value);
		assertEquals(value, simpleRegionId.getValue());
		

	}
	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleRegionId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleRegionId(5).toString());
		assertEquals("table", new SimpleRegionId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleRegionId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "equals", args = { Object.class })
	public void testEquals() {
		SimpleRegionId id_1 = new SimpleRegionId(2);
		SimpleRegionId id_2 = new SimpleRegionId(5);
		SimpleRegionId id_3 = new SimpleRegionId(2);
		SimpleRegionId id_4 = new SimpleRegionId("A");
		SimpleRegionId id_5 = new SimpleRegionId("A");
		SimpleRegionId id_6 = new SimpleRegionId("B");

		// reflexive
		assertEquals(id_1, id_1);
		assertEquals(id_2, id_2);
		assertEquals(id_3, id_3);
		assertEquals(id_4, id_4);
		assertEquals(id_5, id_5);
		assertEquals(id_6, id_6);

		// symmetric
		assertEquals(id_1, id_3);
		assertEquals(id_3, id_1);
		assertEquals(id_4, id_5);
		assertEquals(id_5, id_4);

		assertNotEquals(id_1, id_2);
		assertNotEquals(id_1, id_4);
		assertNotEquals(id_1, id_5);
		assertNotEquals(id_1, id_6);

		assertNotEquals(id_2, id_1);
		assertNotEquals(id_2, id_3);
		assertNotEquals(id_2, id_4);
		assertNotEquals(id_2, id_5);
		assertNotEquals(id_2, id_6);

		assertNotEquals(id_3, id_2);
		assertNotEquals(id_3, id_4);
		assertNotEquals(id_3, id_5);
		assertNotEquals(id_3, id_6);

		assertNotEquals(id_4, id_1);
		assertNotEquals(id_4, id_2);
		assertNotEquals(id_4, id_3);
		assertNotEquals(id_4, id_6);

		assertNotEquals(id_5, id_1);
		assertNotEquals(id_5, id_2);
		assertNotEquals(id_5, id_3);
		assertNotEquals(id_5, id_6);

		assertNotEquals(id_6, id_1);
		assertNotEquals(id_6, id_2);
		assertNotEquals(id_6, id_3);
		assertNotEquals(id_6, id_4);
		assertNotEquals(id_6, id_5);

		assertNotEquals(id_1, null);
		assertNotEquals(id_2, null);
		assertNotEquals(id_3, null);
		assertNotEquals(id_4, null);
		assertNotEquals(id_5, null);
		assertNotEquals(id_6, null);

	}

	@Test
	@UnitTestMethod(target = SimpleRegionId.class, name = "hashCode", args = {})
	public void testHashCode() {

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleRegionId s1 = new SimpleRegionId(i);
			SimpleRegionId s2 = new SimpleRegionId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleRegionId(i).hashCode());
			assertTrue(unique);
		}

	}
}
