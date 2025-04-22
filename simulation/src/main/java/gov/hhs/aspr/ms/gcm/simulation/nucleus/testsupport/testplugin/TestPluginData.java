package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * Thread safe plugin data container for associating plans with actor and data
 * manager aliases.
 */
@ThreadSafe
public class TestPluginData implements PluginData {

	private static class Data {
		private final Map<Object, List<TestReportPlan>> testReportPlanMap = new LinkedHashMap<>();

		private final Map<Object, List<TestActorPlan>> testActorPlanMap = new LinkedHashMap<>();

		private Map<Object, Supplier<TestDataManager>> testDataManagerSuppliers = new LinkedHashMap<>();

		private final Map<Object, List<TestDataManagerPlan>> testDataManagerPlanMap = new LinkedHashMap<>();

		private final Set<PluginId> pluginDependencies = new LinkedHashSet<>();

		private boolean locked;
		
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

			for (Object alias : data.testReportPlanMap.keySet()) {
				List<TestReportPlan> oldPlans = data.testReportPlanMap.get(alias);
				List<TestReportPlan> newPlans = new ArrayList<>();
				testReportPlanMap.put(alias, newPlans);
				for (TestReportPlan oldPlan : oldPlans) {
					TestReportPlan newPlan = new TestReportPlan(oldPlan);
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

			pluginDependencies.addAll(data.pluginDependencies);
			locked = data.locked;

		}

		/**
		 * Standard implementation consistent with the {@link #equals(Object)} method
		 */
		@Override
		public int hashCode() {
			return Objects.hash(testReportPlanMap, testActorPlanMap, testDataManagerSuppliers, testDataManagerPlanMap,
					pluginDependencies);
		}

		/**
		 * Two {@link Data} instances are equal if and only if
		 * their inputs are equal.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			return Objects.equals(testReportPlanMap, other.testReportPlanMap)
					&& Objects.equals(testActorPlanMap, other.testActorPlanMap)
					&& Objects.equals(testDataManagerSuppliers, other.testDataManagerSuppliers)
					&& Objects.equals(testDataManagerPlanMap, other.testDataManagerPlanMap)
					&& Objects.equals(pluginDependencies, other.pluginDependencies);
		}
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
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new TestPluginData(data);
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
		 *                           <ul>
		 *                           <li>{@linkplain TestError#NULL_ALIAS} if the alias
		 *                           is null</li>
		 *                           <li>{@linkplain TestError#NULL_PLAN}if the actor
		 *                           action plan is null</li>
		 *                           </ul>
		 */
		public Builder addTestActorPlan(final Object alias, TestActorPlan testActorPlan) {
			ensureDataMutability();
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
		 * Adds an report action plan associated with the alias
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain TestError#NULL_ALIAS} if the alias
		 *                           is null</li>
		 *                           <li>{@linkplain TestError#NULL_PLAN}if the actor
		 *                           action plan is null</li>
		 *                           </ul>
		 */
		public Builder addTestReportPlan(final Object alias, TestReportPlan testReportPlan) {
			ensureDataMutability();
			if (alias == null) {
				throw new ContractException(TestError.NULL_ALIAS);
			}

			if (testReportPlan == null) {
				throw new ContractException(TestError.NULL_PLAN);
			}

			List<TestReportPlan> list = data.testReportPlanMap.get(alias);

			if (list == null) {
				list = new ArrayList<>();
				data.testReportPlanMap.put(alias, list);
			}

			list.add(testReportPlan);

			return this;

		}

		/**
		 * Adds a test data manager to the test plugin via a supplier of
		 * TestDataManager. The supplier must be threadsafe.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@link TestError#NULL_ALIAS} is the alias is
		 *                           null</li>
		 *                           <li>{@link TestError#NULL_DATA_MANAGER_SUPPLIER} if
		 *                           the supplier is null</li>
		 *                           </ul>
		 */
		public Builder addTestDataManager(Object alias, Supplier<TestDataManager> supplier) {
			ensureDataMutability();
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
		 *                           <ul>
		 *                           <li>{@linkplain TestError#NULL_ALIAS} if the alias
		 *                           is null</li>
		 *                           <li>{@linkplain TestError#NULL_PLAN}if the actor
		 *                           action plan is null</li>
		 *                           </ul>
		 */
		public Builder addTestDataManagerPlan(final Object alias, TestDataManagerPlan testDataManagerPlan) {
			ensureDataMutability();
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

		/**
		 * Adds a plugin dependency
		 * 
		 * @throws ContractException {@linkplain TestError#NULL_PLUGIN_ID} if the plugin
		 *                           id is null
		 */
		public Builder addPluginDependency(final PluginId pluginId) {
			ensureDataMutability();
			if (pluginId == null) {
				throw new ContractException(TestError.NULL_PLUGIN_ID);
			}
			data.pluginDependencies.add(pluginId);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	@Override
	public Builder toBuilder() {
		return new Builder(data);
	}

	private final Data data;

	/**
	 * Returns a list of the test actor aliases
	 */
	public List<Object> getTestActorAliases() {
		return new ArrayList<>(data.testActorPlanMap.keySet());
	}

	/**
	 * Returns a list of the test report aliases
	 */
	public List<Object> getTestReportAliases() {
		return new ArrayList<>(data.testReportPlanMap.keySet());
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
	 * Returns the test report plans associated with the report alias
	 */
	public List<TestReportPlan> getTestReportPlans(Object alias) {
		List<TestReportPlan> result = new ArrayList<>();
		List<TestReportPlan> list = data.testReportPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Returns the plugin dependencies
	 */
	public Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(data.pluginDependencies);
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
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
	 * Two {@link TestPluginData} instances are equal if and only if
	 * their inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestPluginData other = (TestPluginData) obj;
		return Objects.equals(data, other.data);
	}
}
