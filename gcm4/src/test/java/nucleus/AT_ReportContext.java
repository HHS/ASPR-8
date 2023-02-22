package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestReportPlan;
import nucleus.testsupport.testplugin.TestSimulation;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_ReportContext {

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addKeyedPlan", args = { Consumer.class, double.class, Object.class })
	public void testAddKeyedPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * have the added test report add a plan that can be retrieved and thus
		 * was added successfully
		 */
		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(2, (context) -> {
			Object key = new Object();

			assertFalse(context.getPlan(key).isPresent());

			context.addKeyedPlan((c) -> {
			}, 3, key);

			assertTrue(context.getPlan(key).isPresent());
		}));

		/*
		 * Show that passive plans do not execute if there are no remaining
		 * active plans. To do this, we will schedule a few passive plans, one
		 * active plan and then a few more passive plans. We will then show that
		 * the passive plans that come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Object> expectedPassiveKeys = new LinkedHashSet<>();
		expectedPassiveKeys.add("A");
		expectedPassiveKeys.add("B");
		Set<Object> actualPassiveKeys = new LinkedHashSet<>();

		pluginDataBuilder.addTestReportPlan("actor", new TestReportPlan(4, (context) -> {

			// schedule two passive plans
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("A");
			}, 5, "A");
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("B");
			}, 6, "B");

			// schedule two more passive plans
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("C");
			}, 8, "C");
			context.addKeyedPlan((c) -> {
				actualPassiveKeys.add("D");
			}, 9, "D");

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(7, (context) -> {
			// place holder active plan that drives time to 7.0
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation -- we do not need to show that all plans executed
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedPassiveKeys, actualPassiveKeys);

		// precondition test : if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan(null, 0, "key");
		}));
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, -1, "key");
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test : if the plan key is null
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, 0, null);
		}));
		assertEquals(NucleusError.NULL_PLAN_KEY, contractException.getErrorType());

		// precondition test : if the plan key duplicates an existing plan
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addKeyedPlan((c2) -> {
			}, 0, "key");
			c.addKeyedPlan((c2) -> {
			}, 0, "key");
		}));
		assertEquals(NucleusError.DUPLICATE_PLAN_KEY, contractException.getErrorType());
	}

	/*
	 * Executes the simulation by adding TestReport that executes the give
	 * consumer in a task planned at time zero. Also adds a TestActor with a
	 * task scheduled at positive infinity to guarantee the execution of the
	 * report's task.
	 */
	private void testConsumer(Consumer<ReportContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestReportPlan("report", new TestReportPlan(0, consumer));
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY, (c) -> {
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulation.executeSimulation(testPlugin);

	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Show that passive plans do not execute if there are no remaining
		 * active plans. To do this, we will schedule a few passive plans, one
		 * active plan and then a few more passive plans. We will then show that
		 * the passive plans that come after the last active plan never execute
		 */

		// create some containers for passive keys
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("A");
		expectedOutput.add("B");
		Set<Object> actualOuput = new LinkedHashSet<>();

		pluginDataBuilder.addTestReportPlan("actor", new TestReportPlan(4, (context) -> {

			// schedule two passive plans
			context.addPlan((c) -> {
				actualOuput.add("A");
			}, 5);
			context.addPlan((c) -> {
				actualOuput.add("B");
			}, 6);

			// schedule two more passive plans
			context.addPlan((c) -> {
				actualOuput.add("C");
			}, 8);
			context.addPlan((c) -> {
				actualOuput.add("D");
			}, 9);

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(7, (context) -> {
			// place holder active plan that drives time to 7.0
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation -- we do not need to show that all plans executed
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that the last two passive plans did not execute
		assertEquals(expectedOutput, actualOuput);

		// precondition test : if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan(null, 0);
		}));
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		
		// precondition test : if the plan is scheduled for the past
		contractException = assertThrows(ContractException.class, () -> testConsumer((c) -> {
			c.addPlan((c2) -> {
			}, -1 );
		}));
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getDataManager", args = { Class.class }, tags= {UnitTag.INCOMPLETE})
	public void testGetDataManager() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlan", args = { Object.class }, tags= {UnitTag.INCOMPLETE})
	public void testGetPlan() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanKeys", args = {}, tags= {UnitTag.INCOMPLETE})
	public void testGetPlanKeys() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getPlanTime", args = { Object.class }, tags= {UnitTag.INCOMPLETE})
	public void testGetPlanTime() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getReportId", args = {}, tags= {UnitTag.INCOMPLETE})
	public void testGetReportId() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "getTime", args = {}, tags= {UnitTag.INCOMPLETE})
	public void testGetTime() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "releaseOutput", args = { Object.class }, tags= {UnitTag.INCOMPLETE})
	public void testReleaseOutput() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "removePlan", args = { Object.class }, tags= {UnitTag.INCOMPLETE})
	public void testRemovePlan() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribe", args = { Class.class, BiConsumer.class }, tags= {UnitTag.INCOMPLETE})
	public void testSubscribe() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "subscribeToSimulationClose", args = { Consumer.class }, tags= {UnitTag.INCOMPLETE})
	public void testSubscribeToSimulationClose() {
		
	}

	@Test
	@UnitTestMethod(target = ReportContext.class, name = "unsubscribe", args = { Class.class }, tags= {UnitTag.INCOMPLETE})
	public void testUnsubscribe() {
		
	}

}
