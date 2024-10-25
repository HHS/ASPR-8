package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManagerPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestScenarioReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public class AT_DataManagerContext {

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getTime", args = {})
	public void testGetTime() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		Set<Double> planTimes = new LinkedHashSet<>();

		planTimes.add(4.6);
		planTimes.add(13.8764);
		planTimes.add(554.345);
		planTimes.add(7.95346);
		planTimes.add(400.234234);
		planTimes.add(3000.12422346);

		/*
		 * Have the data manager build plans to check the time in the simulation against
		 * the planning time
		 */
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context1) -> {
			for (Double planTime : planTimes) {
				context1.addPlan((context2) -> {
					assertEquals(planTime.doubleValue(), context2.getTime(), 0);
				}, planTime);
			}
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "releaseOutput", args = { Object.class })
	public void testReleaseOutput() {

		// begin building the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// set up the expected output
		Set<Object> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add("the sly fox");
		expectedOutput.add(15);
		expectedOutput.add("the lazy, brown dog");
		expectedOutput.add(45.34513453);

		// have the data manager release the output
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (Object outputValue : expectedOutput) {
				c.releaseOutput(outputValue);
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Set<Object> actualOutput = new LinkedHashSet<>();

		/*
		 * Add an output consumer that will place the output into the actualOutput set
		 * above and then execute the simulation
		 */
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.setOutputConsumer((o) -> {
					if (!(o instanceof TestScenarioReport)) {
						actualOutput.add(o);
					}
				})//
				.build()//
				.execute();//

		// show that the output matches expectations
		assertEquals(expectedOutput, actualOutput);

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "subscribeToSimulationClose", args = { Consumer.class })
	public void testSubscribeToSimulationClose() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean simCloseEventHandled = new MutableBoolean();

		// have a data manager schedule a few events and subscribe to simulation
		// close
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			c.addPlan((c2) -> {
			}, 1);
			c.addPlan((c2) -> {
			}, 2);
			c.addPlan((c2) -> {
			}, 3);
			c.subscribeToSimulationClose((c2) -> {
				simCloseEventHandled.setValue(true);
			});
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the subscription to simulation close was successful
		assertTrue(simCloseEventHandled.getValue());

	}

	private void executeGetDataManagerTest(String dmName, Consumer<DataManagerContext> consumer) {
		PluginId pluginId0 = new SimplePluginId("local plugin 0");
		PluginId pluginId5 = new SimplePluginId("local plugin 5");
		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		pluginDataBuilder.addTestDataManagerPlan(dmName, new TestDataManagerPlan(0, consumer));

		pluginDataBuilder.addPluginDependency(pluginId0);

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();

		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Plugin localPlugin0 = Plugin.builder()//
				.setPluginId(pluginId0)//
				.setInitializer((c) -> c.addDataManager(new TestDataManager0()))//
				.build();
		Plugin localPlugin5 = Plugin.builder()//
				.setPluginId(pluginId5)//
				.setInitializer((c) -> c.addDataManager(new TestDataManager5()))//
				.build();

		TestSimulation.builder()//
				.addPlugin(localPlugin0)//
				.addPlugin(localPlugin5)//
				.addPlugin(testPlugin).build()//
				.execute();//
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getDataManager", args = { Class.class })
	public void testGetDataManager() {
		/*
		 * postcondition test: we demonstrate for all of the test data managers in the
		 * test plugin that they can retrieve the instances that are allowed by the
		 * access rules. Note that TestDataManager0 and TestDataManager5 are defined by
		 * local plugins and only TestDataManager0 is accessible by the remaining data
		 * managers since the test plugin has a plugin dependency on the local plugin
		 * containing the instance of TestDataManager0, but does not have a plugin
		 * dependency on the plugin containing the instance of TestDataManager5.
		 */
		executeGetDataManagerTest("dm4A", (c) -> {
			assertNotNull(c.getDataManager(TestDataManager0.class));
			assertNotNull(c.getDataManager(TestDataManager1.class));
			assertNotNull(c.getDataManager(TestDataManager2.class));
			assertNotNull(c.getDataManager(TestDataManager3A.class));
			assertNotNull(c.getDataManager(TestDataManager3B.class));
		});

		executeGetDataManagerTest("dm3B", (c) -> {
			assertNotNull(c.getDataManager(TestDataManager0.class));
			assertNotNull(c.getDataManager(TestDataManager1.class));
			assertNotNull(c.getDataManager(TestDataManager2.class));
			assertNotNull(c.getDataManager(TestDataManager3A.class));
		});

		executeGetDataManagerTest("dm3A", (c) -> {
			assertNotNull(c.getDataManager(TestDataManager0.class));
			assertNotNull(c.getDataManager(TestDataManager1.class));
			assertNotNull(c.getDataManager(TestDataManager2.class));
		});

		executeGetDataManagerTest("dm2", (c) -> {
			assertNotNull(c.getDataManager(TestDataManager0.class));
			assertNotNull(c.getDataManager(TestDataManager1.class));
		});

		executeGetDataManagerTest("dm1", (c) -> {
			assertNotNull(c.getDataManager(TestDataManager0.class));
		});

		// precondition test: if the class reference is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			executeGetDataManagerTest("dm1", (c) -> {
				c.getDataManager(null);
			});
		});
		assertEquals(NucleusError.NULL_DATA_MANAGER_CLASS, contractException.getErrorType());

		// precondition test: if the class reference is null
		contractException = assertThrows(ContractException.class, () -> {
			executeGetDataManagerTest("dm1", (c) -> {
				c.getDataManager(TestDataManager3.class);
			});
		});
		assertEquals(NucleusError.AMBIGUOUS_DATA_MANAGER_CLASS, contractException.getErrorType());

		// precondition test: if the class reference is unknown
		contractException = assertThrows(ContractException.class, () -> {
			executeGetDataManagerTest("dm1", (c) -> {
				c.getDataManager(TestDataManager4B.class);
			});
		});
		assertEquals(NucleusError.UNKNOWN_DATA_MANAGER, contractException.getErrorType());

		// precondition test: if the requestor does not have access due to order within
		// the plugin
		contractException = assertThrows(ContractException.class, () -> {
			executeGetDataManagerTest("dm1", (c) -> {
				c.getDataManager(TestDataManager2.class);
			});
		});
		assertEquals(NucleusError.DATA_MANAGER_ACCESS_VIOLATION, contractException.getErrorType());

		// precondition test: if the requestor does not have access due to having no
		// plugin dependency path
		contractException = assertThrows(ContractException.class, () -> {
			executeGetDataManagerTest("dm1", (c) -> {
				c.getDataManager(TestDataManager5.class);
			});
		});
		assertEquals(NucleusError.DATA_MANAGER_ACCESS_VIOLATION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "dataManagerExists", args = { Class.class })
	public void testDataManagerExists() {

		// create the test plugin data builder
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a data manager for the actor to find

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManager("dm3A", () -> new TestDataManager3A());
		pluginDataBuilder.addTestDataManager("dm3B", () -> new TestDataManager3B());
		pluginDataBuilder.addTestDataManager("dm4A", () -> new TestDataManager4A());

		// show the various ways bad class references return false
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(1, (c) -> {
			// show that zero class matching returns false
			assertFalse(c.dataManagerExists(TestDataManager4B.class));

			// show that ambiguous class matching returns false
			assertFalse(c.dataManagerExists(TestDataManager3.class));

			// show that a null yields a false
			assertFalse(c.dataManagerExists(null));
		}));

		// show ordering restrictions are enforced
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(1, (c) -> {
			assertFalse(c.dataManagerExists(TestDataManager1.class));
			assertFalse(c.dataManagerExists(TestDataManager2.class));
			assertFalse(c.dataManagerExists(TestDataManager3A.class));
			assertFalse(c.dataManagerExists(TestDataManager3B.class));
			assertFalse(c.dataManagerExists(TestDataManager4A.class));
		}));

		// show ordering restrictions are enforced
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(1, (c) -> {
			assertTrue(c.dataManagerExists(TestDataManager1.class));
			assertFalse(c.dataManagerExists(TestDataManager2.class));
			assertFalse(c.dataManagerExists(TestDataManager3A.class));
			assertFalse(c.dataManagerExists(TestDataManager3B.class));
			assertFalse(c.dataManagerExists(TestDataManager4A.class));
		}));

		// show ordering restrictions are enforced
		pluginDataBuilder.addTestDataManagerPlan("dm3A", new TestDataManagerPlan(1, (c) -> {
			assertTrue(c.dataManagerExists(TestDataManager1.class));
			assertTrue(c.dataManagerExists(TestDataManager2.class));
			assertFalse(c.dataManagerExists(TestDataManager3A.class));
			assertFalse(c.dataManagerExists(TestDataManager3B.class));
			assertFalse(c.dataManagerExists(TestDataManager4A.class));
		}));

		// show ordering restrictions are enforced
		pluginDataBuilder.addTestDataManagerPlan("dm3B", new TestDataManagerPlan(1, (c) -> {
			assertTrue(c.dataManagerExists(TestDataManager1.class));
			assertTrue(c.dataManagerExists(TestDataManager2.class));
			assertTrue(c.dataManagerExists(TestDataManager3A.class));
			assertFalse(c.dataManagerExists(TestDataManager3B.class));
			assertFalse(c.dataManagerExists(TestDataManager4A.class));
		}));

		// show ordering restrictions are enforced
		pluginDataBuilder.addTestDataManagerPlan("dm4A", new TestDataManagerPlan(1, (c) -> {
			assertTrue(c.dataManagerExists(TestDataManager1.class));
			assertTrue(c.dataManagerExists(TestDataManager2.class));
			assertTrue(c.dataManagerExists(TestDataManager3A.class));
			assertTrue(c.dataManagerExists(TestDataManager3B.class));
			assertFalse(c.dataManagerExists(TestDataManager4A.class));
		}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();

		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "addPlan", args = { Consumer.class, double.class })
	public void testAddPlan_Consumer() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		MutableBoolean planExecuted = new MutableBoolean();

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {
			// schedule two passive plans
			context.addPlan((c) -> {
				planExecuted.setValue(true);
			}, 5);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

		// precondition test: if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.addPlan(null, 0);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.NULL_PLAN_CONSUMER, contractException.getErrorType());

		// precondition test: if the plan is scheduled for a time in the past
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.addPlan((c2) -> {
								}, -10);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test: if the plan is added to the simulation after event
		// processing is finished
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.subscribeToSimulationClose(c2 -> {
									c2.addPlan((c3) -> {
									}, 0);
								});
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "addPlan", args = { DataManagerPlan.class })
	public void testAddPlan_Plan() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		MutableBoolean planExecuted = new MutableBoolean();

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {
			// schedule two passive plans
			context.addPlan(new ConsumerDataManagerPlan(5, (c) -> {
				planExecuted.setValue(true);
			}));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// we do not need to show that all plans executed

		// show that the last two passive plans did not execute
		assertTrue(planExecuted.getValue());

		// precondition test: if the plan is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.addPlan(null);
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.NULL_PLAN, contractException.getErrorType());

		// precondition test: if the arrival id is less than -1
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.addPlan(new ConsumerDataManagerPlan(0, true, -2, (c2) -> {
								}));
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.INVALID_PLAN_ARRIVAL_ID, contractException.getErrorType());

		// precondition test: if the plan is scheduled for a time in the past
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.addPlan(new ConsumerDataManagerPlan(-10, true, -1, (c2) -> {
								}));
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PAST_PLANNING_TIME, contractException.getErrorType());

		// precondition test: if the plan is added to the simulation after event
		// processing is finished
		contractException = assertThrows(ContractException.class, () -> {
			Simulation.builder()//
					.addPlugin(TestPlugin.getTestPlugin(TestPluginData.builder()//
							.addTestDataManager("dm", () -> new TestDataManager1())
							.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
								c.subscribeToSimulationClose(c2 -> {
									c2.addPlan(new ConsumerDataManagerPlan(0, true, -1, (c3) -> {
									}));
								});
							})).build()))//
					.build()//
					.execute();//
		});
		assertEquals(NucleusError.PLANNING_QUEUE_CLOSED, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getScheduledSimulationHaltTime", args = {})
	public void testGetScheduledSimulationHaltTime() {
		Set<Double> stopTimes = new LinkedHashSet<>();

		stopTimes.add(4.6);
		stopTimes.add(13.0);
		stopTimes.add(554.3);
		stopTimes.add(7.9);
		stopTimes.add(400.2);
		stopTimes.add(3000.1);

		for (Double stopTime : stopTimes) {
			TestPluginData testPluginData = TestPluginData.builder()
					.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context) -> {
						assertEquals(stopTime, context.getScheduledSimulationHaltTime());
					})).addTestDataManager("dm", () -> new TestDataManager1()).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(stopTime).addPlugin(testPlugin).build().execute();
		}
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "releaseObservationEvent", args = { Event.class })
	public void testReleaseObservationEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean eventResolved = new MutableBoolean();

		// Have the data manager subscribe to test event and then set the
		// eventResolved to true
		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
				eventResolved.setValue(true);
			});
		}));

		// have another data manager resolve a test event
		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(1, (context) -> {
			context.releaseObservationEvent(new TestEvent1());
		}));

		// precondition tests
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class,
					() -> context.releaseObservationEvent(null));
			assertEquals(NucleusError.NULL_EVENT, contractException.getErrorType());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that event actually resolved
		assertTrue(eventResolved.getValue());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "releaseMutationEvent", args = { Event.class })
	public void testReleaseMutationEvent() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean eventResolved = new MutableBoolean();

		// Have the data manager subscribe to test event and then set the
		// eventResolved to true
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
				eventResolved.setValue(true);
			});
		}));

		// have the data manager release the mutation event
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			context.releaseMutationEvent(new TestEvent1());
		}));

		// have the data manager show the event was handled
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			assertTrue(eventResolved.getValue());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// precondition test: if the event is null
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			ContractException contractException = assertThrows(ContractException.class,
					() -> context.releaseObservationEvent(null));
			assertEquals(NucleusError.NULL_EVENT, contractException.getErrorType());
		}));

		// build the plugin
		testPluginData = pluginDataBuilder.build();
		testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "stateRecordingIsScheduled", args = {})
	public void testStateRecordingIsScheduled() {
		Set<Boolean> stateRecordingList = new LinkedHashSet<>();

		stateRecordingList.add(false);
		stateRecordingList.add(false);
		stateRecordingList.add(true);
		stateRecordingList.add(false);
		stateRecordingList.add(true);
		stateRecordingList.add(true);

		for (Boolean stateRecording : stateRecordingList) {
			TestPluginData testPluginData = TestPluginData.builder()
					.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context) -> {
						assertEquals(stateRecording, context.stateRecordingIsScheduled());
					})).addTestDataManager("dm", () -> new TestDataManager1()).build();

			Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

			TestSimulation.builder().setSimulationHaltTime(1).setProduceSimulationStateOnHalt(stateRecording)
					.addPlugin(testPlugin).build().execute();
		}
	}

	private static class TestEvent1 implements Event {

	}

	private static class TestDataManager0 extends DataManager {

	}

	private static class TestDataManager1 extends TestDataManager {
	}

	private static class TestDataManager2 extends TestDataManager {
	}

	private static class TestDataManager3 extends TestDataManager {

	}

	private static class TestDataManager3A extends TestDataManager3 {

	}

	private static class TestDataManager3B extends TestDataManager3 {

	}

	private static class TestDataManager4 extends TestDataManager {

	}

	private static class TestDataManager4A extends TestDataManager4 {

	}

	private static class TestDataManager4B extends TestDataManager4 {

	}

	private static class TestDataManager5 extends DataManager {

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "actorExists", args = { ActorId.class })
	public void testActorExists() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(testTime++, (context) -> {
			for (int i = 0; i < 3; i++) {
				ActorId actorId = new ActorId(i);
				assertFalse(context.actorExists(actorId));
			}
			for (int i = 0; i < 3; i++) {
				ActorId actorId = context.addActor((c) -> {
				});
				assertTrue(context.actorExists(actorId));
			}

		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

	}

	@Test

	@UnitTestMethod(target = DataManagerContext.class, name = "addActor", args = { Consumer.class })
	public void testAddActor() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean actorWasAdded = new MutableBoolean();

		// there are no precondition tests

		// have the test agent show it exists and that other agents do not
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			c.addActor((c2) -> actorWasAdded.setValue(true));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		// show that the action plans got executed
		assertTrue(actorWasAdded.getValue());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "removeActor", args = { ActorId.class })
	public void testRemoveActor() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the resolver execute the precondition tests
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.removeActor(null));
			assertEquals(NucleusError.NULL_ACTOR_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.removeActor(new ActorId(1000)));
			assertEquals(NucleusError.UNKNOWN_ACTOR_ID, contractException.getErrorType());

		}));

		List<ActorId> addedActorIds = new ArrayList<>();

		// have the add a few agents
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			for (int i = 0; i < 10; i++) {
				ActorId actorId = c.addActor((c2) -> {
				});
				assertTrue(c.actorExists(actorId));
				addedActorIds.add(actorId);
			}
		}));

		// have the actor remove the added actors
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			for (ActorId actorId : addedActorIds) {
				c.removeActor(actorId);
				assertFalse(c.actorExists(actorId));
			}
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "halt", args = {})
	public void testHalt() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		Set<Integer> expectedValues = new LinkedHashSet<>();
		expectedValues.add(1);
		expectedValues.add(2);
		expectedValues.add(3);

		Set<Integer> actualValues = new LinkedHashSet<>();

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		// have the test agent execute several tasks, with one of the tasks
		// halting the simulation

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {
			actualValues.add(1);
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (context) -> {
			actualValues.add(2);
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(3, (context) -> {
			actualValues.add(3);
			context.halt();
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(4, (context) -> {
			actualValues.add(4);
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(5, (context) -> {
			actualValues.add(5);
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.setOutputConsumer(testOutputConsumer).build()//
				.execute();//

		// show that the plans that were scheduled after the halt did not
		// execute
		assertEquals(expectedValues, actualValues);

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "subscribe", args = { Class.class, BiConsumer.class })
	public void testSubscribe() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		MutableBoolean observed = new MutableBoolean();

		// have the resolver test preconditions for all the phases
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			ContractException contractException = assertThrows(ContractException.class,
					() -> c.subscribe(null, (c2, e) -> {
					}));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.subscribe(TestEvent1.class, null));
			assertEquals(NucleusError.NULL_EVENT_CONSUMER, contractException.getErrorType());

		}));

		// have the resolver subscribe for test events.
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			c.subscribe(TestEvent1.class, (c2, e) -> {
				observed.setValue(true);
			});

			ContractException contractException = assertThrows(ContractException.class,
					() -> c.subscribe(TestEvent1.class, (c2, e) -> {
					}));
			assertEquals(NucleusError.DUPLICATE_EVENT_SUBSCRIPTION, contractException.getErrorType());

		}));

		// create a data manager that will generate a test event

		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(1, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		/*
		 * show that the resolver engaged in the three event resolution phases in the
		 * proper order
		 */
		assertTrue(observed.getValue());

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "unsubscribe", args = { Class.class })
	public void testUnSubscribeToEvent() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the resolver test preconditions
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.unsubscribe(null));
			assertEquals(NucleusError.NULL_EVENT_CLASS, contractException.getErrorType());
		}));

		/*
		 * Create a container to count then number of times a subscription execution
		 * occured
		 */
		MutableInteger phaseExecutionCount = new MutableInteger();

		/*
		 * have the resolver subscribe to the test event and have it handle each type of
		 * event handling by incrementing a counter
		 */

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			c.subscribe(TestEvent1.class, (c2, e) -> {
				phaseExecutionCount.increment();
			});

		}));

		// create a data manager that will produce a test event
		pluginDataBuilder.addTestDataManager("generator", () -> new TestDataManager());
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(1, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		/*
		 * Show that the phaseExecutionCount is three after the the agent is done
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			assertEquals(1, phaseExecutionCount.getValue());
		}));

		// have the resolver unsubscribe
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(3, (c) -> {
			c.unsubscribe(TestEvent1.class);
		}));

		// have the data manager generate another test event
		pluginDataBuilder.addTestDataManagerPlan("generator", new TestDataManagerPlan(4, (c) -> {
			c.releaseObservationEvent(new TestEvent1());
		}));

		/*
		 * Show that the phaseExecutionCount is still three after the the agent is done
		 * and thus the resolver is no longer subscribed
		 */
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(5, (c) -> {
			assertEquals(1, phaseExecutionCount.getValue());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "subscribersExist", args = { Class.class })
	public void testSubscribersExist() {

		/*
		 * create a simple event label as a place holder -- all test events will be
		 * matched
		 */
		EventFilter<TestEvent1> eventFilter = EventFilter.builder(TestEvent1.class)//
				.build();//

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// add the first data manager

		/*
		 * Have the test resolver show that there are initially no subscribers to test
		 * events.
		 */
		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (c) -> {
			assertFalse(c.subscribersExist(TestEvent1.class));
		}));

		// create an agent and have it subscribe to test events at time 1

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			// subscribe to the event label
			c.subscribe(eventFilter, (c2, e) -> {
			});
		}));

		// show that the resolver now sees that there are subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(2, (c) -> {
			assertTrue(c.subscribersExist(TestEvent1.class));
		}));

		// have the agent unsubscribe
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			c.unsubscribe(eventFilter);
		}));

		// show that the resolver see no subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(4, (c) -> {
			assertFalse(c.subscribersExist(TestEvent1.class));
		}));

		// add a second data manager

		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());

		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(5, (c) -> {
			c.subscribe(TestEvent1.class, (c2, e) -> {
			});
		}));

		// show that the test resolver now sees that there are subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(6, (c) -> {
			assertTrue(c.subscribersExist(TestEvent1.class));
		}));

		// have the second data manager unsubscribe
		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(7, (c) -> {
			c.unsubscribe(TestEvent1.class);
		}));

		// show that dm1 now sees that there are no subscribers
		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(8, (c) -> {
			assertFalse(c.subscribersExist(TestEvent1.class));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the engine
		TestSimulation.builder().addPlugin(testPlugin).build().execute();
	}

	private static class ActorObservingDataManager extends TestDataManager {
		private List<Pair<Double, ActorId>> observedPairs;
		private DataManagerContext dataManagerContext;

		public ActorObservingDataManager(List<Pair<Double, ActorId>> observedPairs) {
			this.observedPairs = observedPairs;
		}

		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			this.dataManagerContext = dataManagerContext;
		}

		public void observe() {
			observedPairs.add(new Pair<>(dataManagerContext.getTime(), dataManagerContext.getActorId()));
		}
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getActorId", args = {})
	public void testGetActorId() {
		List<Pair<Double, ActorId>> expectedPairs = new ArrayList<>();
		List<Pair<Double, ActorId>> observedPairs = new ArrayList<>();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		double testTime = 1;
		// there are no precondition tests

		pluginDataBuilder.addTestDataManager("dm", () -> new ActorObservingDataManager(observedPairs));

		/*
		 * Have actors get their own actor ids and show that these ids match the
		 * expected values established duing the initialization of the TestActors.
		 */
		pluginDataBuilder.addTestActorPlan("Alpha", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(), c.getActorId()));
		}));

		pluginDataBuilder.addTestActorPlan("Beta", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(), c.getActorId()));
		}));

		pluginDataBuilder.addTestActorPlan("Gamma", new TestActorPlan(testTime++, (c) -> {
			c.getDataManager(ActorObservingDataManager.class).observe();
			expectedPairs.add(new Pair<>(c.getTime(), c.getActorId()));
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		TestSimulation.builder().addPlugin(testPlugin).build().execute();

		// show that the number of actor ids matches the number of actor aliases
		assertEquals(expectedPairs, observedPairs);
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getSimulationTime", args = { LocalDateTime.class })
	public void testGetSimulationTime() {

		// create some base dates to test

		LocalDate localDate = LocalDate.of(2020, 4, 1);

		List<LocalDateTime> localDateTimes = new ArrayList<>();

		localDateTimes.add(LocalDateTime.of(2023, 1, 10, 2, 17, 45));
		localDateTimes.add(LocalDateTime.of(2024, 6, 13, 8, 45, 37));
		localDateTimes.add(LocalDateTime.of(2020, 3, 15, 22, 13, 18));
		localDateTimes.add(LocalDateTime.of(2023, 12, 25, 15, 38, 19));

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (LocalDateTime localDateTime : localDateTimes) {
				assertEquals(simulationTimeConverter.getSimulationTime(localDateTime),
						c.getSimulationTime(localDateTime));
			}

		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getLocalDateTime", args = { double.class })
	public void testGetLocalDateTime() {

		LocalDate localDate = LocalDate.of(2020, 4, 1);
		List<Double> times = new ArrayList<>();
		times.add(-5.7);
		times.add(-2.234);
		times.add(0.0);
		times.add(3.9);
		times.add(137.765);
		times.add(4000.5437);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
					LocalDateTime.of(localDate, LocalTime.of(0, 0)));
			for (Double time : times) {
				assertEquals(simulationTimeConverter.getLocalDateTime(time), c.getLocalDateTime(time));
			}
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		SimulationState simulationState = SimulationState.builder().setBaseDate(localDate).build();
		TestSimulation.builder().setSimulationState(simulationState).addPlugin(testPlugin).build().execute();

	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "retrievePlans", args = {})
	public void testRetrievePlans() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// test preconditions
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (context) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> context.retrievePlans());
			assertEquals(NucleusError.PLANNING_QUEUE_ACTIVE, contractException.getErrorType());
		}));

		List<DataManagerPlan> dmPlans = new ArrayList<>();
		List<DataManagerPlan> expectedPlans = new ArrayList<>();
		double haltTime = 50;

		for (int i = 1; i <= 100; i++) {
			DataManagerPlan actorPlan = new ConsumerDataManagerPlan(i, (c) -> {
			});
			if (i > 50) {
				expectedPlans.add(actorPlan);
			}
			dmPlans.add(actorPlan);
		}
		/*
		 * Have the actor add a plan and show that that plan executes
		 */

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (context) -> {
			for (DataManagerPlan plan : dmPlans) {
				context.addPlan(plan);
			}

			context.subscribeToSimulationClose(c -> planRetrievalSimCloseSubscribe(c, expectedPlans));
		}));

		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager1());
		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.setSimulationHaltTime(haltTime).build()//
				.execute();//
	}

	private void planRetrievalSimCloseSubscribe(DataManagerContext context, List<DataManagerPlan> expectedPlans) {
		List<DataManagerPlan> plans = context.retrievePlans();

		assertEquals(expectedPlans.size(), plans.size());
		assertEquals(expectedPlans, plans);
	}

	@Test
	@UnitTestMethod(target = DataManagerContext.class, name = "getDataManagerId", args = {})
	public void testGetDataManagerId() {

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestDataManager("dm1", () -> new TestDataManager1());
		pluginDataBuilder.addTestDataManager("dm2", () -> new TestDataManager2());
		pluginDataBuilder.addTestDataManager("dm3", () -> new TestDataManager3());

		/*
		 * The TestPlugin already contains a data manager that will be added to the
		 * simulation ahead of the ones above, so the numbering will start with 1.
		 */

		pluginDataBuilder.addTestDataManagerPlan("dm1", new TestDataManagerPlan(0, (context) -> {
			assertEquals(new DataManagerId(1), context.getDataManagerId());
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm2", new TestDataManagerPlan(0, (context) -> {
			assertEquals(new DataManagerId(2), context.getDataManagerId());
		}));

		pluginDataBuilder.addTestDataManagerPlan("dm3", new TestDataManagerPlan(0, (context) -> {
			assertEquals(new DataManagerId(3), context.getDataManagerId());
		}));

		// build the plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		// run the simulation
		Simulation.builder()//
				.addPlugin(testPlugin)//
				.setOutputConsumer(testOutputConsumer).build()//
				.execute();//

	}
}
