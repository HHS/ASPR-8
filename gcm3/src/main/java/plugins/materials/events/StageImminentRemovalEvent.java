package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import plugins.materials.support.StageId;

@Immutable

public class StageImminentRemovalEvent implements Event {
	private final StageId stageId;

	public StageImminentRemovalEvent(StageId stageId) {
		super();
		this.stageId = stageId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StageImminentRemovalEvent [stageId=");
		builder.append(stageId);
		builder.append("]");
		return builder.toString();
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageImminentRemovalEvent> ALL_LABEL = new EventLabel<>(StageImminentRemovalEvent.class, LabelerId.ALL, StageImminentRemovalEvent.class);

	public static EventLabel<StageImminentRemovalEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageImminentRemovalEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(StageImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_LABEL)//
							.build();
	}

}
