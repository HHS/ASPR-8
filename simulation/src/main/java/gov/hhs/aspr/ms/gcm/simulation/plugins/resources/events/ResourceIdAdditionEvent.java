package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a resource id has been added.
 */
@Immutable
public record ResourceIdAdditionEvent(ResourceId resourceId, boolean timeTrackingPolicy) implements Event {
	/**
	 * Constructs the event
	 *
	 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *                           resource id is null
	 */
	public ResourceIdAdditionEvent {

		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}

	}

}
