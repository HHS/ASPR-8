package plugins.resources.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * Event for setting a resource property value.
 *
 */
@Immutable
public final class ResourcePropertyValueAssignmentEvent implements Event {

	private final ResourceId resourceId;

	private final ResourcePropertyId resourcePropertyId;

	private final Object resourcePropertyValue;

	/**
	 * Constructs the event
	 */
	public ResourcePropertyValueAssignmentEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object resourcePropertyValue) {
		this.resourceId = resourceId;
		this.resourcePropertyId = resourcePropertyId;
		this.resourcePropertyValue = resourcePropertyValue;
	}

	/**
	 * Returns the resource id used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}
	
	/**
	 * Returns the resource property id used to create this event
	 */
	public ResourcePropertyId getResourcePropertyId() {
		return resourcePropertyId;
	}

	/**
	 * Returns the resource property value used to create this event
	 */
	public Object getResourcePropertyValue() {
		return resourcePropertyValue;
	}

}
