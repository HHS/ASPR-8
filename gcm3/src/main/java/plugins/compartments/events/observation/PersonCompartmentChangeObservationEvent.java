package plugins.compartments.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * An observation event indicating that a person's compartment assignment has
 * changed.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
public class PersonCompartmentChangeObservationEvent implements Event {

	private static enum LabelerId implements EventLabelerId {
		ARRIVAL, DEPARTURE, PERSON
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonCompartmentChangeObservationEvent} events. Matches on
	 * arriving compartment id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 */
	public static EventLabel<PersonCompartmentChangeObservationEvent> getEventLabelByArrivalCompartment(final SimulationContext simulationContext, final CompartmentId compartmentId) {
		validateCompartmentId(simulationContext, compartmentId);
		return new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class, LabelerId.ARRIVAL, PersonCompartmentChangeObservationEvent.class, compartmentId);
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonCompartmentChangeObservationEvent} events. Matches on
	 * departing compartment id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 */
	public static EventLabel<PersonCompartmentChangeObservationEvent> getEventLabelByDepartureCompartment(final SimulationContext simulationContext, final CompartmentId compartmentId) {
		validateCompartmentId(simulationContext, compartmentId);
		return new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class, LabelerId.DEPARTURE, PersonCompartmentChangeObservationEvent.class, compartmentId);
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonCompartmentChangeObservationEvent} events. Matches on person
	 * id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 */
	public static EventLabel<PersonCompartmentChangeObservationEvent> getEventLabelByPerson(final SimulationContext simulationContext, final PersonId personId) {
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class, LabelerId.PERSON, PersonCompartmentChangeObservationEvent.class, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link CompartmentPropertyChangeObservationEvent} events that uses only
	 * the arriving compartment. Automatically added at initialization.
	 */
	public static EventLabeler<PersonCompartmentChangeObservationEvent> getEventLabelerForArrivalCompartment() {
		return new SimpleEventLabeler<>(LabelerId.ARRIVAL, PersonCompartmentChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class,
				LabelerId.ARRIVAL, PersonCompartmentChangeObservationEvent.class, event.getCurrentCompartmentId()));
	}

	/**
	 * Returns an event labeler for
	 * {@link CompartmentPropertyChangeObservationEvent} events that uses only
	 * the departing compartment. Automatically added at initialization.
	 */
	public static EventLabeler<PersonCompartmentChangeObservationEvent> getEventLabelerForDepartureCompartment() {
		return new SimpleEventLabeler<>(LabelerId.DEPARTURE, PersonCompartmentChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class,
				LabelerId.DEPARTURE, PersonCompartmentChangeObservationEvent.class, event.getPreviousCompartmentId()));
	}

	/**
	 * Returns an event labeler for
	 * {@link CompartmentPropertyChangeObservationEvent} events that uses only
	 * the person id. Automatically added at initialization.
	 */
	public static EventLabeler<PersonCompartmentChangeObservationEvent> getEventLabelerForPerson() {
		return new SimpleEventLabeler<>(LabelerId.PERSON, PersonCompartmentChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(PersonCompartmentChangeObservationEvent.class, LabelerId.PERSON, PersonCompartmentChangeObservationEvent.class, event.getPersonId()));
	}

	private static void validateCompartmentId(final SimulationContext simulationContext, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			simulationContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}
		final CompartmentDataView compartmentDataView = simulationContext.getDataView(CompartmentDataView.class).get();
		if (!compartmentDataView.compartmentIdExists(compartmentId)) {
			simulationContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID);
		}

	}

	private static void validatePersonId(final SimulationContext simulationContext, final PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		final PersonDataView personDataView = simulationContext.getDataView(PersonDataView.class).get();
		if (!personDataView.personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private final PersonId personId;

	private final CompartmentId previousCompartmentId;

	private final CompartmentId currentCompartmentId;

	/**
	 * Creates this event from valid, non-null values.
	 *
	 */
	public PersonCompartmentChangeObservationEvent(final PersonId personId, final CompartmentId previousCompartmentId, final CompartmentId currentCompartmentId) {
		super();
		this.personId = personId;
		this.previousCompartmentId = previousCompartmentId;
		this.currentCompartmentId = currentCompartmentId;
	}

	/**
	 * Returns the {@link CompartmentId} value for the person immediately after
	 * the change
	 */
	public CompartmentId getCurrentCompartmentId() {
		return currentCompartmentId;
	}

	/**
	 * Returns the {@link PersonId} for this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the {@link CompartmentId} value for the person immediately before
	 * the change
	 */
	public CompartmentId getPreviousCompartmentId() {
		return previousCompartmentId;
	}

	/**
	 * Standard string implementation
	 */
	@Override
	public String toString() {
		return "PersonCompartmentChangeObservationEvent [personId=" + personId + ", previousCompartmentId=" + previousCompartmentId + ", currentCompartmentId=" + currentCompartmentId + "]";
	}

}