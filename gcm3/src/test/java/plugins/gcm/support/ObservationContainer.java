package plugins.gcm.support;

import java.util.LinkedHashSet;
import java.util.Set;

import util.MultiKey;

/**
 * A mutable container for MultiKey based recording of observations that are
 * made by components as they observe events in the test simulation.
 * 
 * @author Shawn Hatch
 *
 */
public final class ObservationContainer {
	private Set<MultiKey> observations = new LinkedHashSet<>();

	public Set<MultiKey> getObservations() {
		return new LinkedHashSet<>(observations);
	}

	public void addObservation(MultiKey multiKey) {
		observations.add(multiKey);
	}
}
