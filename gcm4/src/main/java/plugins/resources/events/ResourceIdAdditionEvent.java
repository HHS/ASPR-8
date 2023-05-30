package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.errors.ContractException;

/**
 * An observation event indicating that a resource id has been added.
 *
 */

@Immutable
public record ResourceIdAdditionEvent(ResourceId resourceId,
									  boolean timeTrackingPolicy) implements Event {
	/**
	 * Constructs the event
	 *
	 * @throws ContractException <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *                           resource id is null</li>                          
	 */
	public ResourceIdAdditionEvent {

		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		

	}

}
