package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Experiment;
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
				Object alias = actionPluginDataManager.getAgentAlias(c.getAgentId()).get();
				actualObservations.add(new MultiKey(alias, c.getTime()));
			}));
		}

		// build the action plugin
		ActionPluginData actionPluginData = pluginDataBuilder.build();
		ActionPluginInitializer actionPluginInitializer = new ActionPluginInitializer();

		ExperimentActionCompletionObserver experimentActionCompletionObserver = new ExperimentActionCompletionObserver();

		// build and execute the engine
		Experiment	.builder()//
					.addOutputHandler(experimentActionCompletionObserver::init)//
					.addPluginInitializer(actionPluginInitializer)//
					.addPluginData(actionPluginData)//
					.build()//
					.execute();//

		// show that all actions executed
		Optional<ActionCompletionReport> optional = experimentActionCompletionObserver.getActionCompletionReport(0);
		assertTrue(optional.isPresent(),"Scenario did not complete");
		
		ActionCompletionReport actionCompletionReport = optional.get();
		assertTrue(actionCompletionReport.isComplete(), "Some planned action were not executed");

//		for (MultiKey observation : expectedObservations) {
//			System.out.println(observation.toKeyString());
//		}
//		System.out.println("------");
//		for (MultiKey observation : actualObservations) {
//			System.out.println(observation.toKeyString());
//		}

		// show that the agents executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}

}
