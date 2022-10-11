package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * An observation event indicating that a resource property has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class ResourcePropertyUpdateEvent implements Event {
	private final ResourceId resourceId;
	private final ResourcePropertyId resourcePropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs the event
	 */
	public ResourcePropertyUpdateEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object previousPropertyValue, Object currentPropertyValue) {
		super();
		this.resourceId = resourceId;
		this.resourcePropertyId = resourcePropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
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
	 * Returns the previous property value used to create this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns the current property value used to create this event
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Returns the resource property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourcePropertyId;
	}

}
