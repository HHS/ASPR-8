package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

public class AT_TestReport {

	@Test
	@UnitTestMethod(target = TestReport.class, name = "init", args = { ReportContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "report alias 1";
		Object alias2 = "report alias 2";

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

		// add the reports to the action plugin
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		/*
		 * Create ReportActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addTestReportPlan(expectedAlias, new TestReportPlan(expectedTime, (c) -> {
				actualObservations.add(new MultiKey(expectedAlias, c.getTime()));
			}));
		}
		
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(Double.POSITIVE_INFINITY,(c)->{}));

		// build the action plugin
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		//execute the simulation
		List<Plugin> plugins = new ArrayList<>();
		plugins.add(testPlugin);
		
		TestSimulation.builder().addPlugins(plugins).build().execute();
		


		// show that the reports executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

	@Test
	@UnitTestConstructor(target = TestReport.class, args = { Object.class })
	public void testConstructor() {
		/*
		 * The test of the init() method suffices to show that the alias value
		 * passed in the constructor is utilized as designed.
		 */
	}

}
