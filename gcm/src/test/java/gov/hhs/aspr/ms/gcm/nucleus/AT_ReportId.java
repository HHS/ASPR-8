package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public final class AT_ReportId {

	@UnitTestConstructor(target = ReportId.class, args = { int.class })
	@Test
	public void testConstructor() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ReportId(i).getValue());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ReportId(i).getValue());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("ReportId [id=" + i + "]", new ReportId(i).toString());
		}
	}

	@UnitTestMethod(target = ReportId.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		// show equal objects have equal hashcodes
		for (int i = 0; i < 10; i++) {
			ReportId a = new ReportId(i);
			ReportId b = new ReportId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}

		// show that hash codes are dispersed
		Set<Integer> hashcodes = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			hashcodes.add(new ReportId(i).hashCode());
		}
		assertEquals(1000, hashcodes.size());

	}

	@UnitTestMethod(target = ReportId.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		// show actor ids are equal if and only if they have the same base int
		// value
		for (int i = 0; i < 10; i++) {
			ReportId a = new ReportId(i);
			for (int j = 0; j < 10; j++) {
				ReportId b = new ReportId(j);
				if (i == j) {
					assertEquals(a, b);
				} else {
					assertNotEquals(a, b);
				}
			}
		}
	}

}
