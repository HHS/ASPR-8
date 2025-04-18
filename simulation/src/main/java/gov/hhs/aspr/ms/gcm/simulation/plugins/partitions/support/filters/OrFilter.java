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
public final class OrFilter extends Filter {
	final Filter a;
	final Filter b;

	/**
	 * Constructs a filter that is the disjunction of two filters.
	 * 
	 * @throws ContractException {@linkplain PartitionError#NULL_FILTER} if either
	 *                           filter is null
	 */
	public OrFilter(Filter a, Filter b) {
		if (a == null) {
			throw new ContractException(PartitionError.NULL_FILTER);
		}
		if (b == null) {
			throw new ContractException(PartitionError.NULL_FILTER);
		}
		this.a = a;
		this.b = b;
	}

	public Filter getFirstFilter() {
		return a;
	}

	public Filter getSecondFilter() {
		return b;
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		return a.evaluate(partitionsContext, personId) || b.evaluate(partitionsContext, personId);
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = a.getFilterSensitivities();
		result.addAll(b.getFilterSensitivities());
		return result;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		a.validate(partitionsContext);
		b.validate(partitionsContext);
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(a, b);
	}

	/**
	 * Two {@link OrFilter} instances are equal if and only if
	 * their inputs are equal. The order in which inputs are added 
	 * does not matter.
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
		OrFilter other = (OrFilter) obj;
		return Objects.equals(a, other.a) && Objects.equals(b, other.b) ||
				Objects.equals(a, other.b) && Objects.equals(b, other.a);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OrFilter [a=");
		builder.append(a);
		builder.append(", b=");
		builder.append(b);
		builder.append("]");
		return builder.toString();
	}

}
