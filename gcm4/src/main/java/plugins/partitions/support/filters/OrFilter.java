package plugins.partitions.support.filters;

import java.util.Set;

import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class OrFilter extends Filter {
	final Filter a;
	final Filter b;

	/**
	 * Constructs a filter that is the disjunction of two filters.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_FILTER} if either filter
	 *             is null</li>
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
	
	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OrFilter)) {
			return false;
		}
		OrFilter other = (OrFilter) obj;
		return a.equals(other.a) && b.equals(other.b) || a.equals(other.b) && b.equals(other.a);
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
