package plugins.partitions.support;

import java.util.Optional;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.people.support.PersonId;

/**
 * A generics based function that returns an optional {@link PersonId} from a
 * {@link SimulationContext} and {@link Event}. Used by {@link FilterSensitivity} to
 * ascertain the person id from an event and whether that event would effect the
 * filter that owns the filter sensitivity.
 * 
 * 
 * @author Shawn Hatch
 *
 * 
 */
public interface EventPredicate<T extends Event> {
	/**
	 * Returns an optional of the person id associated with the event if that
	 * event would effect that filter.  Returns an empty optional otherwise.
	 */
	public Optional<PersonId> requiresRefresh(SimulationContext simulationContext, T event);
}