package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ActorId;
import nucleus.DataManagerId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestPluginDataManager.class)
public class AT_TestPluginDataManager {
	public static class TestDataManager1 extends TestDataManager {

	}

	public static class TestDataManager2 extends TestDataManager {

	}

	public static class TestDataManager3 extends TestDataManager {

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
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(testPluginData);

		// show that the plans associated with each actors are correct
		for (String alias : expectedTestActorPlans.keySet()) {
			Set<TestActorPlan> expectedPlans = expectedTestActorPlans.get(alias);
			Set<TestActorPlan> actualPlans = new LinkedHashSet<>(testPluginDataManager.getTestActorPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(name = "getActorAlias", args = { ActorId.class })
	public void testGetActorAlias() {
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(TestPluginData.builder().build());
		Optional<Object> optional = testPluginDataManager.getActorAlias(null);
		assertFalse(optional.isPresent());

		testPluginDataManager.setActorAlias(new ActorId(0), "A");
		testPluginDataManager.setActorAlias(new ActorId(1), "B");
		testPluginDataManager.setActorAlias(new ActorId(2), "C");

		optional = testPluginDataManager.getActorAlias(new ActorId(0));
		assertTrue(optional.isPresent());
		Object alias = optional.get();
		assertEquals("A", alias);

		optional = testPluginDataManager.getActorAlias(new ActorId(1));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("B", alias);

		optional = testPluginDataManager.getActorAlias(new ActorId(2));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("C", alias);

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
		builder.addTestDataManager("A", TestDataManager1.class);
		builder.addTestDataManager("B", TestDataManager2.class);
		
		TestPluginData testPluginData = builder.build();

		//create the test plugin data manager
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(testPluginData);
		
		//show that the correct plans are stored
		for (Object alias : planMap.keySet()) {
			Set<TestDataManagerPlan> expectedPlans = planMap.get(alias);
			Set<TestDataManagerPlan> actualPlans = new LinkedHashSet<>(testPluginDataManager.getTestDataManagerPlans(alias));
			assertEquals(expectedPlans, actualPlans);
		}
	}

	@Test
	@UnitTestMethod(name = "getDataManagerAlias", args = { DataManagerId.class })
	public void testGetDataManagerAlias() {
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(TestPluginData.builder().build());
		Optional<Object> optional = testPluginDataManager.getActorAlias(null);
		assertFalse(optional.isPresent());

		testPluginDataManager.setDataManagerAlias(new DataManagerId(0), "A");
		testPluginDataManager.setDataManagerAlias(new DataManagerId(1), "B");
		testPluginDataManager.setDataManagerAlias(new DataManagerId(2), "C");
		

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(0));
		assertTrue(optional.isPresent());
		Object alias = optional.get();
		assertEquals("A", alias);

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(1));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("B", alias);

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(2));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("C", alias);
	}

	@Test
	@UnitTestMethod(name = "setActorAlias", args = { ActorId.class, Object.class })
	public void testSetActorAlias() {
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(TestPluginData.builder().build());
		Optional<Object> optional = testPluginDataManager.getActorAlias(null);
		assertFalse(optional.isPresent());

		testPluginDataManager.setActorAlias(new ActorId(0), "A");
		testPluginDataManager.setActorAlias(new ActorId(1), "B");
		testPluginDataManager.setActorAlias(new ActorId(2), "C");

		optional = testPluginDataManager.getActorAlias(new ActorId(0));
		assertTrue(optional.isPresent());
		Object alias = optional.get();
		assertEquals("A", alias);

		optional = testPluginDataManager.getActorAlias(new ActorId(1));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("B", alias);

		optional = testPluginDataManager.getActorAlias(new ActorId(2));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("C", alias);
	}

	@Test
	@UnitTestMethod(name = "setDataManagerAlias", args = { DataManagerId.class, Object.class })
	public void testSetDataManagerAlias() {
		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(TestPluginData.builder().build());
		Optional<Object> optional = testPluginDataManager.getActorAlias(null);
		assertFalse(optional.isPresent());

		testPluginDataManager.setDataManagerAlias(new DataManagerId(0), "A");
		testPluginDataManager.setDataManagerAlias(new DataManagerId(1), "B");
		testPluginDataManager.setDataManagerAlias(new DataManagerId(2), "C");
		

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(0));
		assertTrue(optional.isPresent());
		Object alias = optional.get();
		assertEquals("A", alias);

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(1));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("B", alias);

		optional = testPluginDataManager.getDataManagerAlias(new DataManagerId(2));
		assertTrue(optional.isPresent());
		alias = optional.get();
		assertEquals("C", alias);
	}
}
