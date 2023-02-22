package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

public class AT_TestActor {

	@Test
	@UnitTestMethod(target = TestActor.class, name = "init", args = { ActorContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "actor alias 1";
		Object alias2 = "actor alias 2";

		// create containers for expected and actual observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		expectedObservations.add(new MultiKey(alias1, 3.0));
		expectedObservations.add(new MultiKey(alias2, 3.0));
		expectedObservations.add(new MultiKey(alias1, 4.212));
		expectedObservations.add(new MultiKey(alias1, 5.123));
		expectedObservations.add(new MultiKey(alias2, 43.0));
		expectedObservations.add(new MultiKey(alias1, 12.123));
		expectedObservations.add(new MultiKey(alias1, 8.534));
		expectedObservations.add(new MultiKey(alias2, 1.423));

		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add the actors to the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Create ActorActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestActorPlan(expectedAlias, new TestActorPlan(expectedTime, (c) -> {
				actualObservations.add(new MultiKey(expectedAlias, c.getTime()));
			}));
		}

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		//execute the simulation
		List<Plugin> plugins = new ArrayList<>();
		plugins.add(testPlugin);
		
		TestSimulation.executeSimulation(plugins);
		


		// show that the actors executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

	@Test
	@UnitTestConstructor(target = TestActor.class, args = { Object.class })
	public void testConstructor() {
		/*
		 * The test of the init() method suffices to show that the alias value
		 * passed in the constructor is utilized as designed.
		 */
	}

}
