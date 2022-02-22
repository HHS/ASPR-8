package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestDataManager;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginInitializer;
import util.MutableBoolean;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * The PluginContext interface is implemented by the {@link Simulation}. These
 * tests cover that implementation.
 *
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PluginContext.class)
public class AT_PluginContext {

	/*
	 * A PluginInitializer that is used to demonstrate that the plugin
	 * context(i.e. the simulation) handles plugin dependencies correctly.
	 */
	private static class DependencyPluginInitializer implements PluginInitializer {
		private final PluginId pluginId;
		private final List<PluginId> dependencies = new ArrayList<>();

		public DependencyPluginInitializer(final PluginId pluginId, final List<PluginId> dependencies) {
			this.pluginId = pluginId;
			this.dependencies.addAll(dependencies);
		}

		@Override
		public PluginId getPluginId() {
			return pluginId;
		}

		@Override
		public void init(final PluginContext pluginContext) {
			for (final PluginId pId : dependencies) {
				pluginContext.addPluginDependency(pId);
			}
			// Add an actor that will release the plugin id as output.
			pluginContext.addActor((c) -> c.releaseOutput(pluginId));
		}
	}

	/*
	 * A PluginInitializer that adds a number of actors to a simulation and
	 * collects signals from those actors when they are initialized by the
	 * simulation
	 */
	private static class LocalPluginInitializer_Actors implements PluginInitializer {
		private final int actorCount;
		private final Set<Integer> actorInitializations = new LinkedHashSet<>();

		public LocalPluginInitializer_Actors(final int actorCount) {
			this.actorCount = actorCount;
		}

		public int actorsCounted() {
			return actorInitializations.size();
		}

		@Override
		public PluginId getPluginId() {
			return new SimplePluginId(this.getClass());
		}

		@Override
		public void init(final PluginContext pluginContext) {
			IntStream.range(0, actorCount).forEach(i -> {
				pluginContext.addActor((c) -> actorInitializations.add(i));
			});

		}
	}

	public static class TestDataManager1 extends TestDataManager {

		@Override
		protected void init(final DataManagerContext dataManagerContext) {
		}
	}

	public static class TestDataManager2 extends TestDataManager {

		@Override
		protected void init(final DataManagerContext dataManagerContext) {
		}
	}

	public static class TestDataManager3 extends TestDataManager {

		@Override
		protected void init(final DataManagerContext dataManagerContext) {
		}
	}

	@Test
	@UnitTestMethod(name = "addActor", args = { Consumer.class })
	public void testAddActor() {

		/*
		 * Create a plugin initializer that will add a few actors. Each actor
		 * will signal when it has initialized and the initializer will record
		 * that signal.
		 */
		final LocalPluginInitializer_Actors localPluginInitializer_Actors = new LocalPluginInitializer_Actors(5);

		// build and execute the simulation
		Simulation	.builder()//
					.addPluginInitializer(localPluginInitializer_Actors)//
					.build()//
					.execute();//

		// show that the actors were added to the simulation
		assertEquals(5, localPluginInitializer_Actors.actorsCounted());
	}

	@Test
	@UnitTestMethod(name = "addDataManager", args = { DataManager.class })
	public void testAddDataManager() {

		/*
		 * The TestPluginInitialzer uses the PluginContext to add data managers.
		 * If we add data managers via the TestPlugin and have an actor show
		 * that each data manager exists during the simulation run, we can infer
		 * that the addDataManager of the plugin context must be working
		 * correctly.
		 */

		final MutableBoolean actorExecuted = new MutableBoolean();

		// add the actors to the action plugin
		final TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("A", TestDataManager1.class);
		pluginDataBuilder.addTestDataManager("B", TestDataManager2.class);
		pluginDataBuilder.addTestDataManager("C", TestDataManager3.class);
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			assertTrue(c.getDataManager(TestDataManager1.class).isPresent());
			assertTrue(c.getDataManager(TestDataManager2.class).isPresent());
			assertTrue(c.getDataManager(TestDataManager3.class).isPresent());
			actorExecuted.setValue(true);
		}));

		// build the action plugin
		final TestPluginData testPluginData = pluginDataBuilder.build();
		final TestPluginInitializer testPluginInitializer = new TestPluginInitializer();

		// build and execute the engine
		Simulation	.builder()//
					.addPluginInitializer(testPluginInitializer)//
					.addPluginData(testPluginData)//
					.build()//
					.execute();//

		// show that the assertions were executed
		assertTrue(actorExecuted.getValue());

	}

	@Test
	@UnitTestMethod(name = "addPluginDependency", args = { PluginId.class })
	public void testAddPluginDependency() {
		final RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5064139892061974375L);

		/*
		 * We will generate several plugins with randomized, non-circular
		 * dependencies. Each plugin initializer will add an actor to the
		 * simulation. This actor will contribute the plugin's id to a local
		 * list. If the plugin dependencies were properly ordered, then each
		 * actor should come after the actors associated with the dependent
		 * plugins.
		 */

		// replicate this test 30 times
		for (int rep = 0; rep < 30; rep++) {

			/*
			 * Create a list to hold the plugin id values collected from the
			 * actors on their initialization
			 */
			final List<PluginId> executionOrdering = new ArrayList<>();

			// determine a random number of plugins to build
			final int numberOfPlugins = randomGenerator.nextInt(20);

			// create plugin ids
			final List<PluginId> pluginIds = new ArrayList<>();
			for (int i = 0; i < numberOfPlugins; i++) {
				pluginIds.add(new SimplePluginId(i));
			}

			/*
			 * Create a map to record the plugin dependencies for each plugin.
			 */
			final Map<PluginId, List<PluginId>> dependencyMap = new LinkedHashMap<>();

			// begin building the simulation
			final Simulation.Builder builder = Simulation.builder();//

			// Create each plugin initializer with randomized, non-circular
			// dependencies. Record the dependencies in the dependencyMap for
			// later use.
			for (int i = 0; i < numberOfPlugins; i++) {
				final PluginId pluginId = pluginIds.get(i);
				final Set<PluginId> selectedPlugins = new LinkedHashSet<>();
				if (i > 0) {
					final int maxNumberOfDependencies = randomGenerator.nextInt(i);
					for (int j = 0; j < maxNumberOfDependencies; j++) {
						selectedPlugins.add(pluginIds.get(randomGenerator.nextInt(i)));
					}
				}
				final List<PluginId> dependencies = new ArrayList<>(selectedPlugins);
				dependencyMap.put(pluginId, dependencies);

				final DependencyPluginInitializer dependencyPluginInitializer = new DependencyPluginInitializer(pluginId, dependencies);
				builder.addPluginInitializer(dependencyPluginInitializer);

			}

			// Create an output consumer that will collect the output from the
			// actors.
			builder.setOutputConsumer((o) -> {
				if (o instanceof PluginId) {
					executionOrdering.add((PluginId) o);
				}
			});

			// Execute the simulation. We expect that the actors associated with
			// the plugins will be initialized in the correct order.
			builder.build().execute();//

			// show that the plugin ordering recorded in the executionOrdering
			// list is consistent with the ordering recorded in the plugin
			// initializers
			for (final PluginId pluginId : dependencyMap.keySet()) {
				final List<PluginId> dependencies = dependencyMap.get(pluginId);
				final int pluginIndex = executionOrdering.indexOf(pluginId);
				assertTrue(pluginIndex >= 0);
				for (final PluginId pId : dependencies) {
					final int dependentIndex = executionOrdering.indexOf(pId);
					assertTrue(dependentIndex >= 0);
					assertTrue(pluginIndex > dependentIndex);
				}
			}

		}

	}

	private static class PluginData1 implements PluginData {

		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}

	}

	private static class PluginData2 implements PluginData {

		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}

	}

	private static class PluginData3 implements PluginData {

		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}

	}

	private static class DMPluginInitializer implements PluginInitializer {

		private boolean initExecuted;

		@Override
		public PluginId getPluginId() {
			return new SimplePluginId(this.getClass());
		}

		@Override
		public void init(PluginContext pluginContext) {
			assertTrue(pluginContext.getPluginData(PluginData1.class).isPresent());
			assertTrue(pluginContext.getPluginData(PluginData2.class).isPresent());
			assertTrue(pluginContext.getPluginData(PluginData3.class).isPresent());
			initExecuted = true;
		}

	}

	@Test
	@UnitTestMethod(name = "getPluginData", args = { Class.class })
	public void testGetPluginData() {

		// Create a plugin initializer that will get three plugin data objects
		// on its initialization
		DMPluginInitializer dmInitializer = new DMPluginInitializer();
		Simulation	.builder()//
					.addPluginInitializer(dmInitializer)//
					.addPluginData(new PluginData1())//
					.addPluginData(new PluginData2())//
					.addPluginData(new PluginData3())//
					.build()//
					.execute();//

		// show that the init method containing the assertions was executed
		assertTrue(dmInitializer.initExecuted);

	}

}
