package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Simulation;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionAgent.class)
public class AT_ActionAgent {

	
	
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
		ActionPluginData.Builder pluginDataBuilder = ActionPluginData.builder();
		pluginDataBuilder.addAgent(alias1);
		pluginDataBuilder.addAgent(alias2);

		/*
		 * Create AgentActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			Object expectedAlias = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginDataBuilder.addAgentActionPlan(expectedAlias, new AgentActionPlan(expectedTime, (c) -> {
				ActionPluginDataManager actionPluginDataManager = c.getDataManager(ActionPluginDataManager.class).get();
				Object alias = actionPluginDataManager.getAgentAlias(c.getCurrentAgentId()).get();				
				actualObservations.add(new MultiKey(alias, c.getTime()));
			}));
		}

		//build the action plugin
		ActionPluginData actionPluginData = pluginDataBuilder.build();
		ActionPluginInitializer actionPluginInitializer = new ActionPluginInitializer();

		// build and execute the engine
		Simulation	.builder()//
				.addPluginData(actionPluginData)//
				.addPluginInitializer(actionPluginInitializer)//
				.build()//
				.execute();//

		// show that all actions executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the agents executed the expected actions
//		for(MultiKey observation : expectedObservations) {
//			System.out.println(observation.toKeyString());
//		}
//		System.out.println();
//		for(MultiKey observation : actualObservations) {
//			System.out.println(observation.toKeyString());
//		}

		assertEquals(expectedObservations, actualObservations);

	}

}
