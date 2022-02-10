package plugins.compartments.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.SimulationContext;
import nucleus.Event;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * A labeler for compartments. The dimension of the labeler is the
 * {@linkplain CompartmentId} class, the event that stimulates a label update is
 * {@linkplain PersonCompartmentChangeObservationEvent} and the labeling
 * function is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class CompartmentLabeler implements Labeler {

	private final Function<CompartmentId, Object> compartmentLabelingFunction;
	private CompartmentLocationDataView compartmentLocationDataView;

	/**
	 * Creates the Compartment labeler from the given labeling function
	 */
	public CompartmentLabeler(Function<CompartmentId, Object> compartmentLabelingFunction) {
		this.compartmentLabelingFunction = compartmentLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		return Optional.of(personCompartmentChangeObservationEvent.getPersonId());
	}

	/**
	 * Returns a single labeler sensitivity for
	 * PersonCompartmentChangeObservationEvent. All compartment changes will
	 * effect the partition.
	 */
	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonCompartmentChangeObservationEvent>(PersonCompartmentChangeObservationEvent.class, this::getPersonId));
		return result;
	}

	/**
	 * Returns the label for the given person id
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
	 */
	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if (compartmentLocationDataView == null) {
			compartmentLocationDataView = simulationContext.getDataView(CompartmentLocationDataView.class).get();
		}
		CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		return compartmentLabelingFunction.apply(compartmentId);
	}

	/**
	 * Returns {@link CompartmentId} class as the dimension.
	 */
	@Override
	public Object getDimension() {
		return CompartmentId.class;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent = (PersonCompartmentChangeObservationEvent)event;
		return compartmentLabelingFunction.apply(personCompartmentChangeObservationEvent.getPreviousCompartmentId());
	}

}
