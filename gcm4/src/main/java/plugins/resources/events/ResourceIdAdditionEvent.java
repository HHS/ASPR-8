package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;

/**
 * An observation event indicating that a resource id has been added.
 *
 * @author Shawn Hatch
 */

@Immutable
public record ResourceIdAdditionEvent(ResourceId resourceId,
									  TimeTrackingPolicy timeTrackingPolicy) implements Event {
	/**
	 * Constructs the event
	 *
	 * @throws ContractException <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *                           resource id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_TIME_TRACKING_POLICY} if
	 *                           the time tracking policy is null</li>
	 */
	public ResourceIdAdditionEvent {

		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (timeTrackingPolicy == null) {
			throw new ContractException(PropertyError.NULL_TIME_TRACKING_POLICY);
		}

	}

}
