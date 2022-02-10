package plugins.personproperties.events.observation;

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
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.ContractException;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 *
 * @author Shawn Hatch
 *
 */

@Immutable
public class PersonPropertyChangeObservationEvent implements Event {
	
		
	
	private final PersonId personId;
	private final PersonPropertyId personPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Creates this event from valid, non-null inputs
	 * 
	 */
	public PersonPropertyChangeObservationEvent(final PersonId personId, final PersonPropertyId personPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
		super();
		this.personId = personId;
		this.personPropertyId = personPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the current property value used to construct this event
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Returns the person property id used to construct this event
	 */
	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	/**
	 * Returns the person id used to construct this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the previous property value used to construct this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns this event in the form:
	 * 
	 * "PersonPropertyChangeObservationEvent [personId=" + personId + ",
	 * personPropertyId=" + personPropertyId + ", previousPropertyValue=" +
	 * previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue
	 * + "]";
	 */
	@Override
	public String toString() {
		return "PersonPropertyChangeObservationEvent [personId=" + personId + ", personPropertyId=" + personPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		COMPARTMENT_PROPERTY, PERSON_PROPERTY, PROPERTY, REGION_PROPERTY
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyChangeObservationEvent} events. Matches on
	 * compartment id and person property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyChangeObservationEvent> getEventLabelByCompartmentAndProperty(SimulationContext simulationContext, CompartmentId compartmentId, PersonPropertyId personPropertyId) {
		validateCompartmentId(simulationContext, compartmentId);
		validatePersonPropertyId(simulationContext, personPropertyId);

		return new MultiKeyEventLabel<>(personPropertyId, LabelerId.COMPARTMENT_PROPERTY, PersonPropertyChangeObservationEvent.class, compartmentId, personPropertyId);
	}

	/**
	 * Returns an event labeler for {@link PersonPropertyChangeObservationEvent}
	 * events that uses compartment id and person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyChangeObservationEvent> getEventLabelerForCompartmentAndProperty(CompartmentLocationDataView compartmentLocationDataView) {
		return new SimpleEventLabeler<>(LabelerId.COMPARTMENT_PROPERTY, PersonPropertyChangeObservationEvent.class,
				(context, event) -> {					
					CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(event.getPersonId());
					return new MultiKeyEventLabel<>(event.getPersonPropertyId(), LabelerId.COMPARTMENT_PROPERTY, PersonPropertyChangeObservationEvent.class, compartmentId,
							event.getPersonPropertyId());
				});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyChangeObservationEvent} events. Matches on person id
	 * and person property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyChangeObservationEvent> getEventLabelByPersonAndProperty(SimulationContext simulationContext, PersonId personId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(personPropertyId, LabelerId.PERSON_PROPERTY, PersonPropertyChangeObservationEvent.class, personId, personPropertyId);
	}

	/**
	 * Returns an event labeler for {@link PersonPropertyChangeObservationEvent}
	 * events that uses person id and person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyChangeObservationEvent> getEventLabelerForPersonAndProperty() {
		return new SimpleEventLabeler<>(LabelerId.PERSON_PROPERTY, PersonPropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getPersonPropertyId(),
				LabelerId.PERSON_PROPERTY, PersonPropertyChangeObservationEvent.class, event.getPersonId(), event.getPersonPropertyId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyChangeObservationEvent} events. Matches on person
	 * property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyChangeObservationEvent> getEventLabelByProperty(SimulationContext simulationContext, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		return new MultiKeyEventLabel<>(personPropertyId, LabelerId.PROPERTY, PersonPropertyChangeObservationEvent.class, personPropertyId);
	}

	/**
	 * Returns an event labeler for {@link PersonPropertyChangeObservationEvent}
	 * events that uses only the person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyChangeObservationEvent> getEventLabelerForProperty() {
		return new SimpleEventLabeler<>(LabelerId.PROPERTY, PersonPropertyChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getPersonPropertyId(), LabelerId.PROPERTY, PersonPropertyChangeObservationEvent.class, event.getPersonPropertyId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyChangeObservationEvent} events. Matches on region id
	 * and person property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyChangeObservationEvent> getEventLabelByRegionAndProperty(SimulationContext simulationContext, RegionId regionId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		validateRegionId(simulationContext, regionId);
		return new MultiKeyEventLabel<>(personPropertyId, LabelerId.REGION_PROPERTY, PersonPropertyChangeObservationEvent.class, regionId, personPropertyId);
	}

	/**
	 * Returns an event labeler for {@link PersonPropertyChangeObservationEvent}
	 * events that uses the region id and person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyChangeObservationEvent> getEventLabelerForRegionAndProperty(RegionLocationDataView regionLocationDataView) {
		return new SimpleEventLabeler<>(LabelerId.REGION_PROPERTY, PersonPropertyChangeObservationEvent.class, (context, event) -> {
			RegionId regionId = regionLocationDataView.getPersonRegion(event.getPersonId());
			return new MultiKeyEventLabel<>(event.getPersonPropertyId(), LabelerId.REGION_PROPERTY, PersonPropertyChangeObservationEvent.class, regionId, event.getPersonPropertyId());
		});
	}

	private static void validatePersonPropertyId(SimulationContext simulationContext, PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			simulationContext.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}

		if (!simulationContext.getDataView(PersonPropertyDataView.class).get().personPropertyIdExists(personPropertyId)) {
			simulationContext.throwContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID);
		}
	}

	private static void validateCompartmentId(SimulationContext simulationContext, CompartmentId compartmentId) {
		if (compartmentId == null) {
			simulationContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!simulationContext.getDataView(CompartmentDataView.class).get().compartmentIdExists(compartmentId)) {
			simulationContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID);
		}
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!simulationContext.getDataView(RegionDataView.class).get().regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}

		if (!simulationContext.getDataView(PersonDataView.class).get().personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/**
	 * Returns the person property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return personPropertyId;
	}

}
