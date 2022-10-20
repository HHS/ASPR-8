package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = TestPluginData.class)
public class AT_TestPluginData {

	private static class TestDataManager1 extends TestDataManager {

	}

	private static class TestDataManager2 extends TestDataManager {

	}

	private static class TestDataManager3 extends TestDataManager {

	}

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(TestPluginData.builder());
	}

	@Test
	@UnitTestMethod(name = "getTestDataManager", args = { Object.class })
	public void testGetTestDataManagerType() {
		TestPluginData testPluginData = TestPluginData	.builder()//
														.addTestDataManager("A", () -> new TestDataManager1())//
														.addTestDataManager("B", () -> new TestDataManager2())//
														.addTestDataManager("C", () -> new TestDataManager3())//
														.build();//

		// show that the aliased data manager types are retrievable
		Optional<TestDataManager1> optional1 = testPluginData.getTestDataManager("A");
		assertTrue(optional1.isPresent());

		Optional<TestDataManager2> optional2 = testPluginData.getTestDataManager("B");
		assertTrue(optional2.isPresent());

		Optional<TestDataManager3> optional3 = testPluginData.getTestDataManager("C");
		assertTrue(optional3.isPresent());

		Optional<TestDataManager> optional4 = testPluginData.getTestDataManager("D");
		assertFalse(optional4.isPresent());

	}

	@Test
	@UnitTestMethod(name = "getTestActorPlans", args = { Object.class })
	public void testGetTestActorPlans() {
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
		TestPluginData.Builder builder = TestPluginData.builder();//

		for (String alias : expectedTestActorPlans.keySet()) {
			testActorPlans = expectedTestActorPlans.get(alias);
			for (TestActorPlan testActorPlan : testActorPlans) {
				builder.addTestActorPlan(alias, testActorPlan);
			}
		}

		TestPluginData testPluginData = builder.build();

		// show that the plans associated with each actors are correct
		for (String alias : expectedTestActorPlans.keySet()) {
			Set<TestActorPlan> expectedPlans = expectedTestActorPlans.get(alias);
			Set<TestActorPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestActorPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}

	}

	@Test
	@UnitTestMethod(name = "getTestActorAliases", args = {})
	public void testGetTestActorAliases() {

		Set<Object> expectedAliases = new LinkedHashSet<>();
		expectedAliases.add("A");
		expectedAliases.add("B");
		expectedAliases.add("C");

		TestPluginData.Builder builder = TestPluginData.builder();//
		for (Object alias : expectedAliases) {
			builder.addTestActorPlan(alias, new TestActorPlan(0, (c) -> {
			}));
		}

		TestPluginData testPluginData = builder.build();

		LinkedHashSet<Object> actualAliases = new LinkedHashSet<>(testPluginData.getTestActorAliases());
		assertEquals(expectedAliases, actualAliases);

	}

	@Test
	@UnitTestMethod(name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		TestPluginData.Builder builder = TestPluginData.builder();//
		// add actors

		builder.addTestActorPlan("A", new TestActorPlan(0, (c) -> {
		}));

		builder.addTestActorPlan("B", new TestActorPlan(0, (c) -> {
		}));
		builder.addTestActorPlan("B", new TestActorPlan(0, (c) -> {
		}));
		builder.addTestActorPlan("C", new TestActorPlan(0, (c) -> {
		}));

		// add data managers
		builder.addTestDataManager("D", ()->new TestDataManager1());
		builder.addTestDataManagerPlan("D", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManager("E", ()->new TestDataManager2());
		builder.addTestDataManagerPlan("E", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManagerPlan("E", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManager("F", ()->new TestDataManager3());

		// build the plugin data
		TestPluginData testPluginData = builder.build();

		// show that the clone builder is properly initialized -- i.e. it will
		// immediately build a clone of the plugin data
		TestPluginData.Builder cloneBuilder = testPluginData.getCloneBuilder();
		assertNotNull(cloneBuilder);
		TestPluginData testPluginData2 = cloneBuilder.build();
		assertEquals(testPluginData, testPluginData2);

	}

	@Test
	@UnitTestMethod(name = "getTestDataManagerPlans", args = { Object.class })
	public void testGetTestDataManagerPlans() {
		// create a few plans
		Map<Object, Set<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();
		Set<TestDataManagerPlan> testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(0, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(1, (c) -> {
		}));
		testDataManagerPlanMap.put("A", testDataManagerPlans);

		testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(2, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(3, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(4, (c) -> {
		}));
		testDataManagerPlanMap.put("B", testDataManagerPlans);

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : testDataManagerPlanMap.keySet()) {
			testDataManagerPlans = testDataManagerPlanMap.get(alias);
			for (TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
				builder.addTestDataManagerPlan(alias, testDataManagerPlan);
			}
		}

		builder.addTestDataManager("A", ()->new TestDataManager1());
		builder.addTestDataManager("B", ()->new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testDataManagerPlanMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = testDataManagerPlanMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}

	}

	@Test
	@UnitTestMethod(name = "getTestDataManagerAliases", args = {})
	public void testGetTestDataManagerAliases() {
		Set<Object> expectedAliases = new LinkedHashSet<>();

		TestPluginData.Builder builder = TestPluginData.builder();
		expectedAliases.add("A");
		builder.addTestDataManager("A", ()->new TestDataManager1());
		expectedAliases.add("B");
		builder.addTestDataManager("B", ()->new TestDataManager2());
		expectedAliases.add("C");
		builder.addTestDataManager("C", ()->new TestDataManager3());
		TestPluginData testPluginData = builder.build();

		LinkedHashSet<Object> actualAliases = new LinkedHashSet<>(testPluginData.getTestDataManagerAliases());
		assertEquals(expectedAliases, actualAliases);

	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addTestActorPlan", args = { Object.class, TestActorPlan.class })
	public void testAddTestActorPlan() {
		// create a few plans
		Map<Object, Set<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();
		Set<TestDataManagerPlan> testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(0, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(1, (c) -> {
		}));
		testDataManagerPlanMap.put("A", testDataManagerPlans);

		testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(2, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(3, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(4, (c) -> {
		}));
		testDataManagerPlanMap.put("B", testDataManagerPlans);

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : testDataManagerPlanMap.keySet()) {
			testDataManagerPlans = testDataManagerPlanMap.get(alias);
			for (TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
				builder.addTestDataManagerPlan(alias, testDataManagerPlan);
			}
		}

		builder.addTestDataManager("A", ()->new TestDataManager1());
		builder.addTestDataManager("B", ()->new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testDataManagerPlanMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = testDataManagerPlanMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addTestDataManager", args = { Object.class, Supplier.class })
	
	 
	public void testAddTestDataManager() {
		// create a few plans
		LinkedHashSet<Object> expectedDataManagerAliases = new LinkedHashSet<>();
		expectedDataManagerAliases.add("A");
		expectedDataManagerAliases.add("B");
		expectedDataManagerAliases.add("C");

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		builder.addTestDataManager("A",()->new TestDataManager1());
		builder.addTestDataManager("B",()->new TestDataManager2());
		builder.addTestDataManager("C",()->new TestDataManager3());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		LinkedHashSet<Object> actualDataManagerAliases = new LinkedHashSet<>(testPluginData.getTestDataManagerAliases());
		assertEquals(expectedDataManagerAliases, actualDataManagerAliases);
	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addTestDataManagerPlan", args = { Object.class, TestDataManagerPlan.class })
	public void testAddTestDataManagerPlan() {
		// create a few plans
		Map<Object, Set<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();
		Set<TestDataManagerPlan> testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(0, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(1, (c) -> {
		}));
		testDataManagerPlanMap.put("A", testDataManagerPlans);

		testDataManagerPlans = new LinkedHashSet<>();
		testDataManagerPlans.add(new TestDataManagerPlan(2, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(3, (c) -> {
		}));
		testDataManagerPlans.add(new TestDataManagerPlan(4, (c) -> {
		}));
		testDataManagerPlanMap.put("B", testDataManagerPlans);

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : testDataManagerPlanMap.keySet()) {
			testDataManagerPlans = testDataManagerPlanMap.get(alias);
			for (TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
				builder.addTestDataManagerPlan(alias, testDataManagerPlan);
			}
		}

		builder.addTestDataManager("A", ()->new TestDataManager1());
		builder.addTestDataManager("B", ()->new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testDataManagerPlanMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = testDataManagerPlanMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// covered by other tests
	}

}
