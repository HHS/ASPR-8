package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;

@ThreadSafe
public class TestPluginData implements PluginData {

	private static class Data {

		private Data() {
		}

		private Data(Data data) {
			
			for(Object alias : data.testActorPlanMap.keySet()) {
				List<TestActorPlan> oldPlans = data.testActorPlanMap.get(alias);
				List<TestActorPlan> newPlans = new ArrayList<>();
				testActorPlanMap.put(alias, newPlans);
				for(TestActorPlan oldPlan : oldPlans) {
					TestActorPlan newPlan = new TestActorPlan(oldPlan);
					newPlans.add(newPlan);
				}				
			}
			
			

			testDataManagerTypeMap.putAll(data.testDataManagerTypeMap);
			
			for(Object alias : data.testDataManagerPlanMap.keySet()) {
				List<TestDataManagerPlan> oldPlans = data.testDataManagerPlanMap.get(alias);
				List<TestDataManagerPlan> newPlans = new ArrayList<>();
				testDataManagerPlanMap.put(alias, newPlans);
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
			result = prime * result + ((testDataManagerTypeMap == null) ? 0 : testDataManagerTypeMap.hashCode());
			result = prime * result + ((testActorPlanMap == null) ? 0 : testActorPlanMap.hashCode());
			result = prime * result + ((testDataManagerPlanMap == null) ? 0 : testDataManagerPlanMap.hashCode());
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
			if (testDataManagerTypeMap == null) {
				if (other.testDataManagerTypeMap != null) {
					return false;
				}
			} else if (!testDataManagerTypeMap.equals(other.testDataManagerTypeMap)) {
				return false;
			}
			if (testActorPlanMap == null) {
				if (other.testActorPlanMap != null) {
					return false;
				}
			} else if (!testActorPlanMap.equals(other.testActorPlanMap)) {
				return false;
			}
			if (testDataManagerPlanMap == null) {
				if (other.testDataManagerPlanMap != null) {
					return false;
				}
			} else if (!testDataManagerPlanMap.equals(other.testDataManagerPlanMap)) {
				return false;
			}
			return true;
		}



		/*
		 * Map of action plans key by actor aliases
		 */
		private final Map<Object, List<TestActorPlan>> testActorPlanMap = new LinkedHashMap<>();

		private Map<Object, Class<? extends TestDataManager>> testDataManagerTypeMap = new LinkedHashMap<>();

		private final Map<Object, List<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();

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
				validate();
				return new TestPluginData(data);
			} finally {
				data = new Data();
			}
		}
		
		private void validate() {
			
			for(Object alias : data.testDataManagerPlanMap.keySet()) {
				if(!data.testDataManagerTypeMap.containsKey(alias)) {
					throw new ContractException(TestError.UNKNOWN_DATA_MANAGER_ALIAS,alias);
				}
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

			List<TestActorPlan> list = data.testActorPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.testActorPlanMap.put(alias, list);
			}

			list.add(testActorPlan);

			return this;

		}


		public Builder addTestDataManager(Object alias, Class<? extends TestDataManager> testDataManagerClass) {
			if (alias == null) {
				throw new RuntimeException("null alias");
			}
			if (testDataManagerClass == null) {
				throw new RuntimeException("null action data manager class");
			}
			data.testDataManagerTypeMap.put(alias, testDataManagerClass);			
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

			List<TestDataManagerPlan> list = data.testDataManagerPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.testDataManagerPlanMap.put(alias, list);
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

	public List<Object> getTestActorAliases() {
		return new ArrayList<>(data.testActorPlanMap.keySet());
	}

	

	public List<TestActorPlan> getTestActorPlans(Object alias) {
		List<TestActorPlan> result = new ArrayList<>();
		List<TestActorPlan> list = data.testActorPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	public Optional<Class<? extends TestDataManager>> getTestDataManagerType(Object alias) {
		Class<? extends TestDataManager> c = data.testDataManagerTypeMap.get(alias);
		return Optional.ofNullable(c);		
	}

	public List<TestDataManagerPlan> getTestDataManagerPlans(Object alias) {		
		List<TestDataManagerPlan> list = data.testDataManagerPlanMap.get(alias);
		List<TestDataManagerPlan> result = new ArrayList<>();
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}
	
	public List<Object> getTestDataManagerAliases(){
		return new ArrayList<>(data.testDataManagerTypeMap.keySet());
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
