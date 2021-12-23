package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

/**
 * Sets the offer state for a stage owned by the invoking materials producer. An
 * offered stage is available for transfer to another materials producer, but
 * has batches that cannot be mutated until the stage's offered state is either
 * set to false or is transferred to another materials producer.
 *
 *
 */
@Immutable
public final class StageOfferEvent implements Event {

	private final StageId stageId;

	private final boolean offer;

	
	public StageOfferEvent(StageId stageId, boolean offer) {
		super();
		this.stageId = stageId;
		this.offer = offer;
	}

	public StageId getStageId() {
		return stageId;
	}

	public boolean isOffer() {
		return offer;
	}

}
