package plugins.partitions.datacontainers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nucleus.AgentId;
import plugins.components.support.ComponentError;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PopulationPartition;
import util.ContractException;

/**
 * Mutable data manager that backs the {@linkplain PartitionDataView}. This
 * data manager is for internal use by the {@link PartitionsPlugin} and should
 * not be published.
 * 
 * 
 * @author Shawn Hatch
 *
 */

public final class PartitionDataManager {

	private final Map<Object, PopulationPartition> keyToPopulationPartitionMap = new LinkedHashMap<>();

	private final Set<Object> safePartitionKeys = Collections.unmodifiableSet(keyToPopulationPartitionMap.keySet());

	private final Map<Object, AgentId> keyToAgentIdMap = new LinkedHashMap<>();

	/**
	 * Adds a population partition for the given key and component id. The key
	 * must not duplicate an existing key.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_PARTITION_KEY} if the key
	 *             is null</li>
	 *             <li>{@linkplain ComponentError#NULL_AGENT_ID} if the
	 *             agent id is null</li>
	 *             <li>{@linkplain PartitionError#NULL_POPULATION_PARTITION} if
	 *             population partition is null</li>
	 *             <li>{@linkplain PartitionError#DUPLICATE_PARTITION} if the
	 *             key is already allocated to another population partition</li>
	 * 
	 */
	public void addPartition(final Object key, final AgentId agentId, final PopulationPartition populationPartition) {
		if (key == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_KEY);
		}
		if (agentId == null) {
			throw new ContractException(ComponentError.NULL_AGENT_ID);
		}
		if (populationPartition == null) {
			throw new ContractException(PartitionError.NULL_POPULATION_PARTITION);
		}
		if (keyToAgentIdMap.containsKey(key)) {
			throw new ContractException(PartitionError.DUPLICATE_PARTITION);
		}
		keyToAgentIdMap.put(key, agentId);
		keyToPopulationPartitionMap.put(key, populationPartition);
	}

	/**
	 * Returns the set of keys for population partitions
	 */
	public Set<Object> getKeys() {
		return safePartitionKeys;
	}

	/**
	 * Returns the component that owns the population partition for the given
	 * key. Returns null if the key is not recognized.
	 */
	public AgentId getOwningAgentId(final Object key) {
		return keyToAgentIdMap.get(key);
	}

	/**
	 * Returns the population partition associated with the given key. Returns
	 * null if the key is not recognized.
	 */
	public PopulationPartition getPopulationPartition(final Object key) {
		return keyToPopulationPartitionMap.get(key);
	}

	/**
	 * Returns true if and only if there is a population partition associated
	 * with the given key.
	 */
	public boolean partitionExists(final Object key) {
		return keyToPopulationPartitionMap.containsKey(key);
	}

	/**
	 * Returns true if and only if there are not population partitions contained
	 * in this manager.
	 */
	public boolean isEmpty() {
		return keyToPopulationPartitionMap.isEmpty();
	}

	/**
	 * Removes the population index for the given key if that key is present.
	 */
	public void removePartition(final Object key) {
		keyToPopulationPartitionMap.remove(key);
		keyToAgentIdMap.remove(key);
	}

}