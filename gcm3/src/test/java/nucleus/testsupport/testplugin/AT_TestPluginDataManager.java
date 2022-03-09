package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;

@UnitTest(target = TestPlanDataManager.class)
public class AT_TestPluginDataManager {
	private static class TestDataManager1 extends TestDataManager {

	}

	private static class TestDataManager2 extends TestDataManager {

	}

	

	
	@Test
	@UnitTestConstructor(args = { TestPluginData.class })
	public void test_Constructor() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(name = "getActorActionPlans", args = { Object.class })
	public void testGetActorActionPlans() {
		// create a few TestActorPlan items associated with two aliases
		Map<String, Set<TestActorPlan>> expectedTestActorPlans = new LinkedHashMap<>();
		Set<TestActorPlan> testActorPlans = new LinkedHashSet<>();
		expectedTestActorPlans.put("actor1", testActorPlans);

		testActorPlans.add(new TestActorPlan(1, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(2, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(3, (c) -> {
		}));

		testActorPlans = new LinkedHashSet<>();
		expectedTestActorPlans.put("actor2", testActorPlans);
		testActorPlans.add(new TestActorPlan(4, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(5, (c) -> {
		}));

		// Build the plugin data from the items above
		TestPluginData.Builder builder = TestPluginData	.builder();
		
		for (String alias : expectedTestActorPlans.keySet()) {
			testActorPlans = expectedTestActorPlans.get(alias);
			for (TestActorPlan testActorPlan : testActorPlans) {
				builder.addTestActorPlan(alias, testActorPlan);
			}
		}

		TestPluginData testPluginData = builder.build();
		TestPlanDataManager testPlanDataManager = new TestPlanDataManager(testPluginData);

		// show that the plans associated with each actors are correct
		for (String alias : expectedTestActorPlans.keySet()) {
			Set<TestActorPlan> expectedPlans = expectedTestActorPlans.get(alias);
			Set<TestActorPlan> actualPlans = new LinkedHashSet<>(testPlanDataManager.getTestActorPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	

	@Test
	@UnitTestMethod(name = "getTestDataManagerPlans", args = { Object.class })
	public void testGetTestDataManagerPlans() {
		// build a few test data manager plans
		Map<Object, Set<TestDataManagerPlan>> planMap = new LinkedHashMap<>();
		Set<TestDataManagerPlan> planSet = new LinkedHashSet<>();
		planSet.add(new TestDataManagerPlan(0, (c) -> {
		}));
		planSet.add(new TestDataManagerPlan(1, (c) -> {
		}));
		planMap.put("A", planSet);
		planSet = new LinkedHashSet<>();
		planSet.add(new TestDataManagerPlan(2, (c) -> {
		}));
		planSet.add(new TestDataManagerPlan(3, (c) -> {
		}));
		planSet.add(new TestDataManagerPlan(4, (c) -> {
		}));
		planMap.put("B", planSet);

		// add them to a test plugin data
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : planMap.keySet()) {
			
			planSet = planMap.get(alias);
			for(TestDataManagerPlan testDataManagerPlan : planSet) {
				builder.addTestDataManagerPlan(alias, testDataManagerPlan);
			}
		}
		builder.addTestDataManager("A", ()-> new TestDataManager1());
		
		builder.addTestDataManager("B", ()-> new TestDataManager2());
		
		TestPluginData testPluginData = builder.build();

		//create the test plugin data manager
		TestPlanDataManager testPlanDataManager = new TestPlanDataManager(testPluginData);
		
		//show that the correct plans are stored
		for (Object alias : planMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = planMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPlanDataManager.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	

	

	
}
