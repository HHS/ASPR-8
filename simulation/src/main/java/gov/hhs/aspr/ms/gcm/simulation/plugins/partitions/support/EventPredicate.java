package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;

/**
 * A generics based function that returns an optional {@link PersonId} from a
 * {@link PartitionsContext} and {@link Event}. Used by
 * {@link FilterSensitivity} to ascertain the person id from an event and
 * whether that event would effect the filter that owns the filter sensitivity.
 */
public interface EventPredicate<T extends Event> {
	/**
	 * Returns an optional of the person id associated with the event if that event
	 * would effect that filter. Returns an empty optional otherwise.
	 */
	public Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, T event);
}