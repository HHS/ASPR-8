package plugins.resources.support;

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
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.events.PersonResourceChangeObservationEvent;

/**
 * A a labeler for resources. The dimension of the labeler is the given
 * {@linkplain ResourceId}, the event that stimulates a label update is
 * {@linkplain PersonResourceChangeObservationEvent} and the labeling function
 * is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class ResourceLabeler implements Labeler {

	private final ResourceId resourceId;

	private final Function<Long, Object> resourceLabelingFunction;

	private ResourceDataManager resourceDataManager;

	public ResourceLabeler(ResourceId resourceId, Function<Long, Object> resourceLabelingFunction) {
		this.resourceId = resourceId;
		this.resourceLabelingFunction = resourceLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonResourceChangeObservationEvent personResourceChangeObservationEvent) {
		PersonId result = null;
		if (personResourceChangeObservationEvent.getResourceId().equals(resourceId)) {
			result = personResourceChangeObservationEvent.getPersonId();
		}
		return Optional.ofNullable(result);
	}

	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonResourceChangeObservationEvent>(PersonResourceChangeObservationEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourceDataManager == null) {
			resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class).get();
		}
		long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		return resourceLabelingFunction.apply(personResourceLevel);
	}

	@Override
	public Object getDimension() {
		return resourceId;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonResourceChangeObservationEvent personResourceChangeObservationEvent = (PersonResourceChangeObservationEvent)event;
		return resourceLabelingFunction.apply(personResourceChangeObservationEvent.getPreviousResourceLevel());
	}

}
