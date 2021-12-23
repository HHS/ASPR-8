package plugins.partitions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.partitions.support.Partition;

/**
 * Adds a population partition using the supplied population partition
 * definition.
 *
 * Only the component that creates a partition may remove that partition, but
 * other components may access the partition if they know its key. The resulting
 * population partition is actively maintained by the GCM and is accessed by the
 * key provided. This index is owned by the component that created it in that
 * only that component may remove it from the Environment. However, all
 * components have access to the partition via its key.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PartitionAdditionEvent implements Event {

	private final Partition partition;

	private final Object key;


	public PartitionAdditionEvent(Partition partition, Object key) {
		super();
		this.partition = partition;
		this.key = key;
	}

	public Partition getPartition() {
		return partition;
	}

	public Object getKey() {
		return key;
	}

}
