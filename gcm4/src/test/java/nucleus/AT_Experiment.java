package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

@UnitTest(target = Experiment.class)
public class AT_Experiment {

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addDimension", args = { Dimension.class })
	public void testAddDimension() {

		Dimension dimension1 = Dimension.builder()//
										.addMetaDatum("Alpha")//
										.addLevel((context) -> {
											List<String> result = new ArrayList<>();
											result.add("alpha1");
											return result;
										})//
										.addLevel((context) -> {
											List<String> result = new ArrayList<>();
											result.add("alpha2");
											return result;
										})//
										.build();//

		Dimension dimension2 = Dimension.builder()//
										.addMetaDatum("Beta")//
										.addLevel((context) -> {
											List<String> result = new ArrayList<>();
											result.add("beta1");
											return result;
										})//
										.addLevel((context) -> {
											List<String> result = new ArrayList<>();
											result.add("beta2");
											return result;
										})//
										.addLevel((context) -> {
											List<String> result = new ArrayList<>();
											result.add("beta3");
											return result;
										})//
										.build();//

		Set<MultiKey> expectedExperimentInstances = new LinkedHashSet<>();
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta1"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta1"));
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta2"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta2"));
		expectedExperimentInstances.add(new MultiKey("alpha1", "beta3"));
		expectedExperimentInstances.add(new MultiKey("alpha2", "beta3"));

		Set<MultiKey> actualExperimentInstances = new LinkedHashSet<>();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin")).setInitializer((c) -> {
									c.addActor((c2) -> {
										c2.releaseOutput(new Object());
									});
								}).build();//

		Experiment	.builder()//
					.reportProgressToConsole(false)//
					.reportFailuresToConsole(false)//

					.addExperimentContextConsumer(c -> {
						c.subscribeToOutput(Object.class, (c2, s, e) -> {
							List<String> scenarioMetaData = c2.getScenarioMetaData(s).get();
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
		 * Show that an output handler receives data released by the simulation
		 * by adding a few actors that release data. Add output handlers that
		 * show that the output is correctly directed. Note the use of
		 * overlapping types for the output consumers and that the output type
		 * may be a subclass of the consumer's type.
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
				List<String> metaData = c2.getScenarioMetaData(s).get();
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

		Dimension dimension1 = Dimension.builder()//
										.addMetaDatum("dim1")//
										.addLevel((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_1_1");
											return result;
										})//
										.addLevel((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_1_2");
											return result;
										})//
										.build();//

		Dimension dimension2 = Dimension.builder()//
										.addMetaDatum("dim2")//
										.addLevel((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_2_1");
											return result;
										})//
										.addLevel((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_2_2");
											return result;
										})//
										.build();//

		Experiment	.builder()//
					.reportProgressToConsole(false)//
					.reportFailuresToConsole(false)//

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
		 * Build the expected order of initialization based on the dependencies
		 * between the plugins
		 */
		List<PluginId> expectedExecutedPlugins = new ArrayList<>();
		expectedExecutedPlugins.add(cId);
		expectedExecutedPlugins.add(bId);
		expectedExecutedPlugins.add(aId);

		// build a container for the actual initialization order
		List<PluginId> actualExecutedPlugins = new ArrayList<>();

		// create the three plugins with A depending on B and C and B depending
		// on C alone.
		Plugin pluginA = Plugin	.builder()//
								.setPluginId(aId)//
								.addPluginDependency(cId)//
								.addPluginDependency(bId)//
								.setInitializer((c) -> {
									actualExecutedPlugins.add(aId);
								}).build();//

		Plugin pluginB = Plugin	.builder()//
								.setPluginId(bId)//
								.addPluginDependency(cId)//
								.setInitializer((c) -> {
									actualExecutedPlugins.add(bId);
								}).build();//

		Plugin pluginC = Plugin	.builder()//
								.setPluginId(cId)//
								.setInitializer((c) -> {
									actualExecutedPlugins.add(cId);
								}).build();//

		// create the simulation
		Experiment	.builder()//
					.reportProgressToConsole(false)//
					.reportFailuresToConsole(false)//

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
		Experiment experiment = Experiment	.builder()//
											.reportProgressToConsole(false)//
											.reportFailuresToConsole(false)//
											.build();//
		experiment.execute();

		// Other aspects of the build are covered in the remaining capability
		// specific tests
	}
	

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setExperimentProgressLog", args = { Path.class }, tags = {UnitTag.MANUAL})	
	public void testSetExperimentProgressLog() {
		// should be manually tested		
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "reportProgressToConsole", args = { boolean.class }, tags = {UnitTag.MANUAL})
	public void testReportProgressToConsole() {
		// should be manually tested	
	}
	
	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setContinueFromProgressLog", args = { boolean.class }, tags = {UnitTag.MANUAL})
	public void testSetContinueFromProgressLog() {
		// should be manually tested	
	}
	
	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "reportFailuresToConsole", args = { boolean.class }, tags = {UnitTag.MANUAL})
	public void testReportFailuresToConsole() {
		// should be manually tested	
	}

	
	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setThreadCount", args = { int.class })
	public void testSetThreadCount() {

		// add two dimensions that will cause the experiment to execute 40
		// simulation instances
		Dimension.Builder dimBuilder = Dimension.builder().addMetaDatum("Alpha");//
		IntStream.range(0, 5).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				return result;
			});
		});

		Dimension dimension1 = dimBuilder.build();

		IntStream.range(0, 8).forEach((i) -> {
			dimBuilder.addLevel((context) -> {
				List<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				return result;
			});
		});

		Dimension dimension2 = dimBuilder.build();

		/*
		 * Create a thread safe set to record the thread ids that are used by
		 * each simulation
		 */
		Set<Long> threadIds = Collections.synchronizedSet(new LinkedHashSet<>());

		/*
		 * Add a plugin that will add an actor that records the thread id of the
		 * simulation running that actor
		 */
		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.setInitializer((c) -> {
									c.addActor((c2) -> {
										threadIds.add(Thread.currentThread().getId());
									});
								}).build();//

		// Run the experiment using several threads
		Experiment	.builder()//
					.reportProgressToConsole(false)//
					.reportFailuresToConsole(false)//
					.addPlugin(plugin)//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.setThreadCount(6).build()//
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
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that a builder is returned
		assertNotNull(Experiment.builder());
	}

	@Test
	@UnitTestMethod(name = "execute", args = {})
	public void testExecute() {
		// Covered by remaining tests that execute the experiment.
	}

}
