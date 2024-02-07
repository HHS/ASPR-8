package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * Partitions are maintained as events relating to people are resolved. To do so
 * efficiently, we need to determine whether a particular event will trigger a
 * change to either a person's membership in the partition or their position
 * within the partition space. Partition filters describe their sensitivity to
 * events via this generics-base class.
 */
@Immutable
public final class FilterSensitivity<T extends Event> {

	private class MetaPredicate<K extends Event> {

		private final EventPredicate<K> eventPredicate;

		public MetaPredicate(EventPredicate<K> eventPredicate) {
			this.eventPredicate = eventPredicate;
		}

		@SuppressWarnings("unchecked")
		public Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, Event event) {
			return eventPredicate.requiresRefresh(partitionsContext, (K) event);
		}
	}

	private final Class<T> eventClass;

	private final MetaPredicate<T> metaPredicate;

	/**
	 * Creates this FilterSensitivity with the generic event type and given event
	 * predicate.
	 */
	public FilterSensitivity(Class<T> eventClass, EventPredicate<T> eventPredicate) {
		super();
		this.eventClass = eventClass;
		this.metaPredicate = new MetaPredicate<T>(eventPredicate);
	}

	/**
	 * Returns the event class for this filter sensitivity
	 */
	public Class<T> getEventClass() {
		return eventClass;
	}

	/**
	 * Returns a PersonId if and only if the event would require a refresh from the
	 * owning filter. The partition resolver is not able to determine from an event
	 * the person id of an event that possibly changes the membership of a person in
	 * a partition. The person id returned is the person that will trigger the
	 * refresh and is thus the person id associated with the event.
	 */
	public Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, Event event) {
		return metaPredicate.requiresRefresh(partitionsContext, event);
	}

}