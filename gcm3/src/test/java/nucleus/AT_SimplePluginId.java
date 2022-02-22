package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
@UnitTest(target = SimplePluginId.class)
public class AT_SimplePluginId {
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertThrows(RuntimeException.class, () -> new SimplePluginId(null));
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		/*
		 * SimplePluginIds are equal if and only if their contained values are
		 * equal
		 */

		SimplePluginId simplePluginId_1 = new SimplePluginId("A");
		SimplePluginId simplePluginId_2 = new SimplePluginId("B");
		SimplePluginId simplePluginId_3 = new SimplePluginId("A");

		assertEquals(simplePluginId_1, simplePluginId_3);
		assertNotEquals(simplePluginId_1, simplePluginId_2);

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * Equal objects have equal hash codes
		 */
		for (int i = 0; i < 20; i++) {
			SimplePluginId simplePluginId_1 = new SimplePluginId(i);
			SimplePluginId simplePluginId_2 = new SimplePluginId(i);
			assertEquals(simplePluginId_1.hashCode(), simplePluginId_2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		assertEquals("A",new SimplePluginId("A").toString());
		assertEquals("ASDF",new SimplePluginId("ASDF").toString());
		assertEquals(Integer.toString(12),new SimplePluginId(12).toString());
	}
}
