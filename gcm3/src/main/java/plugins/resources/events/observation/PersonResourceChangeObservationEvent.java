package plugins.resources.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.ContractException;

/**
 * An observation event indicating that a person's resource level has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class PersonResourceChangeObservationEvent implements Event {
	private final PersonId personId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	/**
	 * Constructs the event
	 */
	public PersonResourceChangeObservationEvent(final PersonId personId, final ResourceId resourceId, final long previousResourceLevel, final long currentResourceLevel) {
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

	private static void validateCompartmentId(SimulationContext simulationContext, CompartmentId compartmentId) {
		if (compartmentId == null) {
			simulationContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}
		CompartmentDataView compartmentDataView = simulationContext.getDataView(CompartmentDataView.class).get();
		if (!compartmentDataView.compartmentIdExists(compartmentId)) {
			simulationContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataView personDataView = simulationContext.getDataView(PersonDataView.class).get();
		if (!personDataView.personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID, personId);
		}
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}
		RegionDataView regionDataView = simulationContext.getDataView(RegionDataView.class).get();
		if (!regionDataView.regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private static void validateResourceId(SimulationContext simulationContext, ResourceId resourceId) {
		if (resourceId == null) {
			simulationContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourceDataView resourceDataView = simulationContext.getDataView(ResourceDataView.class).get();
		if (!resourceDataView.resourceIdExists(resourceId)) {
			simulationContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private static enum LabelerId implements EventLabelerId {
		COMPARTMENT_RESOURCE, REGION_RESOURCE, PERSON_RESOURCE, RESOURCE
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceChangeObservationEvent} events. Matches on
	 * compartment id and resource id.
	 * 
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public static EventLabel<PersonResourceChangeObservationEvent> getEventLabelByCompartmentAndResource(SimulationContext simulationContext, CompartmentId compartmentId, ResourceId resourceId) {
		validateCompartmentId(simulationContext, compartmentId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.COMPARTMENT_RESOURCE, PersonResourceChangeObservationEvent.class, compartmentId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link PersonResourceChangeObservationEvent}
	 * events that uses compartment id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonResourceChangeObservationEvent> getEventLabelerForCompartmentAndResource(CompartmentLocationDataView compartmentLocationDataView) {
		return new SimpleEventLabeler<>(LabelerId.COMPARTMENT_RESOURCE, PersonResourceChangeObservationEvent.class, (context, event) -> {
			CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(event.getPersonId());
			return new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.COMPARTMENT_RESOURCE, PersonResourceChangeObservationEvent.class, compartmentId, event.getResourceId());
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceChangeObservationEvent} events. Matches on region id
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
	public static EventLabel<PersonResourceChangeObservationEvent> getEventLabelByRegionAndResource(SimulationContext simulationContext, RegionId regionId, ResourceId resourceId) {
		validateRegionId(simulationContext, regionId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.REGION_RESOURCE, PersonResourceChangeObservationEvent.class, regionId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link PersonResourceChangeObservationEvent}
	 * events that uses region id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonResourceChangeObservationEvent> getEventLabelerForRegionAndResource(RegionLocationDataView regionLocationDataView) {
		return new SimpleEventLabeler<>(LabelerId.REGION_RESOURCE, PersonResourceChangeObservationEvent.class, (context, event) -> {
			RegionId regionId = regionLocationDataView.getPersonRegion(event.getPersonId());
			return new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.REGION_RESOURCE, PersonResourceChangeObservationEvent.class, regionId, event.getResourceId());
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceChangeObservationEvent} events. Matches on person id
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
	public static EventLabel<PersonResourceChangeObservationEvent> getEventLabelByPersonAndResource(SimulationContext simulationContext, PersonId personId, ResourceId resourceId) {
		validatePersonId(simulationContext, personId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.PERSON_RESOURCE, PersonResourceChangeObservationEvent.class, personId, resourceId);
	}

	/**
	 * Returns an event labeler for {@link PersonResourceChangeObservationEvent}
	 * events that uses person id and resource id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonResourceChangeObservationEvent> getEventLabelerForPersonAndResource() {
		return new SimpleEventLabeler<>(LabelerId.PERSON_RESOURCE, PersonResourceChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.PERSON_RESOURCE, PersonResourceChangeObservationEvent.class, event.getPersonId(), event.getResourceId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonResourceChangeObservationEvent} events. Matches on resource
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
	public static EventLabel<PersonResourceChangeObservationEvent> getEventLabelByResource(SimulationContext simulationContext, ResourceId resourceId) {
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.RESOURCE, PersonResourceChangeObservationEvent.class, resourceId);
	}
	/**
	 * Returns an event labeler for {@link PersonResourceChangeObservationEvent}
	 * events that uses resource id. Automatically added at
	 * initialization.
	 */

	public static EventLabeler<PersonResourceChangeObservationEvent> getEventLabelerForResource() {
		return new SimpleEventLabeler<>(LabelerId.RESOURCE, PersonResourceChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.RESOURCE, PersonResourceChangeObservationEvent.class, event.getResourceId()));
	}

	/**
	 * Returns the resource id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
