package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;

public class AT_ExperimentStatusConsole {

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.class, name = "accept", args = { ExperimentContext.class }, tags = { UnitTag.MANUAL })
	public void testAccept() {
		// deferred to manual test
	}

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.class, name = "builder", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testBuilder() {
		assertNotNull(ExperimentStatusConsole.builder().build());
	}

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.Builder.class, name = "build", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testBuild() {
		/*
		 * Implied behaviors of the resulting ExperimentStatusConsole are
		 * covered by the remaining tests
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.Builder.class, name = "setImmediateErrorReporting", args = { boolean.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testSetImmediateErrorReporting() {
		/*
		 * test covered by testAccept();
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.Builder.class, name = "setReportScenarioProgress", args = { boolean.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testSetReportScenarioProgress() {
		/*
		 * test covered by testAccept();
		 */
	}

	@Test
	@UnitTestMethod(target = ExperimentStatusConsole.Builder.class, name = "setStackTraceReportLimit", args = { int.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testSetStackTraceReportLimit() {
		/*
		 * test covered by testAccept();
		 */
	}

}
