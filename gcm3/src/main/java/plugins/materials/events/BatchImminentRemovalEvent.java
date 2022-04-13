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
public class BatchImminentRemovalEvent implements Event {
	private final BatchId batchId;

	public BatchImminentRemovalEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchImminentRemovalEvent [batchId=");
		builder.append(batchId);
		builder.append("]");
		return builder.toString();
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchImminentRemovalEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchImminentRemovalEvent.class, LabelerId.ALL, BatchImminentRemovalEvent.class);

	public static EventLabel<BatchImminentRemovalEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchImminentRemovalEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchImminentRemovalEvent.class, (context, event) -> ALL_LABEL);
	}

}
