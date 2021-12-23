package plugins.people.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;

/**
 * 
 * Requests that the plugin expands data structure capacities to accommodate an
 * anticipated growth in the population.
 *
 *
 */
@Immutable
public final class PopulationGrowthProjectionEvent implements Event {

	private final int count;

	/**
	 * Sets the number of people who will need allocation in the near term
	 * 
	 * 
	 */
	public PopulationGrowthProjectionEvent(int count) {
		this.count = count;
	}

	/**
	 * Returns the size of the anticipated growth in the population
	 */
	public int getCount() {
		return count;
	}

}
