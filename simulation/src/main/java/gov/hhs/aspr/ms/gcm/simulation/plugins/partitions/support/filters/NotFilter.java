package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters;

import java.util.Objects;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

@Immutable
public final class NotFilter extends Filter {
	final Filter a;

	/**
	 * Constructs a filter that negates another filter.
	 * 
	 * @throws ContractException {@linkplain PartitionError#NULL_FILTER} if the
	 *                           filter is null
	 */
	public NotFilter(Filter a) {
		if (a == null) {
			throw new ContractException(PartitionError.NULL_FILTER);
		}
		this.a = a;
	}

	public Filter getSubFilter() {
		return a;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		a.validate(partitionsContext);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		return !a.evaluate(partitionsContext, personId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotFilter [a=");
		builder.append(a);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		return a.getFilterSensitivities();
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(a);
	}

	/**
	 * Two {@link NotFilter} instances are equal if and only if
	 * their inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NotFilter other = (NotFilter) obj;
		return Objects.equals(a, other.a);
	}

}
