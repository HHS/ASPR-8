package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event indicating the addition of a batch property for the given material.
 */
@Immutable
public record BatchPropertyDefinitionEvent(MaterialId materialId, BatchPropertyId batchPropertyId) implements Event {

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the batch property id is null</li>
	 *                           </ul>
	 */
	public BatchPropertyDefinitionEvent {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
