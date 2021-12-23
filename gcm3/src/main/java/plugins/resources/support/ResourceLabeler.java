package plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Context;
import nucleus.Event;
import nucleus.NucleusError;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import util.ContractException;

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

	private ResourceDataView resourceDataView;

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
	public Object getLabel(Context context, PersonId personId) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (resourceDataView == null) {
			resourceDataView = context.getDataView(ResourceDataView.class).get();
		}
		long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
		return resourceLabelingFunction.apply(personResourceLevel);
	}

	@Override
	public Object getDimension() {
		return resourceId;
	}

	@Override
	public Object getPastLabel(Context context, Event event) {
		PersonResourceChangeObservationEvent personResourceChangeObservationEvent = (PersonResourceChangeObservationEvent)event;
		return resourceLabelingFunction.apply(personResourceChangeObservationEvent.getPreviousResourceLevel());
	}

}
