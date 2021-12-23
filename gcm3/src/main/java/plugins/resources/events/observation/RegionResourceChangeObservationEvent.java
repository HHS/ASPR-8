package plugins.resources.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.ContractException;

/**
 * An observation event indicating that a region's resource level has changed.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class RegionResourceChangeObservationEvent implements Event {
	private final RegionId regionId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	/**
	 * Constructs the event
	 */
	public RegionResourceChangeObservationEvent(RegionId regionId, ResourceId resourceId, long previousResourceLevel, long currentResourceLevel) {
		super();
		this.regionId = regionId;
		this.resourceId = resourceId;
		this.previousResourceLevel = previousResourceLevel;
		this.currentResourceLevel = currentResourceLevel;
	}

	/**
	 * Returns the resource id used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the region id used to create this event
	 */
	public RegionId getRegionId() {
		return regionId;
	}

	/**
	 * Returns the previous resource level used to create this event
	 */
	public long getPreviousResourceLevel() {
		return previousResourceLevel;
	}

	/**
	 * Returns the current resource level used to create this event
	 */
	public long getCurrentResourceLevel() {
		return currentResourceLevel;
	}

	private static enum LabelerId implements EventLabelerId {
		REGION_RESOURCE
	}

	private static void validateRegionId(Context context, RegionId regionId) {
		if (regionId == null) {
			context.throwContractException(RegionError.NULL_REGION_ID);
		}
		RegionDataView regionDataView = context.getDataView(RegionDataView.class).get();
		if (!regionDataView.regionIdExists(regionId)) {
			context.throwContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	private static void validateResourceId(Context context, ResourceId resourceId) {
		if (resourceId == null) {
			context.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();
		if (!resourceDataView.resourceIdExists(resourceId)) {
			context.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link RegionResourceChangeObservationEvent} events. Matches on
	 * region id and resource id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *             region id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *             the region id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public static EventLabel<RegionResourceChangeObservationEvent> getEventLabelByRegionAndResource(Context context, RegionId regionId, ResourceId resourceId) {
		validateRegionId(context, regionId);
		validateResourceId(context, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.REGION_RESOURCE, RegionResourceChangeObservationEvent.class, regionId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link RegionResourceChangeObservationEvent}
	 * events that uses region id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<RegionResourceChangeObservationEvent> getEventLabelerForRegionAndResource() {
		return new SimpleEventLabeler<>(LabelerId.REGION_RESOURCE, RegionResourceChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.REGION_RESOURCE, RegionResourceChangeObservationEvent.class, event.getRegionId(), event.getResourceId()));
	}
	
	/**
	 * Returns the resource id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
