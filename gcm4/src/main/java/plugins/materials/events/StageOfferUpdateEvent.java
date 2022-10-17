package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

@Immutable
public class StageOfferUpdateEvent implements Event {
	private final StageId stageId;
	private final boolean previousOfferState;
	private final boolean currentOfferState;

	public StageOfferUpdateEvent(StageId stageId, boolean previousOfferState, boolean currentOfferState) {
		super();
		this.stageId = stageId;
		this.previousOfferState = previousOfferState;
		this.currentOfferState = currentOfferState;
	}

	public StageId getStageId() {
		return stageId;
	}

	public boolean isPreviousOfferState() {
		return previousOfferState;
	}

	public boolean isCurrentOfferState() {
		return currentOfferState;
	}

	@Override
	public String toString() {
		return "StageOfferUpdateEvent [stageId=" + stageId + ", previousOfferState=" + previousOfferState + ", currentOfferState=" + currentOfferState + "]";
	}

	
	
}
