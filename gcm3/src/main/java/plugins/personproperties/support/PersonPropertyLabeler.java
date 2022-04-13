package plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Event;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;

/**
 * A labeler for person properties. The dimension of the labeler is the given
 * {@linkplain PersonPropertyId}, the event that stimulates a label update is
 * {@linkplain PersonPropertyUpdateEvent} and the labeling function
 * is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyLabeler implements Labeler {

	private final PersonPropertyId personPropertyId;
	private final Function<Object, Object> personPropertyValueLabelingFunction;
	private PersonPropertiesDataManager personPropertiesDataManager;

	public PersonPropertyLabeler(PersonPropertyId personPropertyId, Function<Object, Object> personPropertyValueLabelingFunction) {
		this.personPropertyId = personPropertyId;
		this.personPropertyValueLabelingFunction = personPropertyValueLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonId result = null;
		if (personPropertyUpdateEvent.getPersonPropertyId().equals(personPropertyId)) {
			result = personPropertyUpdateEvent.getPersonId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonPropertyUpdateEvent>(PersonPropertyUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = simulationContext.getDataManager(PersonPropertiesDataManager.class).get();
		}
		Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
		return personPropertyValueLabelingFunction.apply(personPropertyValue);
	}

	@Override
	public Object getDimension() {
		return personPropertyId;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonPropertyUpdateEvent personPropertyUpdateEvent =(PersonPropertyUpdateEvent)event;
		return personPropertyValueLabelingFunction.apply(personPropertyUpdateEvent.getPreviousPropertyValue());
	}

}
