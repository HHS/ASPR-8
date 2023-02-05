package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.TriConsumer;
import tools.annotations.UnitTestMethod;
import util.wrappers.MultiKey;
import util.wrappers.MutableDouble;
import util.wrappers.MutableInteger;

public class AT_ExperimentContext {

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getElapsedSeconds", args = {})
	public void testGetElapsedSeconds() {
		/*
		 * This is a very limited test of the elapsed time and requires a manual
		 * test to perform a more robust test.
		 */
		MutableDouble elapsedSeconds = new MutableDouble();
		Experiment	.builder()//
					.addExperimentContextConsumer(c -> {
						elapsedSeconds.setValue(c.getElapsedSeconds());
					}).build()//
					.execute();//

		// show that some time elapsed
		assertTrue(elapsedSeconds.getValue() > 0);
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {

		List<String> actualMetaData = new ArrayList<>();
		Experiment	.builder()//
					.addExperimentContextConsumer(c -> {
						actualMetaData.addAll(c.getExperimentMetaData());
					}).build()//
					.execute();//

		// show that the meta data is empty in the absence of dimensions
		assertTrue(actualMetaData.isEmpty());

		// create an experiment with two dimensions with some experiment meta
		// data
		Dimension dimension1 = Dimension.builder()//
										.addMetaDatum("A")//
										.addMetaDatum("B")//
										.addLevel((c) -> {
											List<String> result = new ArrayList<>();
											result.add("a");
											result.add("b");
											return result;
										})//
										.build();
		Dimension dimension2 = Dimension.builder()//
										.addMetaDatum("C")//
										.addMetaDatum("D")//
										.build();

		Dimension dimension3 = Dimension.builder()//
										.addMetaDatum("E")//
										.addMetaDatum("F")//
										.addLevel((c) -> {
											List<String> result = new ArrayList<>();
											result.add("e");
											result.add("f");
											return result;
										})//
										.build();

		Experiment	.builder()//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addDimension(dimension3)//
					.addExperimentContextConsumer(c -> {
						// this should execute exactly once since there is one
						// scenario
						actualMetaData.addAll(c.getExperimentMetaData());
					}).build()//
					.execute();//

		/*
		 * The meta data should be added in the order the meta data were added
		 * to the dimension and the order that the dimensions were added to the
		 * experiment. Empty dimensions should be ignored.
		 */
		List<String> expectedMetaData = new ArrayList<>();
		expectedMetaData.add("A");
		expectedMetaData.add("B");
		expectedMetaData.add("E");
		expectedMetaData.add("F");

		assertEquals(expectedMetaData, actualMetaData);

		/*
		 * Show that a different ordering of the dimensions works as expected
		 */

		actualMetaData.clear();
		expectedMetaData.clear();
		expectedMetaData.add("E");
		expectedMetaData.add("F");
		expectedMetaData.add("A");
		expectedMetaData.add("B");

		Experiment	.builder()//
					.addDimension(dimension3)//
					.addDimension(dimension2)//
					.addDimension(dimension1)//
					.addExperimentContextConsumer(c -> {
						actualMetaData.addAll(c.getExperimentMetaData());
					}).build()//
					.execute();//

		assertEquals(expectedMetaData, actualMetaData);

	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getScenarioCount", args = {})
	public void testGetScenarioCount() {

		// create an experiment that has no dimension and show that the scenario
		// count is 1
		MutableInteger scenarioCount = new MutableInteger();

		Experiment	.builder()//
					.addExperimentContextConsumer((c) -> {
						scenarioCount.setValue(c.getScenarioCount());
					})//
					.build()//
					.execute();//
		assertEquals(1, scenarioCount.getValue());

		// create an experiment with two dimensions, having 10 and 7 levels each
		Dimension.Builder dimBuilder = Dimension.builder().addMetaDatum("A").addMetaDatum("B");
		IntStream.range(0, 10).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Integer.toString(3 * i));
				return result;
			});
		});
		Dimension dimension1 = dimBuilder.build();

		dimBuilder.addMetaDatum("X");
		IntStream.range(0, 7).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i * i));
				return result;
			});
		});
		Dimension dimension2 = dimBuilder.build();

		// execute the experiment
		Experiment	.builder()//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer((c) -> {
						scenarioCount.setValue(c.getScenarioCount());
					})//
					.build()//
					.execute();//

		// show that the experiment has the expected number of scenarios
		assertEquals(70, scenarioCount.getValue());
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getScenarioMetaData", args = { int.class })
	public void testGetScenarioMetaData() {

		// create an experiment with two dimensions with some experiment meta
		// data
		Dimension.Builder dimBuilder = Dimension.builder().addMetaDatum("A").addMetaDatum("B");
		IntStream.range(0, 10).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Integer.toString(3 * i));
				return result;
			});
		});
		Dimension dimension1 = dimBuilder.build();

		dimBuilder.addMetaDatum("X");
		IntStream.range(0, 7).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i * i));
				return result;
			});
		});
		Dimension dimension2 = dimBuilder.build();

		/*
		 * Create a plugin that will put a single actor into the simulation and
		 * have that actor release an object to output
		 */
		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										c2.releaseOutput(new Object());
									});//
								}).build();//

		/*
		 * Execute an experiment from the dimension and an output handler that
		 * will be stimulated exactly once per scenario and record the
		 * corresponding scenario meta data
		 */

		// make a container for the collected scenario id and scenario meta data
		// information
		Set<MultiKey> combinedMetaData = new LinkedHashSet<>();
		Experiment	.builder()//
					.addPlugin(plugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer(c -> {
						c.subscribeToOutput(Object.class, (c2, s, e) -> {
							MultiKey.Builder builder = MultiKey.builder();
							builder.addKey(s);
							for (String metaDatum : c2.getScenarioMetaData(s).get()) {
								builder.addKey(Integer.parseInt(metaDatum));
							}
							combinedMetaData.add(builder.build());
						});
					})//
					.build()//
					.execute();//

		/*
		 * There is no guarantee provided by the experiment that any particular
		 * scenario id will be associated with any particular values from the
		 * dimensions.
		 */

		// First, we will show that the scenario ids were 0 to 69, inclusive
		assertEquals(70, combinedMetaData.size());
		Set<Integer> observedScenarioIds = new LinkedHashSet<>();
		for (MultiKey multiKey : combinedMetaData) {
			Integer scenarioId = multiKey.getKey(0);
			assertTrue(scenarioId >= 0);
			assertTrue(scenarioId < 70);
			observedScenarioIds.add(scenarioId);
		}
		assertEquals(70, observedScenarioIds.size());

		// Next we will create a container for the expected meta data
		Set<MultiKey> expectedMetaData = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 7; j++) {
				expectedMetaData.add(new MultiKey(i, 3 * i, j * j));
			}
		}

		// We derive the actual meta data by dropping the scenario ids from the
		// combinedMetaData
		Set<MultiKey> actualMetaData = new LinkedHashSet<>();
		for (MultiKey multiKey : combinedMetaData) {
			actualMetaData.add(new MultiKey(multiKey.getKey(1), multiKey.getKey(2), multiKey.getKey(3)));
		}

		assertEquals(expectedMetaData, actualMetaData);

	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getScenarios", args = { ScenarioStatus.class })
	public void testGetScenarios() {
		// create an experiment with two dimensions with some experiment meta
		// data
		Dimension.Builder dimBuilder = Dimension.builder().addMetaDatum("A").addMetaDatum("B");
		IntStream.range(0, 10).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Integer.toString(3 * i));
				return result;
			});
		});
		Dimension dimension1 = dimBuilder.build();

		dimBuilder.addMetaDatum("X");
		IntStream.range(0, 7).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i * i));
				return result;
			});
		});
		Dimension dimension2 = dimBuilder.build();

		/*
		 * Create a plugin that will put a single actor into the simulation and
		 * have that actor release the scenario id to output
		 */
		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										c2.releaseOutput(new Object());
									});//
								}).build();//

		/*
		 * Execute an experiment from the dimension and an output handler that
		 * will be stimulated exactly once per scenario and record the scenario
		 * id
		 */

		// make a container for the collected scenario id values
		Set<Integer> actualScenarioIds = new LinkedHashSet<>();

		// execute the experiment
		Experiment	.builder()//
					.addPlugin(plugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addExperimentContextConsumer(c -> {
						c.subscribeToOutput(Object.class, (c2, s, e) -> {
							actualScenarioIds.add(s);
						});
					}).build()//
					.execute();//

		// show that the scenarios were as expected
		Set<Integer> expectedScenarioIds = new LinkedHashSet<>();
		for (int i = 0; i < 70; i++) {
			expectedScenarioIds.add(i);
		}

		assertEquals(expectedScenarioIds, actualScenarioIds);

	}

	/*
	 * An enum support scenario status tests
	 */
	private static enum StatusPhase {
		EXP_OPEN, EXP_CLOSE, SIM_OPEN, SIM_CLOSE;
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getScenarioStatus", args = { int.class })
	public void testGetScenarioStatus() {

		/*
		 * Create a container for the scenario id, ASSUMING that the experiment
		 * executes scenarios in scenario id order, starting from zero. The
		 * plugin instance that initializes the simulation does not have access
		 * to the scenario id.
		 */
		MutableInteger expectedScenarioId = new MutableInteger();

		// create a plugin that will cause an exception to be thrown during
		// plugin initialization for even-numbered scenarios.
		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									int value = expectedScenarioId.getValue();
									expectedScenarioId.increment();
									if (value % 2 == 0) {
										throw new RuntimeException();
									}
								}).build();//

		// Create a dimension with integer values. We expect these values to
		// match the scenario id.
		Dimension.Builder dimBuilder = Dimension.builder().addMetaDatum("value");
		IntStream.range(0, 10).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				return result;
			});
		});
		Dimension dimension = dimBuilder.build();

		// create a container for observed scenario status values
		Set<MultiKey> observedScenarioStatusInfo = new LinkedHashSet<>();

		/*
		 * Execute an experiment that uses the dimension and plugin. Add an
		 * output handler that will record the status of each scenario at each
		 * of the phases of the simulation and experiment. Turn off console
		 * notification of scenario failures.
		 */
		Experiment	.builder()//
					.addDimension(dimension)//
					.addPlugin(plugin)//
					.addExperimentContextConsumer((c) -> {
						c.subscribeToExperimentOpen((c2) -> {
							int scenarioCount = c2.getScenarioCount();
							for (int scenarioId = 0; scenarioId < scenarioCount; scenarioId++) {
								ScenarioStatus scenarioStatus = c2.getScenarioStatus(scenarioId).get();
								observedScenarioStatusInfo.add(new MultiKey(StatusPhase.EXP_OPEN, scenarioId, scenarioStatus));
							}
						});
						c.subscribeToSimulationOpen((c2, s) -> {
							ScenarioStatus scenarioStatus = c2.getScenarioStatus(s).get();
							observedScenarioStatusInfo.add(new MultiKey(StatusPhase.SIM_OPEN, s, scenarioStatus));
						});
						c.subscribeToSimulationClose((c2, s) -> {
							ScenarioStatus scenarioStatus = c2.getScenarioStatus(s).get();
							observedScenarioStatusInfo.add(new MultiKey(StatusPhase.SIM_CLOSE, s, scenarioStatus));
						});
						c.subscribeToExperimentClose((c2) -> {
							int scenarioCount = c2.getScenarioCount();
							Map<ScenarioStatus, Set<Integer>> scenariosByStatus = new LinkedHashMap<>();
							for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
								scenariosByStatus.put(scenarioStatus, new LinkedHashSet<>());
							}
							for (int scenarioId = 0; scenarioId < scenarioCount; scenarioId++) {
								ScenarioStatus scenarioStatus = c2.getScenarioStatus(scenarioId).get();
								observedScenarioStatusInfo.add(new MultiKey(StatusPhase.EXP_CLOSE, scenarioId, scenarioStatus));
								scenariosByStatus.get(scenarioStatus).add(scenarioId);
							}

							/*
							 * show that the scenarios retrieved by status match
							 * expectations
							 */
							for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
								Set<Integer> actualScenarios = new LinkedHashSet<>(c2.getScenarios(scenarioStatus));
								assertEquals(scenariosByStatus.get(scenarioStatus), actualScenarios);
							}

						});
					})//
					.setHaltOnException(false)//
					.build()//
					.execute();//

		/*
		 * Build the expected observations
		 */

		Set<MultiKey> expectedScenarioStatusInfo = new LinkedHashSet<>();
		for (int i = 0; i < dimension.size(); i++) {
			ScenarioStatus finalStatus = ScenarioStatus.SUCCEDED;
			if (i % 2 == 0) {
				finalStatus = ScenarioStatus.FAILED;
			}
			expectedScenarioStatusInfo.add(new MultiKey(StatusPhase.EXP_OPEN, i, ScenarioStatus.READY));
			expectedScenarioStatusInfo.add(new MultiKey(StatusPhase.SIM_OPEN, i, ScenarioStatus.RUNNING));
			expectedScenarioStatusInfo.add(new MultiKey(StatusPhase.SIM_CLOSE, i, finalStatus));
			expectedScenarioStatusInfo.add(new MultiKey(StatusPhase.EXP_CLOSE, i, finalStatus));
		}

		// show that the observations of scenario status match expectations.
		assertEquals(expectedScenarioStatusInfo, observedScenarioStatusInfo);

	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getStatusCount", args = { ScenarioStatus.class })
	public void testGetStatusCount() {
		// covered by the test method : testScenarioStatus()
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "subscribeToExperimentClose", args = { Consumer.class })
	public void testSubscribeToExperimentClose() {
		/*
		 * Run an experiment that has several clients of the experiment context
		 * subscribe to experiment open and show that each one is stimulated
		 * correctly.
		 */
		MutableInteger simulationCloseExperimentCount = new MutableInteger();
		int expectedCloseExperimentCount = 5;

		/*
		 * Begin building the experiment
		 */
		Experiment.Builder builder = Experiment.builder();//

		/*
		 * Add the output handlers that will subscribe to the close of the
		 * experiment and respond by incrementing a counter
		 */
		for (int i = 0; i < expectedCloseExperimentCount; i++) {
			builder.addExperimentContextConsumer((c) -> {
				c.subscribeToExperimentClose((c2) -> {
					simulationCloseExperimentCount.increment();
				});
			});//
		}

		// execute the experiment
		builder	.build()//
				.execute();//

		// show the subscribers did observe the opening of the simulation
		assertEquals(expectedCloseExperimentCount, simulationCloseExperimentCount.getValue());
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "subscribeToExperimentOpen", args = { Consumer.class })
	public void testSubscribeToExperimentOpen() {
		/*
		 * Run an experiment that has several clients of the experiment context
		 * subscribe to experiment open and show that each one is stimulated
		 * correctly.
		 */
		MutableInteger simulationOpenExperimentCount = new MutableInteger();
		int expectedOpenExperimentCount = 5;

		/*
		 * Begin building the experiment
		 */
		Experiment.Builder builder = Experiment.builder();//

		/*
		 * Add the output handlers that will subscribe to the opening of the
		 * experiment and respond by incrementing a counter
		 */
		for (int i = 0; i < expectedOpenExperimentCount; i++) {
			builder.addExperimentContextConsumer((c) -> {
				c.subscribeToExperimentOpen((c2) -> {
					simulationOpenExperimentCount.increment();
				});
			});//
		}

		// execute the experiment
		builder	.build()//
				.execute();//

		// show the subscribers did observe the opening of the simulation
		assertEquals(expectedOpenExperimentCount, simulationOpenExperimentCount.getValue());
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "subscribeToOutput", args = { Class.class, TriConsumer.class })
	public void testSubscribeToOutput() {

		/*
		 * Create a plugin that will add a single actor that in turn releases a
		 * few integers, strings and doubles as output
		 */
		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										c2.releaseOutput(45);
										c2.releaseOutput("alpha");
										c2.releaseOutput(16);
										c2.releaseOutput("beta");
										c2.releaseOutput(2.0345);
									});
								})//
								.build();//

		/*
		 * Execute an experiment having two output handlers for integers and
		 * strings, but not doubles
		 */
		Set<MultiKey> observedOutput = new LinkedHashSet<>();
		Experiment	.builder()//
					.addPlugin(plugin)//
					.addExperimentContextConsumer((c) -> {
						c.subscribeToOutput(Integer.class, (c2, s, o) -> {
							observedOutput.add(new MultiKey("int handler", o));
						});
					})//
					.addExperimentContextConsumer((c) -> {
						c.subscribeToOutput(String.class, (c2, s, o) -> {
							observedOutput.add(new MultiKey("string handler", o));
						});
					})//
					.build()//
					.execute();//

		// show that the handlers received the expected output
		Set<MultiKey> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add(new MultiKey("int handler", 45));
		expectedOutput.add(new MultiKey("int handler", 16));
		expectedOutput.add(new MultiKey("string handler", "alpha"));
		expectedOutput.add(new MultiKey("string handler", "beta"));

		assertEquals(expectedOutput, observedOutput);

	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "subscribeToSimulationClose", args = { BiConsumer.class })
	public void testSubscribeToSimulationClose() {
		/*
		 * Run an experiment that has several clients of the experiment context
		 * subscribe to simulation close and show that each one is stimulated
		 * correctly.
		 */
		MutableInteger simulationCloseObservationCount = new MutableInteger();
		int expectedCloseObservationCount = 5;

		/*
		 * Begin building the experiment
		 */
		Experiment.Builder builder = Experiment.builder();//

		/*
		 * Add the output handlers that will subscribe to the opening of the
		 * simulation and respond by incrementing a counter
		 */
		for (int i = 0; i < expectedCloseObservationCount; i++) {
			builder.addExperimentContextConsumer((c) -> {
				c.subscribeToSimulationClose((c2, s) -> {
					simulationCloseObservationCount.increment();
				});
			});//
		}

		// execute the experiment
		builder	.build()//
				.execute();//

		// show the subscribers did observe the opening of the simulation
		assertEquals(expectedCloseObservationCount, simulationCloseObservationCount.getValue());
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "subscribeToSimulationOpen", args = { BiConsumer.class })
	public void testSubscribeToSimulationOpen() {

		/*
		 * Run an experiment that has several clients of the experiment context
		 * subscribe to simulation open and show that each one is stimulated
		 * correctly.
		 */
		MutableInteger simulationOpenObservationCount = new MutableInteger();
		int expectedOpenObservationCount = 5;

		/*
		 * Begin building the experiment
		 */
		Experiment.Builder builder = Experiment.builder();//

		/*
		 * Add the output handlers that will subscribe to the opening of the
		 * simulation and respond by incrementing a counter
		 */
		for (int i = 0; i < expectedOpenObservationCount; i++) {
			builder.addExperimentContextConsumer((c) -> {
				c.subscribeToSimulationOpen((c2, s) -> {
					simulationOpenObservationCount.increment();
				});
			});//
		}

		// execute the experiment
		builder	.build()//
				.execute();//

		// show the subscribers did observe the opening of the simulation
		assertEquals(expectedOpenObservationCount, simulationOpenObservationCount.getValue());
	}

	@Test
	@UnitTestMethod(target = ExperimentContext.class, name = "getScenarioFailureCause", args = { int.class })
	public void testGetScenarioFailureCause() {
		RuntimeException e = new RuntimeException();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			throw e;
		}));
		Plugin testPlugin = TestPlugin.getTestPlugin(pluginDataBuilder.build());

		Consumer<ExperimentContext> experimentContestConsumer = (c) -> {
			c.subscribeToExperimentClose((c2) -> {
				Optional<Exception> optional = c2.getScenarioFailureCause(0);
				assertTrue(optional.isPresent());
				Exception e2 = optional.get();
				assertEquals(e, e2);
			});
		};

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addExperimentContextConsumer(experimentContestConsumer)//
					.setHaltOnException(false)//
					.build()//
					.execute();

	}

}
