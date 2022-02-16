package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

@ThreadSafe
public class TestPluginData implements PluginData {

	private static class Data {

		private Data() {
		}

		private Data(Data data) {
			
			for(Object alias : data.actorActionPlanMap.keySet()) {
				List<TestActorPlan> oldPlans = data.actorActionPlanMap.get(alias);
				List<TestActorPlan> newPlans = new ArrayList<>();
				actorActionPlanMap.put(alias, newPlans);
				for(TestActorPlan oldPlan : oldPlans) {
					TestActorPlan newPlan = new TestActorPlan(oldPlan);
					newPlans.add(newPlan);
				}				
			}
			
			actorAliasesMarkedForConstruction.addAll(data.actorAliasesMarkedForConstruction);

			actionDataManagerTypes.putAll(data.actionDataManagerTypes);
			
			for(Object alias : data.dataManagerActionPlanMap.keySet()) {
				List<TestDataManagerPlan> oldPlans = data.dataManagerActionPlanMap.get(alias);
				List<TestDataManagerPlan> newPlans = new ArrayList<>();
				dataManagerActionPlanMap.put(alias, newPlans);
				for(TestDataManagerPlan oldPlan : oldPlans) {
					TestDataManagerPlan newPlan = new TestDataManagerPlan(oldPlan);
					newPlans.add(newPlan);
				}				
			}			

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((actionDataManagerTypes == null) ? 0 : actionDataManagerTypes.hashCode());
			result = prime * result + ((actorActionPlanMap == null) ? 0 : actorActionPlanMap.hashCode());
			result = prime * result + ((actorAliasesMarkedForConstruction == null) ? 0 : actorAliasesMarkedForConstruction.hashCode());
			result = prime * result + ((dataManagerActionPlanMap == null) ? 0 : dataManagerActionPlanMap.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (actionDataManagerTypes == null) {
				if (other.actionDataManagerTypes != null) {
					return false;
				}
			} else if (!actionDataManagerTypes.equals(other.actionDataManagerTypes)) {
				return false;
			}
			if (actorActionPlanMap == null) {
				if (other.actorActionPlanMap != null) {
					return false;
				}
			} else if (!actorActionPlanMap.equals(other.actorActionPlanMap)) {
				return false;
			}
			if (actorAliasesMarkedForConstruction == null) {
				if (other.actorAliasesMarkedForConstruction != null) {
					return false;
				}
			} else if (!actorAliasesMarkedForConstruction.equals(other.actorAliasesMarkedForConstruction)) {
				return false;
			}
			if (dataManagerActionPlanMap == null) {
				if (other.dataManagerActionPlanMap != null) {
					return false;
				}
			} else if (!dataManagerActionPlanMap.equals(other.dataManagerActionPlanMap)) {
				return false;
			}
			return true;
		}

		/*
		 * Map of action plans key by actor aliases
		 */
		private final Map<Object, List<TestActorPlan>> actorActionPlanMap = new LinkedHashMap<>();

		/*
		 * Contains the alias values for which actor construction must be
		 * handled by the Action Plugin Initializer
		 */
		private Set<Object> actorAliasesMarkedForConstruction = new LinkedHashSet<>();

		private Map<Object, Class<? extends TestDataManager>> actionDataManagerTypes = new LinkedHashMap<>();

		private final Map<Object, List<TestDataManagerPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	}

	private TestPluginData(Data data) {
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
		public TestPluginData build() {
			try {
				return new TestPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds an actor action plan associated with the alias
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null</li>
		 *             <li>if the actor action plan is null</li>
		 */
		public Builder addTestActorPlan(final Object alias, TestActorPlan testActorPlan) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (testActorPlan == null) {
				throw new RuntimeException("null action plan");
			}

			List<TestActorPlan> list = data.actorActionPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.actorActionPlanMap.put(alias, list);
			}

			list.add(testActorPlan);

			return this;

		}

		/**
		 * Causes the action plugin to create the actor as an ActionActor
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null
		 * 
		 */
		public Builder addTestActor(Object alias) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			data.actorAliasesMarkedForConstruction.add(alias);
			return this;
		}

		

		public Builder addTestDataManager(Object alias, Class<? extends TestDataManager> testDataManagerClass) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (testDataManagerClass == null) {
				throw new RuntimeException("null action data manager class");
			}
			data.actionDataManagerTypes.put(alias, testDataManagerClass);			
			return this;
		}

		/**
		 * Adds an data manager action plan associated with the alias
		 * 
		 * @throws RuntimeException
		 *             <li>if the alias is null</li>
		 *             <li>if the actor action plan is null</li>
		 */
		public Builder addTestDataManagerPlan(final Object alias, TestDataManagerPlan testDataManagerPlan) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (testDataManagerPlan == null) {
				throw new RuntimeException("null action plan");
			}

			List<TestDataManagerPlan> list = data.dataManagerActionPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.dataManagerActionPlanMap.put(alias, list);
			}

			list.add(testDataManagerPlan);

			return this;

		}

	}

	@Override
	public Builder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	private final Data data;

	public List<Object> getActorsRequiringConstruction() {
		return new ArrayList<>(data.actorAliasesMarkedForConstruction);
	}

	public List<Object> getActorsRequiringPlanning() {
		return new ArrayList<>(data.actorActionPlanMap.keySet());
	}

	public List<TestActorPlan> getTestActorPlans(Object alias) {
		List<TestActorPlan> result = new ArrayList<>();
		List<TestActorPlan> list = data.actorActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	public Optional<Class<? extends TestDataManager>> getTestDataManagerType(Object alias) {
		Class<? extends TestDataManager> c = data.actionDataManagerTypes.get(alias);
		return Optional.ofNullable(c);		
	}

	public List<TestDataManagerPlan> getTestDataManagerPlans(Object alias) {		
		List<TestDataManagerPlan> list = data.dataManagerActionPlanMap.get(alias);
		List<TestDataManagerPlan> result = new ArrayList<>();
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}
	
	public List<Object> getTestDataManagerAliases(){
		return new ArrayList<>(data.actionDataManagerTypes.keySet());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestPluginData)) {
			return false;
		}
		TestPluginData other = (TestPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
