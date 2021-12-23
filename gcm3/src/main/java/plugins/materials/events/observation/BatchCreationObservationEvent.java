package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.support.BatchId;

@Immutable
public class BatchCreationObservationEvent implements Event {
	private final BatchId batchId;

	public BatchCreationObservationEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		return "BatchCreationObservationEvent [batchId=" + batchId + "]";
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchCreationObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchCreationObservationEvent.class, LabelerId.ALL, BatchCreationObservationEvent.class);

	public static EventLabel<BatchCreationObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchCreationObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchCreationObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
