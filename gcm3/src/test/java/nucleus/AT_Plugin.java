package nucleus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;

@UnitTest(target = Plugin.class)
@Disabled
public class AT_Plugin {
	@Test
	public void test() {

	}
	
//	@Test
//	@UnitTestMethod(name = "addPluginDependency", args = { PluginId.class })
//	public void testAddPluginDependency() {
//		final RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5064139892061974375L);
//
//		/*
//		 * We will generate several plugins with randomized, non-circular
//		 * dependencies. Each plugin initializer will add an actor to the
//		 * simulation. This actor will contribute the plugin's id to a local
//		 * list. If the plugin dependencies were properly ordered, then each
//		 * actor should come after the actors associated with the dependent
//		 * plugins.
//		 */
//
//		// replicate this test 30 times
//		for (int rep = 0; rep < 30; rep++) {
//
//			/*
//			 * Create a list to hold the plugin id values collected from the
//			 * actors on their initialization
//			 */
//			final List<PluginId> executionOrdering = new ArrayList<>();
//
//			// determine a random number of plugins to build
//			final int numberOfPlugins = randomGenerator.nextInt(20);
//
//			// create plugin ids
//			final List<PluginId> pluginIds = new ArrayList<>();
//			for (int i = 0; i < numberOfPlugins; i++) {
//				pluginIds.add(new SimplePluginId(i));
//			}
//
//			/*
//			 * Create a map to record the plugin dependencies for each plugin.
//			 */
//			final Map<PluginId, List<PluginId>> dependencyMap = new LinkedHashMap<>();
//
//			// begin building the simulation
//			final Simulation.Builder builder = Simulation.builder();//
//
//			// Create each plugin initializer with randomized, non-circular
//			// dependencies. Record the dependencies in the dependencyMap for
//			// later use.
//			for (int i = 0; i < numberOfPlugins; i++) {
//				final PluginId pluginId = pluginIds.get(i);
//				final Set<PluginId> selectedPlugins = new LinkedHashSet<>();
//				if (i > 0) {
//					final int maxNumberOfDependencies = randomGenerator.nextInt(i);
//					for (int j = 0; j < maxNumberOfDependencies; j++) {
//						selectedPlugins.add(pluginIds.get(randomGenerator.nextInt(i)));
//					}
//				}
//				final List<PluginId> dependencies = new ArrayList<>(selectedPlugins);
//				dependencyMap.put(pluginId, dependencies);
//
//				final DependencyPluginInitializer dependencyPluginInitializer = new DependencyPluginInitializer(pluginId, dependencies);
//				builder.addPluginInitializer(dependencyPluginInitializer);
//
//			}
//
//			// Create an output consumer that will collect the output from the
//			// actors.
//			builder.setOutputConsumer((o) -> {
//				if (o instanceof PluginId) {
//					executionOrdering.add((PluginId) o);
//				}
//			});
//
//			// Execute the simulation. We expect that the actors associated with
//			// the plugins will be initialized in the correct order.
//			builder.build().execute();//
//
//			// show that the plugin ordering recorded in the executionOrdering
//			// list is consistent with the ordering recorded in the plugin
//			// initializers
//			for (final PluginId pluginId : dependencyMap.keySet()) {
//				final List<PluginId> dependencies = dependencyMap.get(pluginId);
//				final int pluginIndex = executionOrdering.indexOf(pluginId);
//				assertTrue(pluginIndex >= 0);
//				for (final PluginId pId : dependencies) {
//					final int dependentIndex = executionOrdering.indexOf(pId);
//					assertTrue(dependentIndex >= 0);
//					assertTrue(pluginIndex > dependentIndex);
//				}
//			}
//
//		}
//
//	}
}
