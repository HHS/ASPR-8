package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;

/**
 * Sets a property value on the indicated batch.
 *
 *
 */
@Immutable
public final class BatchPropertyValueAssignmentEvent implements Event {

	private final BatchId batchId;

	private final BatchPropertyId batchPropertyId;

	private final Object batchPropertyValue;

	
	public BatchPropertyValueAssignmentEvent(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue) {
		super();
		this.batchId = batchId;
		this.batchPropertyId = batchPropertyId;
		this.batchPropertyValue = batchPropertyValue;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public BatchPropertyId getBatchPropertyId() {
		return batchPropertyId;
	}

	public Object getBatchPropertyValue() {
		return batchPropertyValue;
	}

}
