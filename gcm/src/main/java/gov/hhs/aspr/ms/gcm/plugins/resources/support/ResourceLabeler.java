package gov.hhs.aspr.ms.gcm.plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Labeler;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.PersonResourceUpdateEvent;
import util.errors.ContractException;

/**
 * A a labeler for resources. The dimension of the labeler is the given
 * {@linkplain ResourceId}, the event that stimulates a label update is
 * {@linkplain PersonResourceUpdateEvent} and the labeling function is composed
 * from the given Function.
 */
public abstract class ResourceLabeler implements Labeler {

	private final ResourceId resourceId;

	protected abstract Object getLabelFromAmount(long amount);

	private ResourcesDataManager resourcesDataManager;

	public ResourceId getResourceId() {
		return resourceId;
	}

	public ResourceLabeler(ResourceId resourceId) {
		this.resourceId = resourceId;
	}

	private Optional<PersonId> getPersonId(PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId result = null;
		if (personResourceUpdateEvent.resourceId().equals(resourceId)) {
			result = personResourceUpdateEvent.personId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public final Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(
				new LabelerSensitivity<PersonResourceUpdateEvent>(PersonResourceUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public final Object getCurrentLabel(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourcesDataManager == null) {
			resourcesDataManager = partitionsContext.getDataManager(ResourcesDataManager.class);
		}
		long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
		return getLabelFromAmount(personResourceLevel);
	}

	@Override
	public final Object getId() {
		return resourceId;
	}

	@Override
	public final Object getPastLabel(PartitionsContext partitionsContext, Event event) {
		PersonResourceUpdateEvent personResourceUpdateEvent = (PersonResourceUpdateEvent) event;
		return getLabelFromAmount(personResourceUpdateEvent.previousResourceLevel());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceLabeler [resourceId=");
		builder.append(resourceId);
		builder.append("]");
		return builder.toString();
	}

}
