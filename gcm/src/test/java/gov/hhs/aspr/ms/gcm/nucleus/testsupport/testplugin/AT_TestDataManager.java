package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

public class AT_TestDataManager {

	private static class TestDataManagerType1 extends TestDataManager {

	}

	private static class TestDataManagerType2 extends TestDataManager {

	}

	@Test
	@UnitTestMethod(target = TestDataManager.class,name = "init", args = { DataManagerContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "alias 1";
		Object alias2 = "alias 2";

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
		pluginDataBuilder.addTestDataManager(alias1, () -> new TestDataManagerType1());
		pluginDataBuilder.addTestDataManager(alias2, () -> new TestDataManagerType2());

		/*
		 * Create ActorActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestDataManagerPlan(expectedAlias, new TestDataManagerPlan(expectedTime, (c) -> {
				actualObservations.add(new MultiKey(expectedAlias, c.getTime()));
			}));
		}

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		List<Plugin> plugins = new ArrayList<>();
		plugins.add(testPlugin);
		
		TestSimulation.builder().addPlugins(plugins).build().execute();
		
		// show that the actors executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

	@Test
	@UnitTestConstructor(target = TestDataManager.class, args = {}, tags = { UnitTag.INCOMPLETE })
	public void testConstructor() {
		// nothing to test
	}

}
