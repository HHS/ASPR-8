package plugins.partitions.support.filters;

import java.util.Set;

import nucleus.SimulationContext;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class AndFilter extends Filter {
	final Filter a;
	final Filter b;

	/**
	 * Constructs a filter that is the conjunction of two filters.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_FILTER} if either filter
	 *             is null</li>
	 */
	public AndFilter(Filter a, Filter b) {
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
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		return a.evaluate(simulationContext, personId) && b.evaluate(simulationContext, personId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AndFilter [a=");
		builder.append(a);
		builder.append(", b=");
		builder.append(b);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = a.getFilterSensitivities();
		result.addAll(b.getFilterSensitivities());
		return result;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		a.validate(simulationContext);
		b.validate(simulationContext);
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
		if (!(obj instanceof AndFilter)) {
			return false;
		}
		AndFilter other = (AndFilter) obj;
		return a.equals(other.a) && b.equals(other.b) || a.equals(other.b) && b.equals(other.a);
	}

}