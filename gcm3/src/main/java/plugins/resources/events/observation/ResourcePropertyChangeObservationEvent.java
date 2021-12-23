package plugins.resources.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import util.ContractException;
/**
 * An observation event indicating that a resource property has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class ResourcePropertyChangeObservationEvent implements Event {
	private final ResourceId resourceId;
	private final ResourcePropertyId resourcePropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs the event
	 */
	public ResourcePropertyChangeObservationEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId, Object previousPropertyValue, Object currentPropertyValue) {
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

	private static enum LabelerId implements EventLabelerId {
		RESOURCE_AND_PROPERTY
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

	private static void validateResourcePropertyId(Context context, ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			context.throwContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}
		ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();
		if (!resourceDataView.resourcePropertyIdExists(resourceId, resourcePropertyId)) {
			context.throwContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link ResourcePropertyChangeObservationEvent} events. Matches on
	 * resource id and resource property id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *

	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             resource property id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is unknown</li>	 *             
	 */
	public static EventLabel<ResourcePropertyChangeObservationEvent> getEventLabel(Context context, ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		validateResourceId(context, resourceId);
		validateResourcePropertyId(context, resourceId, resourcePropertyId);
		return new MultiKeyEventLabel<>(resourcePropertyId, LabelerId.RESOURCE_AND_PROPERTY, ResourcePropertyChangeObservationEvent.class, resourceId, resourcePropertyId);
	}

	/**
	 * Returns an event labeler for {@link ResourcePropertyChangeObservationEvent}
	 * events that uses resource id and resource property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<ResourcePropertyChangeObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.RESOURCE_AND_PROPERTY, ResourcePropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getResourcePropertyId(), LabelerId.RESOURCE_AND_PROPERTY, ResourcePropertyChangeObservationEvent.class, event.getResourceId(), event.getResourcePropertyId()));
	}
	/**
	 * Returns the resource property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourcePropertyId;
	}

}
