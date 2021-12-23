package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchConstructionInfo;

/**
 * 
 * Creates a new batch from a {@link BatchConstructionInfo}
 *
 */
@Immutable
public final class BatchConstructionEvent implements Event {

	private final BatchConstructionInfo batchConstructionInfo;

	
	public BatchConstructionEvent(BatchConstructionInfo batchConstructionInfo) {
		super();
		this.batchConstructionInfo = batchConstructionInfo;
	}

	public BatchConstructionInfo getBatchConstructionInfo() {
		return batchConstructionInfo;
	}

}
