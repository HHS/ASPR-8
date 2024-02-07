package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_RunContinuityPlanData {

	@Test
	@UnitTestConstructor(target = RunContinuityPlanData.class, args = { int.class })
	public void testRunContinuityPlanData() {
		for (int i = 0; i < 10; i++) {
			RunContinuityPlanData runContinuityPlanData = new RunContinuityPlanData(i);
			assertEquals(i, runContinuityPlanData.getId());
		}
	}
	
	@Test
	@UnitTestMethod(target = RunContinuityPlanData.class, name = "getId", args = {})
	public void testGetId() {
		for (int i = 0; i < 10; i++) {
			RunContinuityPlanData runContinuityPlanData = new RunContinuityPlanData(i);
			assertEquals(i, runContinuityPlanData.getId());
		}
	}
}
