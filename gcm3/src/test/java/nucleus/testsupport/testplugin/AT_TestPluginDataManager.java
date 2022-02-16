package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ActorId;
import nucleus.DataManagerId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = TestPluginDataManager.class)
public class AT_TestPluginDataManager {
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
		TestPluginData.Builder builder = TestPluginData	.builder()//
														.addTestActor("actor1")//
														.addTestActor("actor2");//
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
		fail();
	}

	@Test
	@UnitTestMethod(name = "getDataManagerActionPlans", args = { Class.class })
	public void testGetDataManagerActionPlans() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "getDataManagerAlias", args = { DataManagerId.class })
	public void testGetDataManagerAlias() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "setActorAlias", args = { ActorId.class, Object.class })
	public void testSetActorAlias() {
		fail();
	}

	@Test
	@UnitTestMethod(name = "setDataManagerAlias", args = { DataManagerId.class, Object.class })
	public void testSetDataManagerAlias() {
		fail();
	}
}
