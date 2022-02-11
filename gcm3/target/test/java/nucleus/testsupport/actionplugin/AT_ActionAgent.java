package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.SimpleAgentId;
import nucleus.Simulation;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionAgent.class)
public class AT_ActionAgent {
	
	public static void main(String[] args) {
		
		new AT_ActionAgent().testInit();
	
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testInit() {
		
		
		// create two aliases
		String alias1 = "agent alias 1";
		String alias2 = "agent alias 2";

		AgentId agentId1 = new SimpleAgentId(alias1);
		AgentId agentId2 = new SimpleAgentId(alias2);

		// create containers for expected and actual observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		expectedObservations.add(new MultiKey(agentId1, 3.0));
		expectedObservations.add(new MultiKey(agentId2, 3.0));
		expectedObservations.add(new MultiKey(agentId1, 4.212));
		expectedObservations.add(new MultiKey(agentId1, 5.123));
		expectedObservations.add(new MultiKey(agentId2, 43.0));
		expectedObservations.add(new MultiKey(agentId1, 12.123));
		expectedObservations.add(new MultiKey(agentId1, 8.534));
		expectedObservations.add(new MultiKey(agentId2, 1.423));

		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add the agents to the action plugin
		ActionPluginData.Builder builder = ActionPluginData.builder();
		builder.addAgent(alias1);
		builder.addAgent(alias2);

		/*
		 * Create AgentActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			SimpleAgentId simpleAgentId = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			builder.addAgentActionPlan(simpleAgentId.getValue(), new AgentActionPlan(expectedTime, (c) -> {
				actualObservations.add(new MultiKey(c.getCurrentAgentId(), c.getTime()));
			}));
		}

		// build the action plugin
		ActionPluginData actionPluginData = builder.build();
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
		
		for(MultiKey observation : expectedObservations) {
			System.out.println(observation.toKeyString());
		}
		System.out.println();
		for(MultiKey observation : actualObservations) {
			System.out.println(observation.toKeyString());
		}
		assertEquals(expectedObservations, actualObservations);


	}

}
