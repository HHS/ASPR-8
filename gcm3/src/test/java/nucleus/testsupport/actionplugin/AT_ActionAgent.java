package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Simulation;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionAgent.class)
public class AT_ActionAgent {

	/**
	 * Show construction works
	 */
	@Test
	@UnitTestConstructor(args = { Object.class })
	public void testConstructor() {
		// precondition checks
		assertThrows(RuntimeException.class, () -> new ActionAgent(null));
	}

	/**
	 * 
	 * Show that an action agent executes its AgentActionPlans via its alias as
	 * expected.
	 */
	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testInit() {
		// create two aliases
		Object alias1 = "agent alias 1";
		Object alias2 = "agent alias 2";

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

		// add the agents to the action plugin
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		pluginBuilder.addAgent(alias1);
		pluginBuilder.addAgent(alias2);

		/*
		 * Create AgentActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginBuilder.addAgentActionPlan(expectedAlias, new AgentActionPlan(expectedTime, (c) -> {
				ActionDataView actionDataView = c.getDataView(ActionDataView.class).get();
				Object actaulAlias = actionDataView.getAgentAliasId(c.getCurrentAgentId()).get();
				Double actualTime = c.getTime();
				actualObservations.add(new MultiKey(actaulAlias, actualTime));
			}));
		}

		//build the action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// build and execute the engine
		Simulation	.builder()//
				.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init)//
				.build()//
				.execute();//

		// show that all actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the agents executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

}
