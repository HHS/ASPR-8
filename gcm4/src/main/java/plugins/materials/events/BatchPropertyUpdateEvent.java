package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialsError;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

@Immutable
public record BatchPropertyUpdateEvent(BatchId batchId,
									   BatchPropertyId batchPropertyId,
									   Object previousPropertyValue,
									   Object currentPropertyValue) implements Event {
	public BatchPropertyUpdateEvent {
		if (batchId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_ID);
		}

		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (previousPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}

		if (currentPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}


}
