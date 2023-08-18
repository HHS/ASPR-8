package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * A filter that passes no people. Used for concatenating filters into an
 * OrFilter over a loop of filters that might be empty. OR operating over an
 * empty set should always be false.
 */

@Immutable
public final class FalseFilter extends Filter {

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FalseFilter []");
		return builder.toString();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		return new LinkedHashSet<>();
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {

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
		if (!(obj instanceof FalseFilter)) {
			return false;
		}
		return true;
	}

}