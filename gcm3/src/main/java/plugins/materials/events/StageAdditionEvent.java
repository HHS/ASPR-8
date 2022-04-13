package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.support.StageId;

@Immutable
public class StageCreationObservationEvent implements Event {
	private final StageId stageId;

	public StageCreationObservationEvent(StageId stageId) {
		super();
		this.stageId = stageId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		return "StageCreation [stageId=" + stageId + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageCreationObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageCreationObservationEvent.class, LabelerId.ALL, StageCreationObservationEvent.class);

	public static EventLabel<StageCreationObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageCreationObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageCreationObservationEvent.class, (context, event) -> ALL_LABEL);
	}
}
