package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;

/**
 * An observation event indicating that a person's resource level has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class PersonResourceUpdateEvent implements Event {
	private final PersonId personId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	/**
	 * Constructs the event
	 */
	public PersonResourceUpdateEvent(final PersonId personId, final ResourceId resourceId, final long previousResourceLevel, final long currentResourceLevel) {
		this.personId = personId;
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
	 * Returns the current resource level used to create this event
	 */
	public long getCurrentResourceLevel() {
		return currentResourceLevel;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the previous resource level used to create this event
	 */
	public long getPreviousResourceLevel() {
		return previousResourceLevel;
	}

	

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataManager personDataManager = simulationContext.getDataManager(PersonDataManager.class).get();
		if (!personDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID, personId);
		}
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
		RegionDataManager regionDataManager = simulationContext.getDataManager(RegionDataManager.class).get();
		if (!regionDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private static void validateResourceId(SimulationContext simulationContext, ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourceDataManager resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class).get();
		if (!resourceDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private static enum LabelerId implements EventLabelerId {
		COMPARTMENT_RESOURCE, REGION_RESOURCE, PERSON_RESOURCE, RESOURCE
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on region id
	 * and resource id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public static EventLabel<PersonResourceUpdateEvent> getEventLabelByRegionAndResource(SimulationContext simulationContext, RegionId regionId, ResourceId resourceId) {
		validateRegionId(simulationContext, regionId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.REGION_RESOURCE, PersonResourceUpdateEvent.class, regionId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link PersonResourceUpdateEvent}
	 * events that uses region id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonResourceUpdateEvent> getEventLabelerForRegionAndResource(RegionDataManager regionDataManager) {
		return new SimpleEventLabeler<>(LabelerId.REGION_RESOURCE, PersonResourceUpdateEvent.class, (context, event) -> {
			RegionId regionId = regionDataManager.getPersonRegion(event.getPersonId());
			return new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.REGION_RESOURCE, PersonResourceUpdateEvent.class, regionId, event.getResourceId());
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on person id
	 * and resource id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public static EventLabel<PersonResourceUpdateEvent> getEventLabelByPersonAndResource(SimulationContext simulationContext, PersonId personId, ResourceId resourceId) {
		validatePersonId(simulationContext, personId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.PERSON_RESOURCE, PersonResourceUpdateEvent.class, personId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link PersonResourceUpdateEvent}
	 * events that uses person id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonResourceUpdateEvent> getEventLabelerForPersonAndResource() {
		return new SimpleEventLabeler<>(LabelerId.PERSON_RESOURCE, PersonResourceUpdateEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.PERSON_RESOURCE, PersonResourceUpdateEvent.class, event.getPersonId(), event.getResourceId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceUpdateEvent} events. Matches on resource
	 * id.
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
	 */
	public static EventLabel<PersonResourceUpdateEvent> getEventLabelByResource(SimulationContext simulationContext, ResourceId resourceId) {
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.RESOURCE, PersonResourceUpdateEvent.class, resourceId);
	}
	/**
	 * Returns an event labeler for {@link PersonResourceUpdateEvent}
	 * events that uses resource id. Automatically added at
	 * initialization.
	 */

	public static EventLabeler<PersonResourceUpdateEvent> getEventLabelerForResource() {
		return new SimpleEventLabeler<>(LabelerId.RESOURCE, PersonResourceUpdateEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.RESOURCE, PersonResourceUpdateEvent.class, event.getResourceId()));
	}

	/**
	 * Returns the resource id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
