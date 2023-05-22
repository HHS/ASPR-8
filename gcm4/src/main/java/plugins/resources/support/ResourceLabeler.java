package plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Event;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import util.errors.ContractException;

/**
 * A a labeler for resources. The dimension of the labeler is the given
 * {@linkplain ResourceId}, the event that stimulates a label update is
 * {@linkplain PersonResourceUpdateEvent} and the labeling function
 * is composed from the given Function.
 * 
 *
 */
public final class ResourceLabeler implements Labeler {

	private final ResourceId resourceId;

	private final Function<Long, Object> resourceLabelingFunction;

	private ResourcesDataManager resourcesDataManager;

	public ResourceLabeler(ResourceId resourceId, Function<Long, Object> resourceLabelingFunction) {
		this.resourceId = resourceId;
		this.resourceLabelingFunction = resourceLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonResourceUpdateEvent personResourceUpdateEvent) {
		PersonId result = null;
		if (personResourceUpdateEvent.resourceId().equals(resourceId)) {
			result = personResourceUpdateEvent.personId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonResourceUpdateEvent>(PersonResourceUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public Object getCurrentLabel(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourcesDataManager == null) {
			resourcesDataManager = simulationContext.getDataManager(ResourcesDataManager.class);
		}
		long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
		return resourceLabelingFunction.apply(personResourceLevel);
	}

	@Override
	public Object getId() {
		return resourceId;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonResourceUpdateEvent personResourceUpdateEvent = (PersonResourceUpdateEvent)event;
		return resourceLabelingFunction.apply(personResourceUpdateEvent.previousResourceLevel());
	}

}
