package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = SimpleRegionPropertyId.class)
public class AT_SimpleRegionPropertyId {

	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleRegionPropertyId(5));

		assertThrows(NullPointerException.class, () -> new SimpleRegionPropertyId(null));
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		/*
		 * Show that the toString of the SimpleRegionPropertyId equals its input's
		 * toString
		 */

		assertEquals(Integer.toString(5), new SimpleRegionPropertyId(5).toString());
		assertEquals("table", new SimpleRegionPropertyId("table").toString());
		assertEquals(Double.toString(2345.5345), new SimpleRegionPropertyId(2345.5345).toString());

	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
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
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleRegionPropertyId s1 = new SimpleRegionPropertyId(i);
			SimpleRegionPropertyId s2 = new SimpleRegionPropertyId(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleRegionPropertyId(i).hashCode());
			assertTrue(unique);
		}

	}
}
