package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import nucleus.Experiment;
import nucleus.ExperimentContext;
import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ExperimentPlanCompletionObserver.class)
public class AT_ExperimentPlanCompletionObserver {

	@Test
	@UnitTestMethod(name = "getActionCompletionReport", args = { Integer.class })
	public void testGetActionCompletionReport() {

		/*
		 * Without the test plugin, the experiment plan completion observer will
		 * not receive any evidence that scenarios completed.
		 */
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

		Experiment	.builder()//
					.addExperimentContextConsumer(experimentPlanCompletionObserver::init)//
					.build()//
					.execute();

		Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(0);
		assertTrue(optional.isEmpty());

		/*
		 * With the test plugin, the experiment plan completion observer will
		 * receive evidence that scenarios completed.
		 */
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

		Experiment	.builder()//
					.addPlugin(testPlugin).addExperimentContextConsumer(experimentPlanCompletionObserver::init)//
					.build()//					
					.execute();

		/*
		 * Scenario zero is the default scenario even when there are no dimensions in the experiment
		 */
		optional = experimentPlanCompletionObserver.getActionCompletionReport(0);
		assertTrue(optional.isPresent());
				
		/*
		 * No other scenarios should be present
		 */

		optional = experimentPlanCompletionObserver.getActionCompletionReport(1);
		assertFalse(optional.isPresent());
	}

	@Test
	@UnitTestMethod(name = "init", args = {ExperimentContext.class}, tags = { UnitTag.LOCAL_PROXY })
	public void testInit() {
		// covered by the test: testGetActionCompletionReport()
	}
	
	@Test
	@UnitTestConstructor(args = {}, tags = { UnitTag.INCOMPLETE })
	public void testConstructor() {
		// nothing to test
	}
}
