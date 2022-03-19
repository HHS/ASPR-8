package plugins.partitions.support;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

public abstract class Filter {

	private static class AndFilter extends Filter {
		final Filter a;
		final Filter b;

		public AndFilter(Filter a, Filter b) {
			this.a = a;
			this.b = b;
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

	private static class OrFilter extends Filter {
		final Filter a;
		final Filter b;

		public OrFilter(Filter a, Filter b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
			return a.evaluate(simulationContext, personId) || b.evaluate(simulationContext, personId);
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

	private static class NotFilter extends Filter {
		final Filter a;

		public NotFilter(Filter a) {
			this.a = a;
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

	/**
	 * Returns a filter that is the conjunction of this and the given filter.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_FILTER} if the filter is
	 *             null</li>
	 */
	public final Filter and(Filter filter) {
		if (filter == null) {
			throw new ContractException(PartitionError.NULL_FILTER);
		}
		return new AndFilter(this, filter);
	}

	/**
	 * Returns a filter that is the disjunction of this and the given filter.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PartitionError#NULL_FILTER} if the filter is
	 *             null</li>
	 */
	public final Filter or(Filter filter) {
		if (filter == null) {
			throw new ContractException(PartitionError.NULL_FILTER);
		}
		return new OrFilter(this, filter);
	}

	/**
	 * Returns a filter that is the negation of this filter.
	 */
	public final Filter negate() {
		return new NotFilter(this);
	}

	private final static class NoPeopleFilter extends Filter {

		@Override
		public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
			return false;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("NoPeopleFilter []");
			return builder.toString();
		}

		@Override
		public Set<FilterSensitivity<?>> getFilterSensitivities() {
			return new LinkedHashSet<>();
		}

		@Override
		public void validate(SimulationContext simulationContext) {

		}

	}

	private final static class AllPeopleFilter extends Filter {

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
	}

	/**
	 * Evaluates the person against the filter.
	 * 
	 * Preconditions :
	 * 
	 * @throws ContractException 
	 * <li>{@linkplain NucleusError#NULL_SIMULATION_CONTEXT} if the context is not null</li>
	 * 
	 * 
	 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is not null</li>
	 * 
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is known</li>
	 */
	public abstract boolean evaluate(SimulationContext simulationContext, PersonId personId);

	/**
	 * Validates the filter from the given context.
	 * 
	 * Preconditions:
	 *  
	 * <li>the context is not null</li>
	 */
	public abstract void validate(SimulationContext simulationContext);

	/**
	 * Returns the filter sensitivities
	 */
	public abstract Set<FilterSensitivity<?>> getFilterSensitivities();

	/**
	 * Returns a filter that passes no people. Used for concatenating filters in
	 * an OR loop.
	 */
	public static Filter noPeople() {
		return new NoPeopleFilter();
	}

	/**
	 * Returns a filter that passes all people. Used for concatenating filters
	 * in an AND loop.
	 */
	public static Filter allPeople() {
		return new AllPeopleFilter();

	}

}
