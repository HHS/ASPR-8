package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_TrueFilter {

	@Test
	@UnitTestConstructor(target = TrueFilter.class, args = {})
	public void testTrueFilter() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// never equal to another type
		TrueFilter trueFilter = new TrueFilter();
		assertFalse(trueFilter.equals(new Object()));

		// is never equal to null
		assertFalse(trueFilter.equals(null));

		// reflexive
		assertTrue(trueFilter.equals(trueFilter));

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			TrueFilter f1 = new TrueFilter();
			TrueFilter f2 = new TrueFilter();
			for (int j = 0; j < 10; j++) {
				assertTrue(f1.equals(f2));
				assertTrue(f2.equals(f1));
			}
		}
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "hashCode", args = {})
	public void testHashCode() {
		assertEquals(1, new TrueFilter().hashCode());

		// equal objects have equal hash codes
		TrueFilter f1 = new TrueFilter();
		TrueFilter f2 = new TrueFilter();
		assertEquals(f1, f2);
		assertEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsContext partitionsContext = null;
		PersonId personId = new PersonId(56);
		assertTrue(new TrueFilter().evaluate(partitionsContext, personId));
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		TrueFilter trueFilter = new TrueFilter();
		assertTrue(trueFilter.getFilterSensitivities().isEmpty());
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "toString", args = {})
	public void testToString() {
		TrueFilter trueFilter = new TrueFilter();
		String actualValue = trueFilter.toString();
		String expectedValue = "TrueFilter []";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = TrueFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		PartitionsContext partitionsContext = null;

		/*
		 * TestFilters throw an exception when validate is invoked when their index is
		 * negative. We show here that the TrueFilter is invoking the validate for both
		 * of its child filters.
		 * 
		 */

		assertDoesNotThrow(() -> {
			TrueFilter trueFilter = new TrueFilter();
			trueFilter.validate(partitionsContext);
		});

		

	}

}
