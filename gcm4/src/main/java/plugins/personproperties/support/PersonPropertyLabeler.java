package plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Event;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import util.errors.ContractException;

/**
 * A labeler for person properties. The dimension of the labeler is the given
 * {@linkplain PersonPropertyId}, the event that stimulates a label update is
 * {@linkplain PersonPropertyUpdateEvent} and the labeling function
 * is composed from the given Function.
 * 
 *
 */
public abstract class PersonPropertyLabeler implements Labeler {

	private final PersonPropertyId personPropertyId;	
	private PersonPropertiesDataManager personPropertiesDataManager;
	
	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}
	 
	public PersonPropertyLabeler(PersonPropertyId personPropertyId) {
		this.personPropertyId = personPropertyId;		
	}
	
	protected abstract Object getLabelFromValue(Object value);

	private Optional<PersonId> getPersonId(PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonId result = null;
		if (personPropertyUpdateEvent.personPropertyId().equals(personPropertyId)) {
			result = personPropertyUpdateEvent.personId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public final Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonPropertyUpdateEvent>(PersonPropertyUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public final Object getCurrentLabel(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = simulationContext.getDataManager(PersonPropertiesDataManager.class);
		}
		Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
		return getLabelFromValue(personPropertyValue);
	}

	@Override
	public final Object getId() {
		return personPropertyId;
	}

	@Override
	public final Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonPropertyUpdateEvent personPropertyUpdateEvent =(PersonPropertyUpdateEvent)event;
		return getLabelFromValue(personPropertyUpdateEvent.previousPropertyValue());
	}

}
