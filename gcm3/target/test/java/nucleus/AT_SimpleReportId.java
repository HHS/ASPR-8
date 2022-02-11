package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
@UnitTest(target = SimpleReportId.class)
public class AT_SimpleReportId {
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		assertThrows(RuntimeException.class, () -> new SimpleReportId(null));
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		/*
		 * SimpleReportIds are equal if and only if their contained values are
		 * equal
		 */

		SimpleReportId simpleReportId_1 = new SimpleReportId("A");
		SimpleReportId simpleReportId_2 = new SimpleReportId("B");
		SimpleReportId simpleReportId_3 = new SimpleReportId("A");

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
			SimpleReportId simpleReportId_1 = new SimpleReportId(i);
			SimpleReportId simpleReportId_2 = new SimpleReportId(i);
			assertEquals(simpleReportId_1.hashCode(), simpleReportId_2.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		assertEquals("SimpleReportId [value=A]",new SimpleReportId("A").toString());
		assertEquals("SimpleReportId [value=ASDF]",new SimpleReportId("ASDF").toString());
		assertEquals("SimpleReportId [value=12]",new SimpleReportId(12).toString());
	}
}
