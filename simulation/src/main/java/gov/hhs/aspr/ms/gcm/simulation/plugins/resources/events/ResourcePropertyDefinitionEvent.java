package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

@Immutable
public record ResourcePropertyDefinitionEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId,
		Object resourcePropertyValue) implements Event {
	/**
	 * Constructs the event.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the resource property id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the resource property value is null</li>
	 *                           </ul>
	 */
	public ResourcePropertyDefinitionEvent {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (resourcePropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (resourcePropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

}
