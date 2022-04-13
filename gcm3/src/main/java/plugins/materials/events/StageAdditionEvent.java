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
public class StageAdditionEvent implements Event {
	private final StageId stageId;

	public StageAdditionEvent(StageId stageId) {
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

	private final static EventLabel<StageAdditionEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageAdditionEvent.class, LabelerId.ALL, StageAdditionEvent.class);

	public static EventLabel<StageAdditionEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageAdditionEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageAdditionEvent.class, (context, event) -> ALL_LABEL);
	}
}
