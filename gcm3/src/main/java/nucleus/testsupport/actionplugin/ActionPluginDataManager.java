package nucleus.testsupport.actionplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.EventLabeler;


public class ActionPluginDataManager extends DataManager {

	private final Map<AgentId, List<AgentActionPlan>> agentActionPlanMap = new LinkedHashMap<>();

	private final Map<Class<? extends ActionDataManager>, List<DataManagerActionPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	private List<EventLabeler<?>> eventLabelers = new ArrayList<>();

	public ActionPluginDataManager(ActionPluginData actionPluginData) {

		for (AgentId agentId : actionPluginData.getAgentIdsRequiringPlanning()) {
			List<AgentActionPlan> newAgentActionPlans = new ArrayList<>();
			agentActionPlanMap.put(agentId, newAgentActionPlans);
			List<AgentActionPlan> agentActionPlans = actionPluginData.getAgentActionPlans(agentId);
			for (AgentActionPlan agentActionPlan : agentActionPlans) {
				newAgentActionPlans.add(new AgentActionPlan(agentActionPlan));
			}
		}

		for (EventLabeler<?> eventLabeler : actionPluginData.getEventLabelers()) {
			eventLabelers.add(eventLabeler);
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

		for (EventLabeler<?> eventLabeler : eventLabelers) {
			dataManagerContext.addEventLabeler(eventLabeler);
		}
	}

	public List<AgentActionPlan> getAgentActionPlans(AgentId agentId) {
		List<AgentActionPlan> result = new ArrayList<>();
		
		for(AgentId aId : agentActionPlanMap.keySet()) {
			if(aId.equals(agentId)) {
				System.out.println("match");
			}else {
				System.out.println(aId+" is not a match for "+ agentId );
				System.out.println(aId.getClass().getSimpleName());
				System.out.println(agentId.getClass().getSimpleName());
			}
		}
		
		List<AgentActionPlan> list = agentActionPlanMap.get(agentId);
		if(list != null) {
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

	public boolean allActionsExecuted() {
		int planCount = 0;
		for (AgentId agentId : agentActionPlanMap.keySet()) {
			for(AgentActionPlan agentActionPlan : agentActionPlanMap.get(agentId)) {
				planCount++;
				if(!agentActionPlan.executed()) {
					return false;
				}
			}
		}
		
		for(Class<? extends ActionDataManager> c :  dataManagerActionPlanMap.keySet()) {
			for(DataManagerActionPlan dataManagerActionPlan :dataManagerActionPlanMap.get(c)) {
				planCount++;
				if(!dataManagerActionPlan.executed()) {
					return false;
				}
			}
		}
		
		return planCount>0;
	}
}
