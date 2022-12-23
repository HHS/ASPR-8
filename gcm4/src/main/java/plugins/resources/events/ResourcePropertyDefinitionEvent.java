package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

@Immutable
public record ResourcePropertyDefinitionEvent(ResourceId resourceId,
											  ResourcePropertyId resourcePropertyId) implements Event {
	/**
	 * Constructs the event.
	 *
	 * @throws ContractException <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is null</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the resource property id is null</li>
	 */
	public ResourcePropertyDefinitionEvent {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (resourcePropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
