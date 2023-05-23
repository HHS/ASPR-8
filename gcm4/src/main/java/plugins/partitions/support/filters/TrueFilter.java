package plugins.partitions.support.filters;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;

/**
 * A filter that passes all people. Used for concatenating filters into an
 * AndFilter over a loop of filters that might be empty. AND operating over an
 * empty set should always be true.
 */
public final class TrueFilter extends Filter {

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AllPeopleFilter []");
		return builder.toString();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		return new LinkedHashSet<>();
	}

	@Override
	public void validate(SimulationContext simulationContext) {

	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TrueFilter)) {
			return false;
		}
		return true;
	}
	
	
}