package nucleus.testsupport.testplugin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;

@UnitTest(target = TestPlugin.class)
@Disabled
public class AT_TestPlugin {

	@Test
	public void test() {
		
	}
	
//	public static class TestDataManager1 extends TestDataManager {
//
//	}
//
//	public static class TestDataManager2 extends TestDataManager {
//
//	}
//
//	@Test
//	public void testGetPluginId() {
//		TestPluginInitializer testPluginInitializer = new TestPluginInitializer();
//		assertEquals(TestPluginId.PLUGIN_ID, testPluginInitializer.getPluginId());
//	}
//
//	@Test
//	public void testInit() {
//		// create aliases for actors and data managers
//		Object actorAlias1 = "actor 1";
//		Object actorAlias2 = "actor 2";
//		Object dataManagerAlias1 = "actor 1";
//		Object dataManagerAlias2 = "actor 2";
//
//		Map<Object, Set<TestActorPlan>> testActorPlanMap = new LinkedHashMap<>();
//		Map<Object, Set<TestDataManagerPlan>> testDataMangerPlanMap = new LinkedHashMap<>();
//		/*
//		 * Create a test actor plan that will be used to verify that the
//		 * initializer added the actors and data managers and provided a
//		 * TestPluginDataManager containing their plans
//		 */
//		TestActorPlan primaryPlan = new TestActorPlan(0, (c) -> {
//
//			// show that the data managers exist
//			Optional<?> optional = c.getDataManager(TestDataManager1.class);
//			assertTrue(optional.isPresent());
//
//			optional = c.getDataManager(TestDataManager2.class);
//			assertTrue(optional.isPresent());
//
//			TestPluginDataManager testPluginDataManager = c.getDataManager(TestPluginDataManager.class).get();
//
//			// show that actors exist
//			assertTrue(testPluginDataManager.getActorAlias(new ActorId(0)).isPresent());
//			assertTrue(testPluginDataManager.getActorAlias(new ActorId(1)).isPresent());
//
//			// show that the actor plans exist
//			for (Object alias : testActorPlanMap.keySet()) {
//				Set<TestActorPlan> expectedPlans = testActorPlanMap.get(alias);
//				Set<TestActorPlan> actualPlans = new LinkedHashSet<>(testPluginDataManager.getTestActorPlans(alias));
//				assertEquals(expectedPlans, actualPlans);
//			}
//
//			// show that the data manager plans exist
//			for (Object alias : testDataMangerPlanMap.keySet()) {
//				Set<TestDataManagerPlan> expectedPlans = testDataMangerPlanMap.get(alias);
//				Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginDataManager.getTestDataManagerPlans(alias));
//				assertEquals(expectedPlans, actualPlans);
//			}
//
//		});
//
//		// Collect various actor plans into a contain
//
//		Set<TestActorPlan> testActorPlans = new LinkedHashSet<>();
//		testActorPlans.add(primaryPlan);
//		testActorPlanMap.put(actorAlias1, testActorPlans);
//
//		testActorPlans = new LinkedHashSet<>();
//		testActorPlans.add(new TestActorPlan(1, (c) -> {
//		}));
//		testActorPlans.add(new TestActorPlan(2, (c) -> {
//		}));
//		testActorPlanMap.put(actorAlias2, testActorPlans);
//
//		// Collect various data manager plans into a contain
//
//		Set<TestDataManagerPlan> testManagerPlans = new LinkedHashSet<>();
//		testManagerPlans.add(new TestDataManagerPlan(3, (c) -> {
//		}));
//		testDataMangerPlanMap.put(dataManagerAlias1, testManagerPlans);
//
//		testManagerPlans = new LinkedHashSet<>();
//		testManagerPlans.add(new TestDataManagerPlan(4, (c) -> {
//		}));
//		testManagerPlans.add(new TestDataManagerPlan(5, (c) -> {
//		}));
//		testDataMangerPlanMap.put(dataManagerAlias2, testManagerPlans);
//
//		// build the plugin data from the plans and aliases above
//		TestPluginData.Builder builder = TestPluginData.builder();		
//		builder.addTestDataManager(dataManagerAlias1, TestDataManager1.class);
//		builder.addTestDataManager(dataManagerAlias2, TestDataManager2.class);
//		for (Object alias : testActorPlanMap.keySet()) {
//			Set<TestActorPlan> plans = testActorPlanMap.get(alias);
//			for (TestActorPlan plan : plans) {
//				builder.addTestActorPlan(alias, plan);
//			}
//		}
//
//		for (Object alias : testDataMangerPlanMap.keySet()) {
//			Set<TestDataManagerPlan> plans = testDataMangerPlanMap.get(alias);
//			for (TestDataManagerPlan plan : plans) {
//				builder.addTestDataManagerPlan(alias, plan);
//			}
//		}
//
//		TestPluginData testPluginData = builder.build();
//
//		TestPluginInitializer testPluginInitializer = new TestPluginInitializer();
//
//		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
//
//		// build and execute the engine
//		Experiment	.builder()//
//					.addOutputHandler(experimentPlanCompletionObserver::init)//
//					.addPluginInitializer(testPluginInitializer)//
//					.addPluginData(testPluginData)//
//					.build()//
//					.execute();//
//
//		// show that all actions executed
//		Optional<TestScenarioReport> optional = experimentPlanCompletionObserver.getActionCompletionReport(0);
//		assertTrue(optional.isPresent(), "Scenario did not complete");
//		TestScenarioReport testScenarioReport = optional.get();
//		assertTrue(testScenarioReport.isComplete(), "Some planned action were not executed");
//
//	}
}
