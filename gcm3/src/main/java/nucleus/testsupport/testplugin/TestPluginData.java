package nucleus.testsupport.actionplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

@ThreadSafe
public class ActionPluginData implements PluginData {

	private static class Data {

		private Data() {
		}

		private Data(Data data) {

			
			for(Object alias : data.agentActionPlanMap.keySet()) {
				List<AgentActionPlan> oldPlans = data.agentActionPlanMap.get(alias);
				List<AgentActionPlan> newPlans = new ArrayList<>();
				agentActionPlanMap.put(alias, newPlans);
				for(AgentActionPlan oldPlan : oldPlans) {
					AgentActionPlan newPlan = new AgentActionPlan(oldPlan);
					newPlans.add(newPlan);
				}				
			}
			
			agentAliasesMarkedForConstruction.addAll(data.agentAliasesMarkedForConstruction);
			
			dataManagerAliasMap.putAll(data.dataManagerAliasMap);

			actionDataManagerTypes.putAll(data.actionDataManagerTypes);
			
			for(Object alias : data.dataManagerActionPlanMap.keySet()) {
				List<DataManagerActionPlan> oldPlans = data.dataManagerActionPlanMap.get(alias);
				List<DataManagerActionPlan> newPlans = new ArrayList<>();
				dataManagerActionPlanMap.put(alias, newPlans);
				for(DataManagerActionPlan oldPlan : oldPlans) {
					DataManagerActionPlan newPlan = new DataManagerActionPlan(oldPlan);
					newPlans.add(newPlan);
				}				
			}			

		}

		/*
		 * Map of action plans key by agent aliases
		 */
		private final Map<Object, List<AgentActionPlan>> agentActionPlanMap = new LinkedHashMap<>();

		/*
		 * Contains the alias values for which agent construction must be
		 * handled by the Action Plugin Initializer
		 */
		private Set<Object> agentAliasesMarkedForConstruction = new LinkedHashSet<>();
		

		private Map<Class<? extends ActionDataManager>, Object> dataManagerAliasMap = new LinkedHashMap<>();

		private Map<Object, Class<? extends ActionDataManager>> actionDataManagerTypes = new LinkedHashMap<>();

		private final Map<Object, List<DataManagerActionPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	}

	private ActionPluginData(Data data) {
		this.data = data;

	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		@Override
		public ActionPluginData build() {
			try {
				return new ActionPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds an agent action plan associated with the alias
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null</li>
		 *             <li>if the agent action plan is null</li>
		 */
		public Builder addAgentActionPlan(final Object alias, AgentActionPlan agentActionPlan) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (agentActionPlan == null) {
				throw new RuntimeException("null action plan");
			}

			List<AgentActionPlan> list = data.agentActionPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.agentActionPlanMap.put(alias, list);
			}

			list.add(agentActionPlan);

			return this;

		}

		/**
		 * Causes the action plugin to create the agent as an ActionAgent
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null
		 * 
		 */
		public Builder addAgent(Object alias) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			data.agentAliasesMarkedForConstruction.add(alias);
			return this;
		}

		

		public Builder addActionDataManager(Object alias, Class<? extends ActionDataManager> actionDataManagerClass) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (actionDataManagerClass == null) {
				throw new RuntimeException("null action data manager class");
			}
			data.actionDataManagerTypes.put(alias, actionDataManagerClass);
			data.dataManagerAliasMap.put(actionDataManagerClass, alias);
			return this;
		}

		/**
		 * Adds an data manager action plan associated with the alias
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null</li>
		 *             <li>if the agent action plan is null</li>
		 */
		public Builder addDataManagerActionPlan(final Object alias, DataManagerActionPlan dataManagerActionPlan) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (dataManagerActionPlan == null) {
				throw new RuntimeException("null action plan");
			}

			List<DataManagerActionPlan> list = data.dataManagerActionPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.dataManagerActionPlanMap.put(alias, list);
			}

			list.add(dataManagerActionPlan);

			return this;

		}

	}

	@Override
	public Builder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	private final Data data;

	public List<Object> getAgentsRequiringConstruction() {
		return new ArrayList<>(data.agentAliasesMarkedForConstruction);
	}

	public List<Object> getAgentsRequiringPlanning() {
		return new ArrayList<>(data.agentActionPlanMap.keySet());
	}

	public List<AgentActionPlan> getAgentActionPlans(Object alias) {
		List<AgentActionPlan> result = new ArrayList<>();
		List<AgentActionPlan> list = data.agentActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	public List<Class<? extends ActionDataManager>> getActionDataManagerTypes() {
		return new ArrayList<>(data.actionDataManagerTypes.values());
	}

	public List<DataManagerActionPlan> getDataManagerActionPlans(Class<? extends ActionDataManager> actionDataManagerType) {
		Object alias = data.dataManagerAliasMap.get(actionDataManagerType);
		List<DataManagerActionPlan> list = data.dataManagerActionPlanMap.get(alias);
		List<DataManagerActionPlan> result = new ArrayList<>();
		if (list != null) {
			result.addAll(list);
		}
		return list;
	}

}
