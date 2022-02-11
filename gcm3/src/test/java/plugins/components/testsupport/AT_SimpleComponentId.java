package plugins.components.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
@UnitTest(target = SimpleComponentId.class)
public class AT_SimpleComponentId {
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertThrows(RuntimeException.class, () -> new SimpleComponentId(null));
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		/*
		 * SimpleComponentIds are equal if and only if their contained values are
		 * equal
		 */

		SimpleComponentId simpleReportId_1 = new SimpleComponentId("A");
		SimpleComponentId simpleReportId_2 = new SimpleComponentId("B");
		SimpleComponentId simpleReportId_3 = new SimpleComponentId("A");

		assertEquals(simpleReportId_1, simpleReportId_3);
		assertNotEquals(simpleReportId_1, simpleReportId_2);

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * Equal objects have equal hash codes
		 */
		for (int i = 0; i < 20; i++) {
			SimpleComponentId simpleReportId_1 = new SimpleComponentId(i);
			SimpleComponentId simpleReportId_2 = new SimpleComponentId(i);
			assertEquals(simpleReportId_1.hashCode(), simpleReportId_2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		assertEquals("A",new SimpleComponentId("A").toString());
		assertEquals("ASDF",new SimpleComponentId("ASDF").toString());
		assertEquals(Integer.toString(12),new SimpleComponentId(12).toString());
	}
}
