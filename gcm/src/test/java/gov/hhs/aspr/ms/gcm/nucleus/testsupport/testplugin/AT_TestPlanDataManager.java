package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestPlanDataManager {
	private static class TestDataManager1 extends TestDataManager {

	}

	private static class TestDataManager2 extends TestDataManager {

	}

	@Test
	@UnitTestMethod(target = TestPlanDataManager.class, name = "init", args = { DataManagerContext.class }, tags = { UnitTag.INCOMPLETE})
	public void testInit() {
		
		
		// test needs to demonstrate that the TestPlanDataManager releases the TestScenarioReport at the end of the simulation
	}

	@Test
	@UnitTestConstructor(target = TestPlanDataManager.class, args = { TestPluginData.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testConstructor() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(target = TestPlanDataManager.class, name = "getTestActorPlans", args = { Object.class })
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
		TestPluginData.Builder builder = TestPluginData.builder();

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
	@UnitTestMethod(target = TestPlanDataManager.class, name = "getTestReportPlans", args = { Object.class })
	public void testGetTestReportPlans() {
		// create a few TestRepoertPlan items associated with two aliases
		Map<String, Set<TestReportPlan>> expectedTestReportPlans = new LinkedHashMap<>();
		Set<TestReportPlan> testReportPlans = new LinkedHashSet<>();
		expectedTestReportPlans.put("actor1", testReportPlans);

		testReportPlans.add(new TestReportPlan(1, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(2, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(3, (c) -> {
		}));

		testReportPlans = new LinkedHashSet<>();
		expectedTestReportPlans.put("actor2", testReportPlans);
		testReportPlans.add(new TestReportPlan(4, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(5, (c) -> {
		}));

		// Build the plugin data from the items above
		TestPluginData.Builder builder = TestPluginData.builder();

		for (String alias : expectedTestReportPlans.keySet()) {
			testReportPlans = expectedTestReportPlans.get(alias);
			for (TestReportPlan testReportPlan : testReportPlans) {
				builder.addTestReportPlan(alias, testReportPlan);
			}
		}

		TestPluginData testPluginData = builder.build();
		TestPlanDataManager testPlanDataManager = new TestPlanDataManager(testPluginData);

		// show that the plans associated with each actors are correct
		for (String alias : expectedTestReportPlans.keySet()) {
			Set<TestReportPlan> expectedPlans = expectedTestReportPlans.get(alias);
			Set<TestReportPlan> actualPlans = new LinkedHashSet<>(testPlanDataManager.getTestReportPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(target = TestPlanDataManager.class, name = "getTestDataManagerPlans", args = { Object.class })
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
			for (TestDataManagerPlan testDataManagerPlan : planSet) {
				builder.addTestDataManagerPlan(alias, testDataManagerPlan);
			}
		}
		builder.addTestDataManager("A", () -> new TestDataManager1());

		builder.addTestDataManager("B", () -> new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// create the test plugin data manager
		TestPlanDataManager testPlanDataManager = new TestPlanDataManager(testPluginData);

		// show that the correct plans are stored
		for (Object alias : planMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = planMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPlanDataManager.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

}
