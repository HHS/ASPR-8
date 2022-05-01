package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import util.errors.ContractException;

/**
 * Thread safe plugin data container for associating plans with actor and data
 * manager aliases.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public class TestPluginData implements PluginData {

	private static class Data {

		private Data() {
		}

		private Data(Data data) {

			for (Object alias : data.testActorPlanMap.keySet()) {
				List<TestActorPlan> oldPlans = data.testActorPlanMap.get(alias);
				List<TestActorPlan> newPlans = new ArrayList<>();
				testActorPlanMap.put(alias, newPlans);
				for (TestActorPlan oldPlan : oldPlans) {
					TestActorPlan newPlan = new TestActorPlan(oldPlan);
					newPlans.add(newPlan);
				}
			}

			testDataManagerSuppliers.putAll(data.testDataManagerSuppliers);

			for (Object alias : data.testDataManagerPlanMap.keySet()) {
				List<TestDataManagerPlan> oldPlans = data.testDataManagerPlanMap.get(alias);
				List<TestDataManagerPlan> newPlans = new ArrayList<>();
				testDataManagerPlanMap.put(alias, newPlans);
				for (TestDataManagerPlan oldPlan : oldPlans) {
					TestDataManagerPlan newPlan = new TestDataManagerPlan(oldPlan);
					newPlans.add(newPlan);
				}
			}

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((testActorPlanMap == null) ? 0 : testActorPlanMap.hashCode());
			result = prime * result + ((testDataManagerPlanMap == null) ? 0 : testDataManagerPlanMap.hashCode());
			result = prime * result + ((testDataManagerSuppliers == null) ? 0 : testDataManagerSuppliers.hashCode());
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
			if (testDataManagerSuppliers == null) {
				if (other.testDataManagerSuppliers != null) {
					return false;
				}
			} else if (!testDataManagerSuppliers.equals(other.testDataManagerSuppliers)) {
				return false;
			}
			return true;
		}

		/*
		 * Map of action plans key by actor aliases
		 */
		private final Map<Object, List<TestActorPlan>> testActorPlanMap = new LinkedHashMap<>();

		private Map<Object, Supplier<TestDataManager>> testDataManagerSuppliers = new LinkedHashMap<>();

		private final Map<Object, List<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();

	}

	private TestPluginData(Data data) {
		this.data = data;

	}

	/**
	 * Returns a builder for TestPluginData
	 */
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

			for (Object alias : data.testDataManagerPlanMap.keySet()) {
				if (!data.testDataManagerSuppliers.containsKey(alias)) {
					throw new ContractException(TestError.UNKNOWN_DATA_MANAGER_ALIAS, alias);
				}
			}
		}

		/**
		 * Adds an actor action plan associated with the alias
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain TestError#NULL_ALIAS} if the alias is
		 *             null</li>
		 *             <li>{@linkplain TestError#NULL_PLAN}if the actor action
		 *             plan is null</li>
		 */
		public Builder addTestActorPlan(final Object alias, TestActorPlan testActorPlan) {
			if (alias == null) {
				throw new ContractException(TestError.NULL_ALIAS);
			}

			if (testActorPlan == null) {
				throw new ContractException(TestError.NULL_PLAN);
			}

			List<TestActorPlan> list = data.testActorPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.testActorPlanMap.put(alias, list);
			}

			list.add(testActorPlan);

			return this;

		}

		/**
		 * Adds a test data manager to the test plugin via a supplier of
		 * TestDataManager. The supplier must be threadsafe.
		 * 
		 * @throws ContractException
		 * 
		 */
		public Builder addTestDataManager(Object alias, Supplier<TestDataManager> supplier) {
			if (alias == null) {
				throw new ContractException(TestError.NULL_ALIAS);
			}
			if (supplier == null) {
				throw new ContractException(TestError.NULL_DATA_MANAGER_SUPPLIER);
			}
			data.testDataManagerSuppliers.put(alias, supplier);
			return this;
		}

		/**
		 * Adds an data manager action plan associated with the alias
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain TestError#NULL_ALIAS} if the alias is
		 *             null</li>
		 *             <li>{@linkplain TestError#NULL_PLAN}if the actor action
		 *             plan is null</li>
		 */
		public Builder addTestDataManagerPlan(final Object alias, TestDataManagerPlan testDataManagerPlan) {

			if (alias == null) {
				throw new ContractException(TestError.NULL_ALIAS);
			}
			if (testDataManagerPlan == null) {
				throw new ContractException(TestError.NULL_PLAN);
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

	/**
	 * Returns a Builder that is initialized to contain the plans and suppliers
	 * of data managers contained in this TestPluginData.
	 */
	@Override
	public Builder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	private final Data data;

	/**
	 * Returns a list of the test actor aliases
	 */
	public List<Object> getTestActorAliases() {
		return new ArrayList<>(data.testActorPlanMap.keySet());
	}

	/**
	 * Returns the test actor plans associated with the actor alias
	 */
	public List<TestActorPlan> getTestActorPlans(Object alias) {
		List<TestActorPlan> result = new ArrayList<>();
		List<TestActorPlan> list = data.testActorPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Returns a test data manager instance from the given alias.
	 */
	@SuppressWarnings("unchecked")
	public <T extends TestDataManager> Optional<T> getTestDataManager(Object alias) {
		TestDataManager result = null;
		Supplier<TestDataManager> supplier = data.testDataManagerSuppliers.get(alias);
		if (supplier != null) {
			result = supplier.get();
			result.setAlias(alias);
		}
		return Optional.ofNullable((T) result);
	}

	/**
	 * Returns the test data manager plans associated with the actor alias
	 */
	public List<TestDataManagerPlan> getTestDataManagerPlans(Object alias) {
		List<TestDataManagerPlan> list = data.testDataManagerPlanMap.get(alias);
		List<TestDataManagerPlan> result = new ArrayList<>();
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Returns a list of the test data manager aliases
	 */
	public List<Object> getTestDataManagerAliases() {
		return new ArrayList<>(data.testDataManagerSuppliers.keySet());
	}

	/**
	 * Hash code implementation consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/**
	 * TestPluginData instances are equal if and only if they contain identical
	 * plans and suppliers of test data managers.
	 */
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
