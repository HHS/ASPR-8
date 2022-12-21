package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.Experiment;
import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

@UnitTest(target = TestDataManager.class)
public class AT_TestDataManager {

	private static class TestDataManagerType1 extends TestDataManager {

	}

	private static class TestDataManagerType2 extends TestDataManager {

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "alias 1";
		Object alias2 = "alias 2";

		// create containers for expected and actual observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		expectedObservations.add(new MultiKey(alias1, 3.0));
		expectedObservations.add(new MultiKey(alias2, 3.0));
		expectedObservations.add(new MultiKey(alias1, 4.212));
		expectedObservations.add(new MultiKey(alias1, 5.123));
		expectedObservations.add(new MultiKey(alias2, 43.0));
		expectedObservations.add(new MultiKey(alias1, 12.123));
		expectedObservations.add(new MultiKey(alias1, 8.534));
		expectedObservations.add(new MultiKey(alias2, 1.423));

		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add the actors to the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager(alias1, ()->new TestDataManagerType1());
		pluginDataBuilder.addTestDataManager(alias2, ()->new TestDataManagerType2());

		/*
		 * Create ActorActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestDataManagerPlan(expectedAlias, new TestDataManagerPlan(expectedTime, (c) -> {
				actualObservations.add(new MultiKey(expectedAlias, c.getTime()));
			}));
		}

		

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

		// build and execute the engine
		Experiment	.builder()//					
					.addExperimentContextConsumer(experimentPlanCompletionObserver::init)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions executed
		Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(0);
		assertTrue(optional.isPresent(), "Scenario did not complete");

		TestScenarioReport testScenarioReport = optional.get();
		assertTrue(testScenarioReport.isComplete(), "Some plans were not executed");

		// show that the actors executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}
	
	@Test
	@UnitTestConstructor(args = {}, tags = {UnitTag.INCOMPLETE})
	public void testConstructor() {
		//nothing to test		
	}

}
