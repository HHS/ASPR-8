package nucleus.testsupport.actionplugin;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.AgentId;
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
public final class ActionDataContainer {

	/*
	 * Map of action plans key by agent aliases
	 */
	private final Map<Object, Set<AgentActionPlan>> agentActionPlanMap = new LinkedHashMap<>();

	private final Map<ReportId, Set<ReportActionPlan>> reportActionPlanMap = new LinkedHashMap<>();

	private final Map<ResolverId, Set<ResolverActionPlan>> resolverActionPlanMap = new LinkedHashMap<>();

	/**
	 * Returns the action plans associated with the given alias in the order of
	 * their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the alias is null</li>
	 */
	public Set<AgentActionPlan> getAgentActionPlans(final Object alias) {
		if (alias == null) {
			throw new RuntimeException("null alias");
		}
		final Set<AgentActionPlan> result = new LinkedHashSet<>();
		final Set<AgentActionPlan> set = agentActionPlanMap.get(alias);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Adds the agent action plan in association with the given alias. Duplicate
	 * action plans replace previously added action plans
	 * 
	 * @throws RuntimeException
	 *             <li>if the alias is null</li>
	 *             <li>if the agent action plan is null</li>
	 */
	public void addAgentActionPlan(final Object alias, AgentActionPlan agentActionPlan) {
		if (alias == null) {
			throw new RuntimeException("null alias");
		}
		if (agentActionPlan == null) {
			throw new RuntimeException("null action plan");
		}

		Set<AgentActionPlan> set = agentActionPlanMap.get(alias);

		if (set == null) {
			set = new LinkedHashSet<>();
			agentActionPlanMap.put(alias, set);
		}

		set.add(agentActionPlan);
	}

	/**
	 * Returns the report plans associated with the given report id in the order
	 * of their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the report id is null</li>
	 */
	public Set<ReportActionPlan> getReportActionPlans(final ReportId reportId) {
		if (reportId == null) {
			throw new RuntimeException("null report id");
		}
		final Set<ReportActionPlan> result = new LinkedHashSet<>();
		final Set<ReportActionPlan> set = reportActionPlanMap.get(reportId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns the resolver plans associated with the given resolver id in the
	 * order of their addition
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the resolver id is null</li>
	 */
	public Set<ResolverActionPlan> getResolverActionPlans(final ResolverId resolverId) {
		if (resolverId == null) {
			throw new RuntimeException("null resolver id");
		}
		final Set<ResolverActionPlan> result = new LinkedHashSet<>();
		final Set<ResolverActionPlan> set = resolverActionPlanMap.get(resolverId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Adds the report action plan in association with the given report id.
	 * Duplicate report action plans replace previously added report action
	 * plans
	 * 
	 * @throws RuntimeException
	 *             <li>if the report id is null</li>
	 *             <li>if the report action plan is null</li>
	 */
	public void addReportActionPlan(final ReportId reportId, ReportActionPlan reportActionPlan) {
		if (reportId == null) {
			throw new RuntimeException("null report id");
		}
		if (reportActionPlan == null) {
			throw new RuntimeException("null action plan");
		}

		Set<ReportActionPlan> set = reportActionPlanMap.get(reportId);

		if (set == null) {
			set = new LinkedHashSet<>();
			reportActionPlanMap.put(reportId, set);
		}

		set.add(reportActionPlan);
	}

	public void addResolverActionPlan(final ResolverId resolverId, ResolverActionPlan resolverActionPlan) {
		if (resolverId == null) {
			throw new RuntimeException("null report id");
		}
		if (resolverActionPlan == null) {
			throw new RuntimeException("null action plan");
		}

		Set<ResolverActionPlan> set = resolverActionPlanMap.get(resolverId);

		if (set == null) {
			set = new LinkedHashSet<>();
			resolverActionPlanMap.put(resolverId, set);
		}

		set.add(resolverActionPlan);
	}

	/*
	 * Mapping from agent id to alias. Provided by the ActionAgents during their
	 * initialization.
	 */
	private Map<AgentId, Object> agentToAliasMap = new LinkedHashMap<>();
	private Map<Object,AgentId> aliasToAgentMap = new LinkedHashMap<>();

	/**
	 * Set the association between an alias and an agent id
	 * 
	 * @throws RuntimeException
	 * <li>if the alias is null</li>
	 * <li>if the agent id is null</li>
	 * <li>if the agent id was previously associated with an alias</li>
	 * <li>if the alias was previously associated with an agent id</li>
	 */
	public void assignAgentIdToAlias(Object alias, AgentId agentId) {
		if (alias == null) {
			throw new RuntimeException("null alias");
		}
		if (agentId == null) {
			throw new RuntimeException("null agent id");
		}
		if (agentToAliasMap.containsKey(agentId)) {
			throw new RuntimeException("agent id already has an alias assigned");
		}
		if (aliasToAgentMap.containsKey(alias)) {
			throw new RuntimeException("alias already has agent id assigned");
		}
		
		aliasToAgentMap.put(alias, agentId);
		agentToAliasMap.put(agentId, alias);
	}

	/**
	 * Returns the alias id provided by each agent.
	 * 
	 * @throws RuntimeException
	 *             <li>if the agent id is null</li>
	 * 
	 */
	public Optional<Object> getAgentAliasId(AgentId agentId) {
		if(agentId == null) {
			throw new RuntimeException("null agent id");
		}
		Object result = agentToAliasMap.get(agentId);
		return Optional.ofNullable(result);
	}

}
