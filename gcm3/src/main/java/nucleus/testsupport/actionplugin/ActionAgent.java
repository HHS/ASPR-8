package nucleus.testsupport.actionplugin;

import java.util.List;

import nucleus.AgentContext;
import nucleus.AgentId;

/**
 * Test Support agent implementation designed to execute test-defined behaviors
 * from within the agent. The agent first associates its AgentId with its alias
 * via an AliasAssignmentEvent. It then schedules the AgentActionPlans that were
 * stored in the ActionDataView that were associated with its alias.
 * 
 * Alias identification exists for the convenience of the test implementor so
 * that tests can name agents and are not bound to the forced ordering pattern
 * implied by AgentId values.
 * 
 * @author Shawn Hatch
 *
 */
public final class ActionAgent {

	/**
	 * Associates its AgentId with its alias via an AliasAssignmentEvent. Schedules the
	 * AgentActionPlans that were stored in the ActionDataView that were associated
	 * with its alias.
	 */
	public void init(AgentContext agentContext) {
		ActionPluginDataManager actionPluginDataManager = agentContext.getDataManager(ActionPluginDataManager.class).get();
		AgentId agentId = agentContext.getCurrentAgentId();
		List<AgentActionPlan> agentActionPlans = actionPluginDataManager.getAgentActionPlans(agentId);
		for (final AgentActionPlan agentActionPlan : agentActionPlans) {
			if (agentActionPlan.getKey() != null) {
				agentContext.addKeyedPlan(agentActionPlan::executeAction, agentActionPlan.getScheduledTime(), agentActionPlan.getKey());
			} else {
				agentContext.addPlan(agentActionPlan::executeAction, agentActionPlan.getScheduledTime());
			}
		}
	}

}
