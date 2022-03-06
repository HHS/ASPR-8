package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Experiment;
import nucleus.Plugin;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestActor.class)
public class AT_TestActor {

	@Test
	@UnitTestMethod(name = "init", args = { ActorContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "actor alias 1";
		Object alias2 = "actor alias 2";

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

		/*
		 * Create ActorActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestActorPlan(expectedAlias, new TestActorPlan(expectedTime, (c) -> {
				TestPlanDataManager testPlanDataManager = c.getDataManager(TestPlanDataManager.class).get();
				Object alias = testPlanDataManager.getActorAlias(c.getActorId()).get();
				actualObservations.add(new MultiKey(alias, c.getTime()));
			}));
		}

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);

		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

		// build and execute the engine
		Experiment	.builder()//
					.setExperimentProgressConsole(false)//
					.setReportScenarioFailureToConsole(false)//
					.addOutputHandler(experimentPlanCompletionObserver::init)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions executed
		Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(0);
		assertTrue(optional.isPresent(), "Scenario did not complete");

		TestScenarioReport testScenarioReport = optional.get();
		assertTrue(testScenarioReport.isComplete(), "Some planned action were not executed");

		// show that the actors executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

}
