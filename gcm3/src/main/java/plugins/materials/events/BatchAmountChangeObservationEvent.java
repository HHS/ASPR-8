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
public class BatchAmountChangeObservationEvent implements Event {
	private final BatchId batchId;
	private final double previousAmount;
	private final double currentAmount;

	public BatchAmountChangeObservationEvent(BatchId batchId, double previousAmount, double currentAmount) {
		super();
		this.batchId = batchId;
		this.previousAmount = previousAmount;
		this.currentAmount = currentAmount;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public double getPreviousAmount() {
		return previousAmount;
	}

	public double getCurrentAmount() {
		return currentAmount;
	}

	@Override
	public String toString() {
		return "BathAmountChangeObservationEvent [batchId=" + batchId + ", previousAmount=" + previousAmount + ", currentAmount=" + currentAmount + "]";
	}
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BatchAmountChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(BatchAmountChangeObservationEvent.class, LabelerId.ALL, BatchAmountChangeObservationEvent.class);

	public static EventLabel<BatchAmountChangeObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<BatchAmountChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, BatchAmountChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}
}
