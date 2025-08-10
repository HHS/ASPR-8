package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_TestScenarioReport {

	@Test
	@UnitTestConstructor(target = TestScenarioReport.class, args = boolean.class)
	public void testConstructor() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(target = TestScenarioReport.class, name = "equals", args = { Object.class })
	public void testEquals() {

		// create a few reports with different completion states
		TestScenarioReport report1 = new TestScenarioReport(true);
		TestScenarioReport report2 = new TestScenarioReport(true);
		TestScenarioReport report3 = new TestScenarioReport(true);

		TestScenarioReport report4 = new TestScenarioReport(false);
		TestScenarioReport report5 = new TestScenarioReport(false);
		TestScenarioReport report6 = new TestScenarioReport(false);

		// never equal to another type
		assertFalse(report1.equals(new Object()));
		assertFalse(report2.equals(new Object()));
		assertFalse(report3.equals(new Object()));
		assertFalse(report4.equals(new Object()));
		assertFalse(report5.equals(new Object()));
		assertFalse(report6.equals(new Object()));

		// never equal to null
		assertNotEquals(report1, null);
		assertNotEquals(report2, null);
		assertNotEquals(report3, null);
		assertNotEquals(report4, null);
		assertNotEquals(report5, null);
		assertNotEquals(report6, null);

		// show reflexive equality
		assertEquals(report1, report1);
		assertEquals(report2, report2);
		assertEquals(report3, report3);
		assertEquals(report4, report4);
		assertEquals(report5, report5);
		assertEquals(report6, report6);

		// symmetric, transitive, consistent
		for (int j = 0; j < 10; j++) {
			assertFalse(report1 == report2);
			assertEquals(report1, report2);
			assertEquals(report2, report1);

			assertFalse(report1 == report3);
			assertEquals(report1, report3);
			assertEquals(report3, report1);

			assertFalse(report3 == report2);
			assertEquals(report3, report2);
			assertEquals(report2, report3);

			assertFalse(report4 == report5);
			assertEquals(report4, report5);
			assertEquals(report5, report4);

			assertFalse(report4 == report6);
			assertEquals(report4, report6);
			assertEquals(report6, report4);

			assertFalse(report6 == report5);
			assertEquals(report6, report5);
			assertEquals(report5, report6);
		}

		// different inputs yield unequal testScenarioReports
		assertNotEquals(report1, report4);
		assertNotEquals(report1, report5);
		assertNotEquals(report1, report6);
		assertNotEquals(report2, report4);
		assertNotEquals(report2, report5);
		assertNotEquals(report2, report6);
		assertNotEquals(report3, report4);
		assertNotEquals(report3, report5);
		assertNotEquals(report3, report6);
	}

	@Test
	@UnitTestMethod(target = TestScenarioReport.class, name = "hashCode", args = {})
	public void testHashCode() {
		
		TestScenarioReport report1 = new TestScenarioReport(true);
		TestScenarioReport report2 = new TestScenarioReport(true);
		TestScenarioReport report3 = new TestScenarioReport(false);
		TestScenarioReport report4 = new TestScenarioReport(false);

		// equal objects have equal hash codes
		assertEquals(report1, report2);
		assertEquals(report1.hashCode(), report2.hashCode());

		assertEquals(report3, report4);
		assertEquals(report3.hashCode(), report4.hashCode());

		// hash codes are reasonably distributed
		assertNotEquals(report1.hashCode(), report3.hashCode());
		assertNotEquals(report1.hashCode(), report4.hashCode());
		assertNotEquals(report2.hashCode(), report3.hashCode());
		assertNotEquals(report2.hashCode(), report4.hashCode());
	}

	@Test
	@UnitTestMethod(target = TestScenarioReport.class, name = "isComplete", args = {})
	public void testIsComplete() {
		TestScenarioReport report1 = new TestScenarioReport(true);
		assertTrue(report1.isComplete());
		TestScenarioReport report2 = new TestScenarioReport(false);
		assertFalse(report2.isComplete());
	}

}
