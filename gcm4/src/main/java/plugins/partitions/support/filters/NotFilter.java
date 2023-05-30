package plugins.partitions.support.filters;

import java.util.Set;

import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class NotFilter extends Filter {
	final Filter a;

	/**
	 * Constructs a filter that negates another filter.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_FILTER} if the filter is
	 *             null</li>
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + a.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NotFilter)) {
			return false;
		}
		NotFilter other = (NotFilter) obj;
		if (!a.equals(other.a)) {
			return false;
		}
		return true;
	}

}
