package plugins.partitions.support.filters;

import java.util.Set;

import nucleus.SimulationContext;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;

public final class AndFilter extends Filter {
	final Filter a;
	final Filter b;

	public AndFilter(Filter a, Filter b) {
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

}