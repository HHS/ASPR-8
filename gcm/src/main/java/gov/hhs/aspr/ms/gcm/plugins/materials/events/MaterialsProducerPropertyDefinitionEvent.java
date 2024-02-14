package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event released by the materials data manager whenever a materials producer
 * property definition is added to the simulation.
 */
@Immutable
public record MaterialsProducerPropertyDefinitionEvent(MaterialsProducerPropertyId materialsProducerPropertyId)
		implements Event {

	/**
	 * Creates the event.
	 *
	 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *                           property id is null
	 */
	public MaterialsProducerPropertyDefinitionEvent {

		if (materialsProducerPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}