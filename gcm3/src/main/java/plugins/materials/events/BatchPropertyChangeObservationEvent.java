package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;

@Immutable
public class BatchPropertyChangeObservationEvent implements Event {
	private final BatchId batchId;
	private final BatchPropertyId batchPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public BatchPropertyChangeObservationEvent(BatchId batchId, BatchPropertyId batchPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
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
		return "BatchPropertyChangeObservationEvent [batchId=" + batchId + ", batchPropertyId=" + batchPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchPropertyChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchPropertyChangeObservationEvent.class, LabelerId.ALL, BatchPropertyChangeObservationEvent.class);

	public static EventLabel<BatchPropertyChangeObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchPropertyChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchPropertyChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}
}
