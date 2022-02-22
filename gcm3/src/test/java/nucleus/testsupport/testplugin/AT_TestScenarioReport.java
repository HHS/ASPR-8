package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestScenarioReport.class)
public class AT_TestScenarioReport {

	@Test
	@UnitTestConstructor(args = Boolean.class)
	public void testConstructor() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		
		//create a few reports with different completion states
		TestScenarioReport report1 = new TestScenarioReport(true);
		TestScenarioReport report2 = new TestScenarioReport(true);
		TestScenarioReport report3 = new TestScenarioReport(true);
		
		TestScenarioReport report4 = new TestScenarioReport(false);
		TestScenarioReport report5 = new TestScenarioReport(false);
		TestScenarioReport report6 = new TestScenarioReport(false);

		//show reflexive equality
		assertEquals(report1, report1);
		assertEquals(report2, report2);
		assertEquals(report3, report3);
		assertEquals(report4, report4);
		assertEquals(report5, report5);
		assertEquals(report6, report6);
		
		//show symmetric equality
		assertEquals(report1, report2);
		assertEquals(report2, report1);
		
		//show transitivity
		assertEquals(report1, report2);
		assertEquals(report2, report3);
		assertEquals(report1, report3);
			
		//show that equality is equivalent to completion
		assertNotEquals(report1, report4);
		
		
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		//equal objects have equal hash codes
		TestScenarioReport report1 = new TestScenarioReport(true);
		TestScenarioReport report2 = new TestScenarioReport(true);
		TestScenarioReport report3 = new TestScenarioReport(false);		
		TestScenarioReport report4 = new TestScenarioReport(false);
		
		assertEquals(report1.hashCode(), report2.hashCode());
		assertEquals(report3.hashCode(), report4.hashCode());
	}

	@Test
	@UnitTestMethod(name = "isComplete", args = {})
	public void testIsComplete() {
		TestScenarioReport report1 = new TestScenarioReport(true);
		assertTrue(report1.isComplete());
		TestScenarioReport report2 = new TestScenarioReport(false);
		assertFalse(report2.isComplete());
	}

}
