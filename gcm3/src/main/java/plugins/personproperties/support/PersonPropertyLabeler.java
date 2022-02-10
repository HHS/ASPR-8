package plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import util.ContractException;

/**
 * A labeler for person properties. The dimension of the labeler is the given
 * {@linkplain PersonPropertyId}, the event that stimulates a label update is
 * {@linkplain PersonPropertyChangeObservationEvent} and the labeling function
 * is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyLabeler implements Labeler {

	private final PersonPropertyId personPropertyId;
	private final Function<Object, Object> personPropertyValueLabelingFunction;
	private PersonPropertyDataView personPropertyDataView;

	public PersonPropertyLabeler(PersonPropertyId personPropertyId, Function<Object, Object> personPropertyValueLabelingFunction) {
		this.personPropertyId = personPropertyId;
		this.personPropertyValueLabelingFunction = personPropertyValueLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonId result = null;
		if (personPropertyChangeObservationEvent.getPersonPropertyId().equals(personPropertyId)) {
			result = personPropertyChangeObservationEvent.getPersonId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonPropertyChangeObservationEvent>(PersonPropertyChangeObservationEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personPropertyDataView == null) {
			personPropertyDataView = simulationContext.getDataView(PersonPropertyDataView.class).get();
		}
		Object personPropertyValue = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
		return personPropertyValueLabelingFunction.apply(personPropertyValue);
	}

	@Override
	public Object getDimension() {
		return personPropertyId;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent =(PersonPropertyChangeObservationEvent)event;
		return personPropertyValueLabelingFunction.apply(personPropertyChangeObservationEvent.getPreviousPropertyValue());
	}

}
