package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = Experiment.class)
@Disabled
public class AT_Experiment {

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addDimension", args = { Dimension.class })
	public void testAddDimension() {
		fail();
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "addOutputHandler", args = { Consumer.class })
	public void testAddOutputHandler() {

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
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);
		
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
										.addPoint((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_1_1");
											return result;
										})//
										.addPoint((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_1_2");
											return result;
										})//
										.build();//

		Dimension dimension2 = Dimension.builder()//
										.addMetaDatum("dim2")//
										.addPoint((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_2_1");
											return result;
										})//
										.addPoint((tmap) -> {
											List<String> result = new ArrayList<>();
											result.add("var_2_2");
											return result;
										})//
										.build();//

		Experiment	.builder()//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.addOutputHandler(integerOutputHandler)//
					.addOutputHandler(stringOutputHandler)//
					.addOutputHandler(doubleOutputHandler)//
					.addOutputHandler(numberOutputHandler)//
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
		fail();
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
	@UnitTestMethod(target = Experiment.Builder.class, name = "setExperimentProgressConsole", args = { boolean.class })
	public void testSetExperimentProgressConsole() {
		fail();
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setExperimentProgressLog", args = { Path.class })
	public void testSetExperimentProgressLog() {
		fail();
	}

	@Test
	@UnitTestMethod(target = Experiment.Builder.class, name = "setThreadCount", args = { int.class })
	public void testSetThreadCount() {
		fail();
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
