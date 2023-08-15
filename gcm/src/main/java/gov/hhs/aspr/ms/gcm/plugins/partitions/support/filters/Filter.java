package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.errors.ContractException;

public abstract class Filter {

	protected Filter() {
	}

	/**
	 * Returns a filter that is the conjunction of this and the given filter.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PartitionError#NULL_FILTER} if the filter is
	 *             null</li>
	 */
	public final Filter and(Filter filter) {
		return new AndFilter(this, filter);
	}

	/**
	 * Returns a filter that is the disjunction of this and the given filter.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PartitionError#NULL_FILTER} if the filter is
	 *             null</li>
	 */
	public final Filter or(Filter filter) {
		return new OrFilter(this, filter);
	}

	/**
	 * Returns a filter that is the negation of this filter.
	 */
	public final Filter not() {
		return new NotFilter(this);
	}

	/**
	 * Evaluates the person against the filter.
	 *
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_SIMULATION_CONTEXT} if the
	 *             context is null</li>
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 * 
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public abstract boolean evaluate(PartitionsContext partitionsContext, PersonId personId);

	/**
	 * Validates the filter from the given context.
	 * 
	 * Preconditions:
	 * 
	 * <li>the context is not null</li>
	 */
	public abstract void validate(PartitionsContext partitionsContext);

	/**
	 * Returns the filter sensitivities
	 */
	public abstract Set<FilterSensitivity<?>> getFilterSensitivities();

	/**
	 * Filters are equal if they represent the same logical operation
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Filters are equal if they represent the same logical operation
	 */
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract String toString();

}
