package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * A filter that passes all people. Used for concatenating filters into an
 * AndFilter over a loop of filters that might be empty. AND operating over an
 * empty set should always be true.
 */

@Immutable
public final class TrueFilter extends Filter {

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrueFilter []");
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
		return 1;
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