package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

@Immutable
public record BatchPropertyUpdateEvent(BatchId batchId, BatchPropertyId batchPropertyId, Object previousPropertyValue,
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
