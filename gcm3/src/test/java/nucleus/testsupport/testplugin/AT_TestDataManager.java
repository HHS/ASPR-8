package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Experiment;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestActor.class)
public class AT_TestDataManager {
	
	public static class TestDataManagerType1 extends TestDataManager{
		
	}
	
	public static class TestDataManagerType2 extends TestDataManager{
		
	}


	@Test
	@UnitTestMethod(name = "init", args = { ActorContext.class })
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
		pluginDataBuilder.addTestDataManager(alias1,TestDataManagerType1.class);
		pluginDataBuilder.addTestDataManager(alias2,TestDataManagerType2.class);

		/*
		 * Create ActorActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestDataManagerPlan(expectedAlias, new TestDataManagerPlan(expectedTime, (c) -> {
				TestPluginDataManager testPluginDataManager = c.getDataManager(TestPluginDataManager.class).get();
				Object alias = testPluginDataManager.getDataManagerAlias(c.getDataManagerId()).get();
				actualObservations.add(new MultiKey(alias, c.getTime()));
			}));
		}
		
		pluginDataBuilder.addTestActor("actor");
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1,(c)->{
			Optional<TestDataManagerType1> optional1 = c.getDataManager(TestDataManagerType1.class);
			assertTrue(optional1.isPresent());
			
			Optional<TestDataManagerType2> optional2 = c.getDataManager(TestDataManagerType2.class);
			assertTrue(optional2.isPresent());
			
		}));
		

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		TestPluginInitializer testPluginInitializer = new TestPluginInitializer();

		TestExperimentObserver testExperimentObserver = new TestExperimentObserver();

		// build and execute the engine
		Experiment	.builder()//
					.addOutputHandler(testExperimentObserver::init)//
					.addPluginInitializer(testPluginInitializer)//
					.addPluginData(testPluginData)//
					.build()//
					.execute();//

		// show that all actions executed
		Optional<TestScenarioReport> optional = testExperimentObserver.getActionCompletionReport(0);
		assertTrue(optional.isPresent(),"Scenario did not complete");
		
		TestScenarioReport testScenarioReport = optional.get();
		assertTrue(testScenarioReport.isComplete(), "Some plans were not executed");

		// show that the actors executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

}
