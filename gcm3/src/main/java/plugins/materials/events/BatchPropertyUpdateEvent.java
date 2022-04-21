package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;

@Immutable
public class BatchPropertyUpdateEvent implements Event {
	private final BatchId batchId;
	private final BatchPropertyId batchPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public BatchPropertyUpdateEvent(BatchId batchId, BatchPropertyId batchPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
		super();
		this.batchId = batchId;
		this.batchPropertyId = batchPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public BatchPropertyId getBatchPropertyId() {
		return batchPropertyId;
	}

	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	@Override
	public String toString() {
		return "BatchPropertyUpdateEvent [batchId=" + batchId + ", batchPropertyId=" + batchPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}

	
}
