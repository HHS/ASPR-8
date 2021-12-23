package plugins.partitions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.NucleusError;
import util.ContractException;

/**
 * Removes the partition associated with the given key. Partitions may only be
 * removed by the components that create them.
 *
 */
@Immutable
public final class PartitionRemovalEvent implements Event {
	private Object key;

	/**
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population index
	 *             <li>{@link NucleusError#PARTITION_DELETION_BY_NON_OWNER} if
	 *             the invoker is not the component that created the index
	 *
	 */
	public PartitionRemovalEvent(Object key) {
		super();
		this.key = key;
	}

	public Object getKey() {
		return key;
	}

}
