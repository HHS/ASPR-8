package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Simulation;
import nucleus.ReportContext;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
@UnitTest(target = ActionResolver.class)
public class AT_ActionResolver {
	/**
	 * Show construction works
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test in the constructor
	}

	/**
	 * 
	 * Show that an action report executes its ReportActionPlans via its report
	 * id as expected.
	 */
	@Test
	@UnitTestMethod(name = "init", args = { ReportContext.class })
	public void testInit() {
		// create two reports
		ResolverId resolverId1 = new SimpleResolverId("resolver id 1");
		ResolverId resolverId2 = new SimpleResolverId("resolver id 2");

		// create containers for expected and actual observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		expectedObservations.add(new MultiKey(resolverId1, 3.0));
		expectedObservations.add(new MultiKey(resolverId2, 3.0));
		expectedObservations.add(new MultiKey(resolverId1, 4.212));
		expectedObservations.add(new MultiKey(resolverId1, 5.123));
		expectedObservations.add(new MultiKey(resolverId2, 43.0));
		expectedObservations.add(new MultiKey(resolverId1, 12.123));
		expectedObservations.add(new MultiKey(resolverId1, 8.534));
		expectedObservations.add(new MultiKey(resolverId2, 1.423));

		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add the resolvers to the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addResolver(resolverId1);
		pluginBuilder.addResolver(resolverId2);

		/*
		 * Create ReportActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			ResolverId expectedResolverId = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginBuilder.addResolverActionPlan(expectedResolverId, new ResolverActionPlan(expectedTime, (c) -> {
				ResolverId actaulResolverId = c.getCurrentResolverId();
				Double actualTime = c.getTime();
				actualObservations.add(new MultiKey(actaulResolverId, actualTime));
			}));
		}

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation	.builder()//
				.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init)//
				.build()//
				.execute();//

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the agents executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}
}
