package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.support.StageId;

@Immutable

public class StageImminentRemovalObservationEvent implements Event {
	private final StageId stageId;

	public StageImminentRemovalObservationEvent(StageId stageId) {
		super();
		this.stageId = stageId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StageImminentRemovalObservationEvent [stageId=");
		builder.append(stageId);
		builder.append("]");
		return builder.toString();
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageImminentRemovalObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageImminentRemovalObservationEvent.class, LabelerId.ALL, StageImminentRemovalObservationEvent.class);

	public static EventLabel<StageImminentRemovalObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageImminentRemovalObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageImminentRemovalObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
