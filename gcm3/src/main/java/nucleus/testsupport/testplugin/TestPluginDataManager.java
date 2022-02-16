package nucleus.testsupport.actionplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.DataManagerId;

public class ActionPluginDataManager extends DataManager {

	private Map<AgentId, Object> agentAliasMap = new LinkedHashMap<>();
	
	private Map<DataManagerId, Object> dataManagerAliasMap = new LinkedHashMap<>();

	private final Map<Object, List<AgentActionPlan>> agentActionPlanMap = new LinkedHashMap<>();

	private final Map<Class<? extends ActionDataManager>, List<DataManagerActionPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	

	public ActionPluginDataManager(ActionPluginData actionPluginData) {

		for (Object alias : actionPluginData.getAgentsRequiringPlanning()) {
			List<AgentActionPlan> newAgentActionPlans = new ArrayList<>();
			agentActionPlanMap.put(alias, newAgentActionPlans);
			List<AgentActionPlan> agentActionPlans = actionPluginData.getAgentActionPlans(alias);
			for (AgentActionPlan agentActionPlan : agentActionPlans) {
				newAgentActionPlans.add(new AgentActionPlan(agentActionPlan));
			}
		}

		
		List<Class<? extends ActionDataManager>> actionDataManagerTypes = actionPluginData.getActionDataManagerTypes();
		for (Class<? extends ActionDataManager> c : actionDataManagerTypes) {
			List<DataManagerActionPlan> newDataManagerActionPlans = new ArrayList<>();
			dataManagerActionPlanMap.put(c, newDataManagerActionPlans);
			for (DataManagerActionPlan dataManagerActionPlan : actionPluginData.getDataManagerActionPlans(c)) {
				newDataManagerActionPlans.add(new DataManagerActionPlan(dataManagerActionPlan));
			}
		}
	}

	@Override
	protected void init(DataManagerContext dataManagerContext) {		
		dataManagerContext.subscribeToSimulationClose(this::sendActionCompletionReport);
	}

	public List<AgentActionPlan> getAgentActionPlans(Object alias) {
		List<AgentActionPlan> result = new ArrayList<>();
		List<AgentActionPlan> list = agentActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	public List<DataManagerActionPlan> getDataManagerActionPlans(Class<? extends ActionDataManager> actionDataManagerClass) {
		List<DataManagerActionPlan> result = new ArrayList<>();

		List<DataManagerActionPlan> list = dataManagerActionPlanMap.get(actionDataManagerClass);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Return true if and only if all actions that are stored in this action
	 * data view have been executed. Indicates that all injected behaviors in a
	 * unit test were actually executed. RETURNS FALSE IF THERE WERE NO ACTIONS
	 * STORED.
	 */

	private void sendActionCompletionReport(DataManagerContext context) {
		context.releaseOutput(new ActionCompletionReport(allActionsExecuted()));
	}
	
	private boolean allActionsExecuted() {
		int planCount = 0;
		for (Object alias : agentActionPlanMap.keySet()) {
			for (AgentActionPlan agentActionPlan : agentActionPlanMap.get(alias)) {
				planCount++;
				if (!agentActionPlan.executed()) {
					return false;
				}
			}
		}

		for (Class<? extends ActionDataManager> c : dataManagerActionPlanMap.keySet()) {
			for (DataManagerActionPlan dataManagerActionPlan : dataManagerActionPlanMap.get(c)) {
				planCount++;
				if (!dataManagerActionPlan.executed()) {
					return false;
				}
			}
		}

		return planCount > 0;
		
	}

	public Optional<Object> getAgentAlias(AgentId agentId) {
		return Optional.ofNullable(agentAliasMap.get(agentId));
	}

	public void setAgentAlias(AgentId agentId, Object alias) {

		if ((agentId != null) && (alias != null)) {
			agentAliasMap.put(agentId, alias);
		}

	}

	public Optional<Object> getDataManagerAlias(DataManagerId dataManagerId) {
		return Optional.ofNullable(dataManagerAliasMap.get(dataManagerId));
	}

	public void setDataManagerAlias(DataManagerId dataManagerId, Object alias) {
		if ((dataManagerId != null) & (alias != null)) {
			dataManagerAliasMap.put(dataManagerId, alias);
		}
	}

}
