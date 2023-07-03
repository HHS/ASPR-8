package nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestPlugin;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_RunContinuityPlanData {

	@Test
	@UnitTestConstructor(target = TestPlugin.class, args = { int.class })
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
