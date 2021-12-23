package nucleus.testsupport.actionplugin;

import java.util.Optional;
import java.util.Set;

import nucleus.AgentId;
import nucleus.DataView;
import nucleus.ReportId;
import nucleus.ResolverId;

/**
 * Test Support unit that provides a mutable, shared data source that is
 * available to simulation members as well as the test code that executes the
 * test simulation.
 * 
 * @author Shawn Hatch
 *
 */
public final class ActionDataView implements DataView {

	private final ActionDataContainer actionDataContainer;

	
	/**
	 * Constructs an action data view from the given action data container
	 * 
	 * @throws RuntimeException
	 * <li>if the data container is null</li>
	 */
	public ActionDataView(ActionDataContainer actionDataContainer) {
		if(actionDataContainer == null) {
			throw new RuntimeException("null action data container");
		}
		this.actionDataContainer = actionDataContainer;
	}

	/**
	 * Returns the action plans associated with the given alias in the order of
	 * their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the alias is null</li>
	 */
	public Set<AgentActionPlan> getAgentActionPlans(final Object alias) {		
		return actionDataContainer.getAgentActionPlans(alias);
	}
	
	/**
	 * Returns the report plans associated with the given report id in the order
	 * of their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the report id is null</li>
	 */
	public Set<ReportActionPlan> getReportActionPlans(final ReportId reportId) {		
		return actionDataContainer.getReportActionPlans(reportId);
	}

	/**
	 * Returns the resolver plans associated with the given resolver id in the
	 * order of their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the resolver id is null</li>
	 */
	public Set<ResolverActionPlan> getResolverActionPlans(final ResolverId resolverId) {
		return actionDataContainer.getResolverActionPlans(resolverId);
	}
	/**
	 * Returns the alias id provided by each agent.
	 * 
	 * @throws RuntimeException
	 *             <li>if the agent id is null</li>
	 * 
	 */
	public Optional<Object> getAgentAliasId(AgentId agentId) {
		return actionDataContainer.getAgentAliasId(agentId);
	}

}
