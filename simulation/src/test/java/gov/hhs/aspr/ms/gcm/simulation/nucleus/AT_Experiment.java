package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;
import gov.hhs.aspr.ms.util.wrappers.MutableObject;

public class AT_Experiment {

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addDimension", args = { Dimension.class })
	public void testAddDimension() {

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("Alpha")//
				.addValue("Level_0", (context) -> {
					List<String> result = new ArrayList<>();
					result.add("alpha1");
					return result;
				})//
				.addValue("Level_1", (context) -> {
					List<String> result = new ArrayList<>();
					result.add("alpha2");
					return result;
				})//
				.build();//
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("Beta")//
				.addValue("Level_0", (context) -> {
					List<String> result = new ArrayList<>();
					result.add("beta1");
					return result;
				})//
				.addValue("Level_1", (context) -> {
					List<String> result = new ArrayList<>();
					result.add("beta2");
					return result;
				})//
				.addValue("Level_2", (context) -> {
					List<String> result = new ArrayList<>();
					result.add("beta3");
					return result;
				})//
				.build();//
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		Set<MultiKey> expectedExperimentInstances = new LinkedHashSet<>();
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta1"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta1"));
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta2"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta2"));
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta3"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta3"));

		Set<MultiKey> actualExperimentInstances = new LinkedHashSet<>();

		Plugin plugin = Plugin.builder()//
				.setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
					c.addActor((c2) -> {
						c2.releaseOutput(new Object());
					});
				}).build();//

		Experiment.builder()//
				.addExperimentContextConsumer(c -> {
					c.subscribeToOutput(Object.class, (c2, s, e) -> {
						List<String> scenarioMetaData = c2.getScenarioMetaData(s);
						MultiKey.Builder builder = MultiKey.builder();
						for (String scenarioMetaDatum : scenarioMetaData) {
							builder.addKey(scenarioMetaDatum);
						}
						actualExperimentInstances.add(builder.build());
					});
				}).addPlugin(plugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.build()//
				.execute();//

		assertEquals(expectedExperimentInstances, actualExperimentInstances);

	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addExperimentContextConsumer", args = { Consumer.class })
	public void testAddExperimentContextConsumer() {

		/*
		 * Show that an output handler receives data released by the simulation by
		 * adding a few actors that release data. Add output handlers that show that the
		 * output is correctly directed. Note the use of overlapping types for the
		 * output consumers and that the output type may be a subclass of the consumer's
		 * type.
		 */

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("Integer Actor", new TestActorPlan(0, (c) -> {
			c.releaseOutput(56);
		}));

		pluginBuilder.addTestActorPlan("String Actor", new TestActorPlan(0, (c) -> {
			c.releaseOutput("string 1");
		}));

		pluginBuilder.addTestActorPlan("Double Actor", new TestActorPlan(0, (c) -> {
			c.releaseOutput(34.5);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		Set<MultiKey> actualOutput = new LinkedHashSet<>();

		Consumer<ExperimentContext> integerOutputHandler = (c) -> {
			c.subscribeToOutput(Integer.class, (c2, s, e) -> {
				MultiKey.Builder builder = MultiKey.builder();
				List<String> metaData = c2.getScenarioMetaData(s);
				builder.addKey("Integer Output Handler");
				for (String metaDatum : metaData) {
					builder.addKey(metaDatum);
				}
				builder.addKey(s);
				builder.addKey(e);
				actualOutput.add(builder.build());
			});
		};

		Consumer<ExperimentContext> stringOutputHandler = (c) -> {
			c.subscribeToOutput(String.class, (c2, s, e) -> {
				actualOutput.add(new MultiKey("String Output Handler", s, e));
			});
		};

		Consumer<ExperimentContext> doubleOutputHandler = (c) -> {
			c.subscribeToOutput(Double.class, (c2, s, e) -> {
				actualOutput.add(new MultiKey("Double Output Handler", s, e));
			});
		};

		Consumer<ExperimentContext> numberOutputHandler = (c) -> {
			c.subscribeToOutput(Number.class, (c2, s, e) -> {
				actualOutput.add(new MultiKey("Number Output Handler", s, e));
			});
		};

		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
				.addMetaDatum("dim1")//
				.addValue("Level_0", (tmap) -> {
					List<String> result = new ArrayList<>();
					result.add("var_1_1");
					return result;
				})//
				.addValue("Level_1", (tmap) -> {
					List<String> result = new ArrayList<>();
					result.add("var_1_2");
					return result;
				})//
				.build();//
		FunctionalDimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
				.addMetaDatum("dim2")//
				.addValue("Level_0", (tmap) -> {
					List<String> result = new ArrayList<>();
					result.add("var_2_1");
					return result;
				})//
				.addValue("Level_1", (tmap) -> {
					List<String> result = new ArrayList<>();
					result.add("var_2_2");
					return result;
				})//
				.build();//
		FunctionalDimension dimension2 = new FunctionalDimension(dimensionData2);

		Experiment.builder()//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.addExperimentContextConsumer(integerOutputHandler)//
				.addExperimentContextConsumer(stringOutputHandler)//
				.addExperimentContextConsumer(doubleOutputHandler)//
				.addExperimentContextConsumer(numberOutputHandler)//
				.addPlugin(testPlugin)//
				.build()//
				.execute();//

		Set<MultiKey> expectedOutput = new LinkedHashSet<>();

		expectedOutput.add(new MultiKey("Integer Output Handler", "var_1_1", "var_2_1", 0, 56));
		expectedOutput.add(new MultiKey("Number Output Handler", 0, 56));
		expectedOutput.add(new MultiKey("String Output Handler", 0, "string 1"));
		expectedOutput.add(new MultiKey("Double Output Handler", 0, 34.5));
		expectedOutput.add(new MultiKey("Number Output Handler", 0, 34.5));
		expectedOutput.add(new MultiKey("Integer Output Handler", "var_1_2", "var_2_1", 1, 56));
		expectedOutput.add(new MultiKey("Number Output Handler", 1, 56));
		expectedOutput.add(new MultiKey("String Output Handler", 1, "string 1"));
		expectedOutput.add(new MultiKey("Double Output Handler", 1, 34.5));
		expectedOutput.add(new MultiKey("Number Output Handler", 1, 34.5));
		expectedOutput.add(new MultiKey("Integer Output Handler", "var_1_1", "var_2_2", 2, 56));
		expectedOutput.add(new MultiKey("Number Output Handler", 2, 56));
		expectedOutput.add(new MultiKey("String Output Handler", 2, "string 1"));
		expectedOutput.add(new MultiKey("Double Output Handler", 2, 34.5));
		expectedOutput.add(new MultiKey("Number Output Handler", 2, 34.5));
		expectedOutput.add(new MultiKey("Integer Output Handler", "var_1_2", "var_2_2", 3, 56));
		expectedOutput.add(new MultiKey("Number Output Handler", 3, 56));
		expectedOutput.add(new MultiKey("String Output Handler", 3, "string 1"));
		expectedOutput.add(new MultiKey("Double Output Handler", 3, 34.5));
		expectedOutput.add(new MultiKey("Number Output Handler", 3, 34.5));

		assertEquals(expectedOutput, actualOutput);

	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addPlugin", args = { Plugin.class })
	public void testAddPlugin() {

		// show that several plugins can be added and that they execute in the
		// correct order

		// create plugin ids
		PluginId aId = new SimplePluginId("plugin A");
		PluginId bId = new SimplePluginId("plugin B");
		PluginId cId = new SimplePluginId("plugin C");

		/*
		 * Build the expected order of initialization based on the dependencies between
		 * the plugins
		 */
		List<PluginId> expectedExecutedPlugins = new ArrayList<>();
		expectedExecutedPlugins.add(cId);
		expectedExecutedPlugins.add(bId);
		expectedExecutedPlugins.add(aId);

		// build a container for the actual initialization order
		List<PluginId> actualExecutedPlugins = new ArrayList<>();

		// create the three plugins with A depending on B and C and B depending
		// on C alone.
		Plugin pluginA = Plugin.builder()//
				.setPluginId(aId)//
				.addPluginDependency(cId)//
				.addPluginDependency(bId)//
				.setInitializer((c) -> {
					actualExecutedPlugins.add(aId);
				}).build();//

		Plugin pluginB = Plugin.builder()//
				.setPluginId(bId)//
				.addPluginDependency(cId)//
				.setInitializer((c) -> {
					actualExecutedPlugins.add(bId);
				}).build();//

		Plugin pluginC = Plugin.builder()//
				.setPluginId(cId)//
				.setInitializer((c) -> {
					actualExecutedPlugins.add(cId);
				}).build();//

		// create the simulation
		Experiment.builder()//
				.addPlugin(pluginA)//
				.addPlugin(pluginB)//
				.addPlugin(pluginC)//
				.build()//
				.execute();//

		// show that the plugins initialized in the correct order
		assertEquals(expectedExecutedPlugins, actualExecutedPlugins);
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "build", args = {})
	public void testBuild() {
		// show that an empty experiment will executed
		Experiment experiment = Experiment.builder().build();
		experiment.execute();

		// Other aspects of the build are covered in the remaining capability
		// specific tests
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setExperimentParameterData", args = {
			ExperimentParameterData.class }, tags = { UnitTag.INCOMPLETE })
	public void testSetExperimentParameterData() {
		testSetHaltOnException();
		testThreadCount();
	}

	private void testThreadCount() {

		// add two dimensions that will cause the experiment to execute 40
		// simulation instances
		FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder().addMetaDatum("alpha");//
		IntStream.range(0, 5).forEach((i) -> {
			dimDataBuilder.addValue("Level_" + i, (context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				return result;
			});
		});

		FunctionalDimensionData dimensionData1 = dimDataBuilder.build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData.Builder dimDataBuilder2 = FunctionalDimensionData.builder();
		IntStream.range(0, 8).forEach((i) -> {
			dimDataBuilder2.addValue("Level_" + i, (context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				return result;
			});
		});

		FunctionalDimensionData dimensionData2 = dimDataBuilder2.addMetaDatum("beta").build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		/*
		 * Create a thread safe set to record the thread ids that are used by each
		 * simulation
		 */
		Set<Long> threadIds = Collections.synchronizedSet(new LinkedHashSet<>());

		/*
		 * Add a plugin that will add an actor that records the thread id of the
		 * simulation running that actor
		 */
		Plugin plugin = Plugin.builder()//
				.setPluginId(new SimplePluginId("plugin"))//
				.setInitializer((c) -> {
					c.addActor((c2) -> {
						threadIds.add(Thread.currentThread().getId());
					});
				}).build();//
		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(6)//
				.build();

		// Run the experiment using several threads
		Experiment.builder()//
				.addPlugin(plugin)//
				.addDimension(dimension1)//
				.addDimension(dimension2)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();//

		// We show that more than one thread was used. It is very difficult,
		// especially with simulation instances that run very quickly, to reason
		// out the number of threads that will be allocated or reused. The best
		// we can do is show that the main thread was not used for any
		// simulation instance. In practice, only a long running manual test can
		// demonstrate that the experiment thread management is working as
		// intended.
		assertFalse(threadIds.contains(Thread.currentThread().getId()));
	}

	@Test
	@UnitTestMethod(target = Experiment.class, name = "builder", args = {})
	public void testBuilder() {
		// show that a builder is returned
		assertNotNull(Experiment.builder());
	}

	@Test
	@UnitTestMethod(target = Experiment.class, name = "execute", args = {})
	public void testExecute() {
		// Covered by remaining tests that execute the experiment.
	}

	private void testSetHaltOnException() {
		testSetHaltOnException_Explicit_True();
		testSetHaltOnException_Explicit_False();
		testSetHaltOnException_Implicit();
	}

	@Test
	private void testSetHaltOnException_Explicit_True() {
		// This test will run the experiment two times in single thread mode

		// we create a counter that will be incremented by each actor on
		// initialization
		MutableInteger actorInitializationCounter = new MutableInteger();
		MutableObject<ExperimentContext> experimentContext = new MutableObject<>();

		// we create a dimension with two levels
		FunctionalDimensionData dimensionData = FunctionalDimensionData.builder()//
				.addValue("Level_0", (c) -> new ArrayList<>())//
				.addValue("Level_1", (c) -> new ArrayList<>())//
				.build();
		Dimension dimension = new FunctionalDimension(dimensionData);

		// we create a plugin that will instantiate three actors, with the
		// second actor throwing a runtime exception
		Plugin plugin = Plugin.builder().setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
				throw new RuntimeException();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
		}).build();

		Consumer<ExperimentContext> experimentContextConsumer = (c) -> {
			experimentContext.setValue(c);
		};

		/*
		 * By setting the haltOnException to true, the simulation should execute both
		 * scenarios, with each scenario executing only two of the actors before the
		 * simulation fails. Thus we expect that the counter will be equal to four after
		 * the experiment executes and that there will be no exception bubbling out of
		 * the execute() invocation
		 */

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder().setHaltOnException(true)
				.build();

		// Explicitly setting the haltOnException to true
		assertThrows(RuntimeException.class, () -> Experiment.builder()//
				.addDimension(dimension)//
				.addPlugin(plugin)//
				.addExperimentContextConsumer(experimentContextConsumer)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute());

		// since the experiment should halt on the first failure, we expect only
		// two of the actors to have initialized
		assertEquals(2, actorInitializationCounter.getValue());

		// show that only the first scenario executed and failed. The second
		// scenario should still be in READY status.
		assertEquals(ScenarioStatus.FAILED, experimentContext.getValue().getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.READY, experimentContext.getValue().getScenarioStatus(1).get());
	}

	private void testSetHaltOnException_Explicit_False() {
		// This test will run the experiment two times in single thread mode

		// we create a counter that will be incremented by each actor on
		// initialization
		MutableInteger actorInitializationCounter = new MutableInteger();
		MutableObject<ExperimentContext> experimentContext = new MutableObject<>();

		// we create a dimension with two levels
		FunctionalDimensionData dimensionData = FunctionalDimensionData.builder()//
				.addValue("Level_0", (c) -> new ArrayList<>())//
				.addValue("Level_1", (c) -> new ArrayList<>())//
				.build();
		Dimension dimension = new FunctionalDimension(dimensionData);

		// we create a plugin that will instantiate three actors, with the
		// second actor throwing a runtime exception
		Plugin plugin = Plugin.builder().setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
				throw new RuntimeException();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
		}).build();

		Consumer<ExperimentContext> experimentContextConsumer = (c) -> {
			experimentContext.setValue(c);
		};

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setHaltOnException(false)//
				.build();

		// Explicitly setting the haltOnException to false
		Experiment.builder()//
				.addDimension(dimension)//
				.addPlugin(plugin)//
				.addExperimentContextConsumer(experimentContextConsumer)//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();
		assertEquals(4, actorInitializationCounter.getValue());

		// show that both scenarios executed and failed
		assertEquals(ScenarioStatus.FAILED, experimentContext.getValue().getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.FAILED, experimentContext.getValue().getScenarioStatus(1).get());

	}

	private void testSetHaltOnException_Implicit() {
		// This test will run the experiment two times in single thread mode

		// we create a counter that will be incremented by each actor on
		// initialization
		MutableInteger actorInitializationCounter = new MutableInteger();
		MutableObject<ExperimentContext> experimentContext = new MutableObject<>();

		// we create a dimension with two levels
		FunctionalDimensionData dimensionData = FunctionalDimensionData.builder()//
				.addValue("Level_0", (c) -> new ArrayList<>())//
				.addValue("Level_1", (c) -> new ArrayList<>())//
				.build();
		Dimension dimension = new FunctionalDimension(dimensionData);

		// we create a plugin that will instantiate three actors, with the
		// second actor throwing a runtime exception
		Plugin plugin = Plugin.builder().setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
				throw new RuntimeException();
			});
			c.addActor((c2) -> {
				actorInitializationCounter.increment();
			});
		}).build();

		Consumer<ExperimentContext> experimentContextConsumer = (c) -> {
			experimentContext.setValue(c);
		};

		/*
		 * By setting the haltOnException to true, the simulation should execute both
		 * scenarios, with each scenario executing only two of the actors before the
		 * simulation fails. Thus we expect that the counter will be equal to four after
		 * the experiment executes and that there will be no exception bubbling out of
		 * the execute() invocation
		 */

		// Implicitly setting the haltOnException to true
		assertThrows(RuntimeException.class, () -> Experiment.builder()//
				.addDimension(dimension)//
				.addPlugin(plugin)//
				.addExperimentContextConsumer(experimentContextConsumer)//
				.build()//
				.execute());

		// since the experiment should halt on the first failure, we expect only
		// two of the actors to have initialized
		assertEquals(2, actorInitializationCounter.getValue());

		// show that only the first scenario executed and failed. The second
		// scenario should still be in READY status.
		assertEquals(ScenarioStatus.FAILED, experimentContext.getValue().getScenarioStatus(0).get());
		assertEquals(ScenarioStatus.READY, experimentContext.getValue().getScenarioStatus(1).get());

	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setSimulationState", args = { SimulationState.class })
	public void testSetSimulationState() {

		//set a time for the simulation to start
		double expectedStartTime = 456.789;
		
		//create the simulation state that will dictate the start time
		SimulationState simulationState = SimulationState.builder()//
				.setStartTime(expectedStartTime)//
				.build();

		/*
		 * create a plugin that contains an actor. This actor will confirm that the
		 * simulation starts at the expected time.
		 */
		Plugin plugin = Plugin.builder().setPluginId(new SimplePluginId("plugin id")).setInitializer((c) -> {
			c.addActor((c2) -> {
				assertEquals(expectedStartTime, c2.getTime());
			});
		}).build();

		// execute the experiment
		Experiment.builder()//
				.addPlugin(plugin)//
				.setSimulationState(simulationState)//
				.build()//
				.execute();//

	}
}
