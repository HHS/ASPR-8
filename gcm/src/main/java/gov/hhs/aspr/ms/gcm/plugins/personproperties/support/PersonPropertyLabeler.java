package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Labeler;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.events.PersonPropertyUpdateEvent;
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
	public final Object getCurrentLabel(PartitionsContext partitionsContext, PersonId personId) {
		if(partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = partitionsContext.getDataManager(PersonPropertiesDataManager.class);
		}
		Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
		return getLabelFromValue(personPropertyValue);
	}

	@Override
	public final Object getId() {
		return personPropertyId;
	}

	@Override
	public final Object getPastLabel(PartitionsContext partitionsContext, Event event) {
		PersonPropertyUpdateEvent personPropertyUpdateEvent =(PersonPropertyUpdateEvent)event;
		return getLabelFromValue(personPropertyUpdateEvent.previousPropertyValue());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonPropertyLabeler [personPropertyId=");
		builder.append(personPropertyId);
		builder.append("]");
		return builder.toString();
	}
	
	

}
