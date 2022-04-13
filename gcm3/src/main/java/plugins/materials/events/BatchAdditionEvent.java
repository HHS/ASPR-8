package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
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

	private final static EventLabel<BatchAdditionEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchAdditionEvent.class, LabelerId.ALL, BatchAdditionEvent.class);

	public static EventLabel<BatchAdditionEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchAdditionEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchAdditionEvent.class, (context, event) -> ALL_LABEL);
	}

}
