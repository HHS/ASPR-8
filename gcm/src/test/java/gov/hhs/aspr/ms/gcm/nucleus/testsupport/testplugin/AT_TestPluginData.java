package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_TestPluginData {

	private static class TestDataManager1 extends TestDataManager {

	}

	private static class TestDataManager2 extends TestDataManager {

	}

	private static class TestDataManager3 extends TestDataManager {

	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(TestPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "getTestDataManager", args = { Object.class })
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
	@UnitTestMethod(target = TestPluginData.class, name = "getTestActorPlans", args = { Object.class })
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
	@UnitTestMethod(target = TestPluginData.class, name = "getTestReportPlans", args = { Object.class })
	public void testGetTestReportPlans() {
		// create a few TestReportPlan items associated with two aliases
		Map<String, Set<TestReportPlan>> expectedTestReportPlans = new LinkedHashMap<>();
		Set<TestReportPlan> testReportPlans = new LinkedHashSet<>();
		expectedTestReportPlans.put("report1", testReportPlans);

		testReportPlans.add(new TestReportPlan(1, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(2, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(3, (c) -> {
		}));

		testReportPlans = new LinkedHashSet<>();
		expectedTestReportPlans.put("report2", testReportPlans);
		testReportPlans.add(new TestReportPlan(4, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(5, (c) -> {
		}));

		// Build the plugin data from the items above
		TestPluginData.Builder builder = TestPluginData.builder();//

		for (String alias : expectedTestReportPlans.keySet()) {
			testReportPlans = expectedTestReportPlans.get(alias);
			for (TestReportPlan testReportPlan : testReportPlans) {
				builder.addTestReportPlan(alias, testReportPlan);
			}
		}

		TestPluginData testPluginData = builder.build();

		// show that the plans associated with each reports are correct
		for (String alias : expectedTestReportPlans.keySet()) {
			Set<TestReportPlan> expectedPlans = expectedTestReportPlans.get(alias);
			Set<TestReportPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestReportPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}

	}
	
	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "getTestActorAliases", args = {})
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
	@UnitTestMethod(target = TestPluginData.class, name = "getTestReportAliases", args = {})
	public void testGetTestReportAliases() {

		Set<Object> expectedAliases = new LinkedHashSet<>();
		expectedAliases.add("A");
		expectedAliases.add("B");
		expectedAliases.add("C");

		TestPluginData.Builder builder = TestPluginData.builder();//
		for (Object alias : expectedAliases) {
			builder.addTestReportPlan(alias, new TestReportPlan(0, (c) -> {
			}));
		}

		TestPluginData testPluginData = builder.build();

		LinkedHashSet<Object> actualAliases = new LinkedHashSet<>(testPluginData.getTestReportAliases());
		assertEquals(expectedAliases, actualAliases);

	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "getCloneBuilder", args = {})
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
		builder.addTestDataManager("D", () -> new TestDataManager1());
		builder.addTestDataManagerPlan("D", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManager("E", () -> new TestDataManager2());
		builder.addTestDataManagerPlan("E", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManagerPlan("E", new TestDataManagerPlan(0, (c) -> {
		}));
		builder.addTestDataManager("F", () -> new TestDataManager3());

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
	@UnitTestMethod(target = TestPluginData.class, name = "getTestDataManagerPlans", args = { Object.class })
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

		builder.addTestDataManager("A", () -> new TestDataManager1());
		builder.addTestDataManager("B", () -> new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testDataManagerPlanMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = testDataManagerPlanMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}

	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "getTestDataManagerAliases", args = {})
	public void testGetTestDataManagerAliases() {
		Set<Object> expectedAliases = new LinkedHashSet<>();

		TestPluginData.Builder builder = TestPluginData.builder();
		expectedAliases.add("A");
		builder.addTestDataManager("A", () -> new TestDataManager1());
		expectedAliases.add("B");
		builder.addTestDataManager("B", () -> new TestDataManager2());
		expectedAliases.add("C");
		builder.addTestDataManager("C", () -> new TestDataManager3());
		TestPluginData testPluginData = builder.build();

		LinkedHashSet<Object> actualAliases = new LinkedHashSet<>(testPluginData.getTestDataManagerAliases());
		assertEquals(expectedAliases, actualAliases);

	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addTestActorPlan", args = { Object.class, TestActorPlan.class })
	public void testAddTestActorPlan() {
		// create a few plans
		Map<Object, Set<TestActorPlan>> testActorPlanMap = new LinkedHashMap<>();
		Set<TestActorPlan> testActorPlans = new LinkedHashSet<>();
		testActorPlans.add(new TestActorPlan(0, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(1, (c) -> {
		}));
		testActorPlanMap.put("A", testActorPlans);

		testActorPlans = new LinkedHashSet<>();
		testActorPlans.add(new TestActorPlan(2, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(3, (c) -> {
		}));
		testActorPlans.add(new TestActorPlan(4, (c) -> {
		}));
		testActorPlanMap.put("B", testActorPlans);

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : testActorPlanMap.keySet()) {
			testActorPlans = testActorPlanMap.get(alias);
			for (TestActorPlan testActorPlan : testActorPlans) {
				builder.addTestActorPlan(alias, testActorPlan);
			}
		}

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testActorPlanMap.keySet()) {
			Set<TestActorPlan> expectedPlans = testActorPlanMap.get(alias);
			Set<TestActorPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestActorPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}
	
	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addTestReportPlan", args = { Object.class, TestReportPlan.class })
	public void testAddTestReportPlan() {
		// create a few plans
		Map<Object, Set<TestReportPlan>> testReportPlanMap = new LinkedHashMap<>();
		Set<TestReportPlan> testReportPlans = new LinkedHashSet<>();
		testReportPlans.add(new TestReportPlan(0, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(1, (c) -> {
		}));
		testReportPlanMap.put("A", testReportPlans);

		testReportPlans = new LinkedHashSet<>();
		testReportPlans.add(new TestReportPlan(2, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(3, (c) -> {
		}));
		testReportPlans.add(new TestReportPlan(4, (c) -> {
		}));
		testReportPlanMap.put("B", testReportPlans);

		// add those plans to the builder
		TestPluginData.Builder builder = TestPluginData.builder();
		for (Object alias : testReportPlanMap.keySet()) {
			testReportPlans = testReportPlanMap.get(alias);
			for (TestReportPlan testReportPlan : testReportPlans) {
				builder.addTestReportPlan(alias, testReportPlan);
			}
		}

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testReportPlanMap.keySet()) {
			Set<TestReportPlan> expectedPlans = testReportPlanMap.get(alias);
			Set<TestReportPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestReportPlans(alias));
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
		builder.addTestDataManager("A", () -> new TestDataManager1());
		builder.addTestDataManager("B", () -> new TestDataManager2());
		builder.addTestDataManager("C", () -> new TestDataManager3());

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

		builder.addTestDataManager("A", () -> new TestDataManager1());
		builder.addTestDataManager("B", () -> new TestDataManager2());

		TestPluginData testPluginData = builder.build();

		// show that the plugin data contains the expected plans
		for (Object alias : testDataManagerPlanMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = testDataManagerPlanMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginData.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "addPluginDependency", args = { PluginId.class })
	public void testAddPluginDependency() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6227272133886959116L);
		Set<PluginId> candidatePluginIds = new LinkedHashSet<>();
		candidatePluginIds.add(new SimplePluginId("A"));
		candidatePluginIds.add(new SimplePluginId("B"));
		candidatePluginIds.add(new SimplePluginId("C"));
		candidatePluginIds.add(new SimplePluginId("D"));
		candidatePluginIds.add(new SimplePluginId("E"));

		for (int i = 0; i < 30; i++) {

			Set<PluginId> expectedPluginIds = new LinkedHashSet<>();
			TestPluginData.Builder builder = TestPluginData.builder();
			for (PluginId pluginId : candidatePluginIds) {
				if (randomGenerator.nextBoolean()) {
					builder.addPluginDependency(pluginId);
					expectedPluginIds.add(pluginId);
				}
			}

			Set<PluginId> actualPluginIds = builder.build().getPluginDependencies();
			assertEquals(expectedPluginIds, actualPluginIds);
		}

		// precondition test: if the plugin id is null
		ContractException contractException = assertThrows(ContractException.class, () -> TestPluginData.builder().addPluginDependency(null));
		assertEquals(TestError.NULL_PLUGIN_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = TestPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// covered by other tests
	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		SimplePluginId simplePluginIdA = new SimplePluginId("A");
		SimplePluginId simplePluginIdB = new SimplePluginId("B");
		Supplier<TestDataManager> supplier1 = () -> new TestDataManager();
		Supplier<TestDataManager> supplier2 = () -> new TestDataManager();
		TestActorPlan testActorPlan1 = new TestActorPlan(0, (c) -> {
		});
		TestActorPlan testActorPlan2 = new TestActorPlan(0, (c) -> {
		});
		TestDataManagerPlan testDataManagerPlan1 = new TestDataManagerPlan(0, (c) -> {
		});
		TestDataManagerPlan testDataManagerPlan2 = new TestDataManagerPlan(0, (c) -> {
		});

		TestPluginData testPluginData1 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();

		// identical inputs
		TestPluginData testPluginData2 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();

		assertEquals(testPluginData1, testPluginData2);

		// with different plugin dependencies
		TestPluginData testPluginData3 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdB)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();
		assertNotEquals(testPluginData1, testPluginData3);

		testPluginData3 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addPluginDependency(simplePluginIdB)//
										.addTestActorPlan("actor", testActorPlan1)//
										.addTestDataManagerPlan("dm", testDataManagerPlan1)//
										.addTestDataManager("dm", supplier1)//
										.build();
		assertNotEquals(testPluginData1, testPluginData3);

		// with different actor plans
		TestPluginData testPluginData4 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan2)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();
		assertEquals(testPluginData1, testPluginData4);

		testPluginData4 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addTestActorPlan("actor2", testActorPlan1)//
										.addTestDataManagerPlan("dm", testDataManagerPlan1)//
										.addTestDataManager("dm", supplier1)//
										.build();
		assertNotEquals(testPluginData1, testPluginData4);

		// with different data manager plans
		TestPluginData testPluginData5 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan2)//
														.addTestDataManager("dm", supplier1)//
														.build();
		assertEquals(testPluginData1, testPluginData5);

		// with different data manager suppliers
		TestPluginData testPluginData6 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier2)//
														.build();
		assertNotEquals(testPluginData1, testPluginData6);

	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * Show that equal objects have equal hash codes over a few example
		 * cases
		 */

		SimplePluginId simplePluginIdA = new SimplePluginId("A");
		SimplePluginId simplePluginIdB = new SimplePluginId("B");
		Supplier<TestDataManager> supplier1 = () -> new TestDataManager();
		Supplier<TestDataManager> supplier2 = () -> new TestDataManager();
		TestActorPlan testActorPlan1 = new TestActorPlan(0, (c) -> {
		});
		TestActorPlan testActorPlan2 = new TestActorPlan(0, (c) -> {
		});
		TestDataManagerPlan testDataManagerPlan1 = new TestDataManagerPlan(0, (c) -> {
		});
		TestDataManagerPlan testDataManagerPlan2 = new TestDataManagerPlan(0, (c) -> {
		});

		TestPluginData testPluginData1 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();

		// identical inputs
		TestPluginData testPluginData2 = TestPluginData	.builder()//
														.addPluginDependency(simplePluginIdA)//
														.addTestActorPlan("actor", testActorPlan1)//
														.addTestDataManagerPlan("dm", testDataManagerPlan1)//
														.addTestDataManager("dm", supplier1)//
														.build();

		assertEquals(testPluginData1.hashCode(), testPluginData2.hashCode());

		testPluginData1 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addPluginDependency(simplePluginIdB)//
										.addTestActorPlan("actor", testActorPlan2)//
										.addTestDataManagerPlan("dm", testDataManagerPlan1)//
										.addTestDataManager("dm", supplier2)//
										.build();

		testPluginData2 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addPluginDependency(simplePluginIdB)//
										.addTestActorPlan("actor", testActorPlan2)//
										.addTestDataManagerPlan("dm", testDataManagerPlan1)//
										.addTestDataManager("dm", supplier2)//
										.build();

		assertEquals(testPluginData1.hashCode(), testPluginData2.hashCode());

		testPluginData1 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addPluginDependency(simplePluginIdB)//
										.addTestActorPlan("actor", testActorPlan1)//
										.addTestActorPlan("actor", testActorPlan2)//
										.addTestDataManagerPlan("dm", testDataManagerPlan2)//
										.addTestDataManager("dm", supplier2)//
										.build();

		testPluginData2 = TestPluginData.builder()//
										.addPluginDependency(simplePluginIdA)//
										.addPluginDependency(simplePluginIdB)//
										.addTestActorPlan("actor", testActorPlan1)//
										.addTestActorPlan("actor", testActorPlan2)//
										.addTestDataManagerPlan("dm", testDataManagerPlan2)//
										.addTestDataManager("dm", supplier2)//
										.build();

		assertEquals(testPluginData1.hashCode(), testPluginData2.hashCode());

	}

	@Test
	@UnitTestMethod(target = TestPluginData.class, name = "getPluginDependencies", args = {})
	public void testGetPluginDependencies() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4923209038525994062L);
		Set<PluginId> candidatePluginIds = new LinkedHashSet<>();
		candidatePluginIds.add(new SimplePluginId("A"));
		candidatePluginIds.add(new SimplePluginId("B"));
		candidatePluginIds.add(new SimplePluginId("C"));
		candidatePluginIds.add(new SimplePluginId("D"));
		candidatePluginIds.add(new SimplePluginId("E"));

		for (int i = 0; i < 30; i++) {

			Set<PluginId> expectedPluginIds = new LinkedHashSet<>();
			TestPluginData.Builder builder = TestPluginData.builder();
			for (PluginId pluginId : candidatePluginIds) {
				if (randomGenerator.nextBoolean()) {
					builder.addPluginDependency(pluginId);
					expectedPluginIds.add(pluginId);
				}
			}

			Set<PluginId> actualPluginIds = builder.build().getPluginDependencies();
			assertEquals(expectedPluginIds, actualPluginIds);
		}
	}

}
