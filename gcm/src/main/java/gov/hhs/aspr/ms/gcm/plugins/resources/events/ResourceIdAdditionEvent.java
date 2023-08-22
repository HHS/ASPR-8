package gov.hhs.aspr.ms.gcm.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

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
