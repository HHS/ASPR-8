package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_FalseFilter {

	@Test
	@UnitTestConstructor(target = FalseFilter.class, args = {})
	public void testTrueFilter() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		FalseFilter falseFilter = new FalseFilter();

		// is never equal to null
		assertFalse(falseFilter.equals(null));

		// reflexive
		assertTrue(falseFilter.equals(falseFilter));

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			FalseFilter f1 = new FalseFilter();
			FalseFilter f2 = new FalseFilter();
			for (int j = 0; j < 10; j++) {
				assertTrue(f1.equals(f2));
				assertTrue(f2.equals(f1));
			}
		}
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "hashCode", args = {})
	public void testHashCode() {
		assertEquals(0, new FalseFilter().hashCode());
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsContext partitionsContext = null;
		PersonId personId = new PersonId(56);
		assertFalse(new FalseFilter().evaluate(partitionsContext, personId));
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		FalseFilter falseFilter = new FalseFilter();
		assertTrue(falseFilter.getFilterSensitivities().isEmpty());
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "toString", args = {})
	public void testToString() {
		FalseFilter falseFilter = new FalseFilter();
		String actualValue = falseFilter.toString();
		String expectedValue = "FalseFilter []";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = FalseFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		PartitionsContext partitionsContext = null;

		/*
		 * TestFilters throw an exception when validate is invoked when their index is
		 * negative. We show here that the FalseFilter is invoking the validate for both
		 * of its child filters.
		 * 
		 */

		assertDoesNotThrow(() -> {
			FalseFilter falseFilter = new FalseFilter();
			falseFilter.validate(partitionsContext);
		});

		

	}

}
