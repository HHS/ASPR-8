package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import java.util.Optional;
import java.util.function.Function;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * Partitions are maintained as events relating to people are resolved. To do so
 * efficiently, we need to determine whether a particular event will trigger a
 * change to either a person's membership in the partition or their position
 * within the partition space. Partition labelers describe their sensitivity to
 * events via this generics-base class.
 */
@Immutable
public final class LabelerSensitivity<T extends Event> {
	private final Class<T> eventClass;
	private final Function<T, Optional<PersonId>> personFunction;

	/**
	 * Creates the labeler sensitivity from the event type and person function. The
	 * person function should return and empty optional if an event will not effect
	 * a person's label and an optional of the person id otherwise.
	 */
	public LabelerSensitivity(Class<T> eventClass, Function<T, Optional<PersonId>> personFunction) {
		super();
		this.eventClass = eventClass;
		this.personFunction = personFunction;
	}

	/**
	 * Returns the event class of this labeler sensitivity. This will be same type
	 * and the generic type of the class.
	 */
	public Class<T> getEventClass() {
		return eventClass;
	}

	/**
	 * Returns the person id from the event if the event will effect that person's
	 * label value and an empty optional otherwise. The partition resolver is not
	 * able to determine from an event the person id of an event that possibly
	 * changes the label cell of a person in a partition. The person id returned is
	 * the person that will trigger the refresh and is thus the person id associated
	 * with the event.
	 */
	@SuppressWarnings("unchecked")
	public Optional<PersonId> getPersonId(Event event) {
		return personFunction.apply((T) event);
	}

}