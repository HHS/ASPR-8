package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import util.errors.ContractException;

@Immutable
public class ResourcePropertyDefinitionEvent implements Event {
	private final ResourceId resourceId;
	private final ResourcePropertyId resourcePropertyId;

	/**
	 * Constructs the event.  
	 * 
	 * @throws ContractException
	 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is null</li>
	 * <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if the resource property id is null</li>
	 * 
	 */
	public ResourcePropertyDefinitionEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		if(resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if(resourcePropertyId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}
		this.resourceId = resourceId;
		this.resourcePropertyId = resourcePropertyId;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public ResourcePropertyId getResourcePropertyId() {
		return resourcePropertyId;
	}

}
