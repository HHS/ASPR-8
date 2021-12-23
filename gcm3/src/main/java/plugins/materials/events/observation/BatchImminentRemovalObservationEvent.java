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
public class BatchImminentRemovalObservationEvent implements Event {
	private final BatchId batchId;

	public BatchImminentRemovalObservationEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchImminentRemovalObservationEvent [batchId=");
		builder.append(batchId);
		builder.append("]");
		return builder.toString();
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchImminentRemovalObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchImminentRemovalObservationEvent.class, LabelerId.ALL, BatchImminentRemovalObservationEvent.class);

	public static EventLabel<BatchImminentRemovalObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchImminentRemovalObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchImminentRemovalObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
