package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * Event indicating the addition of a region property
 */
@Immutable
public record RegionPropertyDefinitionEvent(RegionPropertyId regionPropertyId) implements Event {

	/**
	 * Constructs the event
	 *
	 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *                           region property id is null
	 */
	public RegionPropertyDefinitionEvent {
		if (regionPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

	}

}
