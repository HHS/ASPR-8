package plugins.stochastics.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;

/**
 * 
 * An event for re-seeding the stochastics plugin.
 * 
 */
@Immutable
public final class StochasticsReseedEvent implements Event {

	private final long seed;

	/**
	 * Constructs this event.
	 *
	 */
	public StochasticsReseedEvent(long seed) {
		this.seed = seed;
	}

	/**
	 * Returns the seed value
	 */
	public long getSeed() {
		return seed;
	}

}
