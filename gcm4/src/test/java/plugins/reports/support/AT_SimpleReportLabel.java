package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableInteger;

public class AT_SimpleReportLabel {

	@Test
	@UnitTestConstructor(target = SimpleReportLabel.class, args = { Object.class })
	public void testConstructor() {
		assertNotNull(new SimpleReportLabel(5));

		// show that a null report label is not thrown
		ContractException contractException = assertThrows(ContractException.class, () -> new SimpleReportLabel(null));
		assertEquals(contractException.getErrorType(), ReportError.NULL_REPORT_LABEL);

	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "toString", args = {})
	public void testToString() {
		Object value = 325;
		SimpleReportLabel simpleReportLabel = new SimpleReportLabel(value);
		String expectedString = "SimpleReportLabel [value=" + value + "]";
		String actualString = simpleReportLabel.toString();

		assertEquals(expectedString, actualString);
	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "hashCode", args = {})
	public void testHashCode() {
		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			SimpleReportLabel s1 = new SimpleReportLabel(i);
			SimpleReportLabel s2 = new SimpleReportLabel(i);
			assertEquals(s1.hashCode(), s2.hashCode());
		}

		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			boolean unique = hashCodes.add(new SimpleReportLabel(i).hashCode());
			assertTrue(unique);
		}
	}

	@Test
	@UnitTestMethod(target = SimpleReportLabel.class, name = "equals", args = { Object.class }, tags = UnitTag.INCOMPLETE)
	public void testEquals() {
		Object value = 2;
		SimpleReportLabel id_1 = new SimpleReportLabel(2);
		SimpleReportLabel id_2 = new SimpleReportLabel(5);
		SimpleReportLabel id_3 = new SimpleReportLabel(2);
		SimpleReportLabel id_4 = new SimpleReportLabel("A");
		SimpleReportLabel id_5 = new SimpleReportLabel("A");
		SimpleReportLabel id_6 = new SimpleReportLabel("B");
		SimpleReportLabel id_7 = new SimpleReportLabel("A");
		MutableInteger simpleGlobalPropertyId = new MutableInteger(2);

		// should return false if the object is not a SimpleReportLabel
		assertNotEquals(id_1, simpleGlobalPropertyId);

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

}