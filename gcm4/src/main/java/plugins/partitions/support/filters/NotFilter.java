package plugins.partitions.support.filters;

import java.util.Set;

import nucleus.SimulationContext;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;

public final class NotFilter extends Filter {
	final Filter a;

	public NotFilter(Filter a) {
		this.a = a;
	}
	
	public Filter getSubFilter() {
		return a;
	}
		

	@Override
	public void validate(SimulationContext simulationContext) {
		a.validate(simulationContext);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		return !a.evaluate(simulationContext, personId);
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

}
