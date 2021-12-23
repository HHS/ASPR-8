package nucleus.testsupport.actionplugin;

import java.util.Set;

import nucleus.AgentContext;

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
	private final Object alias;

	/**
	 * Constructs an ActionAgent with the given alias
	 * 
	 * @throws RuntimeException
	 *             <li>if the alias is null</li>
	 */
	public ActionAgent(Object alias) {
		if(alias == null) {
			throw new RuntimeException("null alias");
		}		
		this.alias = alias;
	}

	/**
	 * Associates its AgentId with its alias via an AliasAssignmentEvent. Schedules the
	 * AgentActionPlans that were stored in the ActionDataView that were associated
	 * with its alias.
	 */
	public void init(AgentContext agentContext) {
		ActionDataView actionDataView = agentContext.getDataView(ActionDataView.class).get();
		// let the action data view know how to associate an agent id with its
		// alias

		agentContext.resolveEvent(new AliasAssignmentEvent(alias));

		// retrieve the action plans from the action data view and schedule them
		// with the context
		Set<AgentActionPlan> agentActionPlans = actionDataView.getAgentActionPlans(alias);
		for (final AgentActionPlan agentActionPlan : agentActionPlans) {
			if (agentActionPlan.getKey() != null) {
				agentContext.addPlan(agentActionPlan::executeAction, agentActionPlan.getScheduledTime(), agentActionPlan.getKey());
			} else {
				agentContext.addPlan(agentActionPlan::executeAction, agentActionPlan.getScheduledTime());
			}
		}
	}

}
