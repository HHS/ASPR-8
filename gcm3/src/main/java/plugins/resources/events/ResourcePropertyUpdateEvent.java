package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.support.ResourceError;
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

	private static enum LabelerId implements EventLabelerId {
		RESOURCE_AND_PROPERTY
	}

	private static void validateResourceId(SimulationContext simulationContext, ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourceDataManager resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class);
		if (!resourceDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID);
		}
	}

	private static void validateResourcePropertyId(SimulationContext simulationContext, ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}
		ResourceDataManager resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class);
		if (!resourceDataManager.resourcePropertyIdExists(resourceId, resourcePropertyId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link ResourcePropertyUpdateEvent} events. Matches on resource id and
	 * resource property id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID}
	 *             if the resource property id is unknown</li> *
	 */
	public static EventLabel<ResourcePropertyUpdateEvent> getEventLabel(SimulationContext simulationContext, ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		validateResourceId(simulationContext, resourceId);
		validateResourcePropertyId(simulationContext, resourceId, resourcePropertyId);
		return _getEventLabel(resourceId, resourcePropertyId);
	}
	
	private static EventLabel<ResourcePropertyUpdateEvent> _getEventLabel(ResourceId resourceId, ResourcePropertyId resourcePropertyId) {
		
		return EventLabel	.builder(ResourcePropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.RESOURCE_AND_PROPERTY)//
							.addKey(resourcePropertyId)//
							.addKey(resourceId)//
							.build();
	}

	/**
	 * Returns an event labeler for {@link ResourcePropertyUpdateEvent} events
	 * that uses resource id and resource property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<ResourcePropertyUpdateEvent> getEventLabeler() {
		return EventLabeler	.builder(ResourcePropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.RESOURCE_AND_PROPERTY)//
							.setLabelFunction((context, event) -> _getEventLabel(event.getResourceId(), event.getResourcePropertyId()))//
							.build();
	}

	/**
	 * Returns the resource property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourcePropertyId;
	}

}
