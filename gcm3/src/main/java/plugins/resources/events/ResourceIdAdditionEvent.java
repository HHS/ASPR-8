package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceId;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * An observation event indicating that a resource id has been added.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public final class ResourceIdAdditionEvent implements Event {
	private final ResourceId resourceId;
	private final TimeTrackingPolicy timeTrackingPolicy;

	public ResourceIdAdditionEvent(ResourceId resourceId, TimeTrackingPolicy timeTrackingPolicy) {
		super();
		this.resourceId = resourceId;
		this.timeTrackingPolicy = timeTrackingPolicy;
	}

	
	public TimeTrackingPolicy getTimeTrackingPolicy() {
		return timeTrackingPolicy;
	}


	public ResourceId getResourceId() {
		return resourceId;
	}
	
}
