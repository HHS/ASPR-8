package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import plugins.materials.support.BatchId;

@Immutable
public class BatchAdditionEvent implements Event {
	private final BatchId batchId;

	public BatchAdditionEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		return "BatchAdditionEvent [batchId=" + batchId + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchAdditionEvent> ALL_LABEL = new EventLabel<>(BatchAdditionEvent.class, LabelerId.ALL, BatchAdditionEvent.class);

	public static EventLabel<BatchAdditionEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchAdditionEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(BatchAdditionEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_LABEL)//
							.build();
	}

}
