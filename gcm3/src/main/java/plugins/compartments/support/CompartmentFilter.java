package plugins.compartments.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * A filter implementation that selects for people who belong to a particular
 * {@link CompartmentId}
 * 
 * @author Shawn Hatch
 *
 */

public final class CompartmentFilter extends Filter {
	private final CompartmentId compartmentId;
	private CompartmentLocationDataView compartmentLocationDataView;

	private void validateCompartmentId(final Context context, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			context.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}
		
		if (!context.getDataView(CompartmentDataView.class).get().compartmentIdExists(compartmentId)) {
			context.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}

	}

	/**
	 * Creates a compartment filter. Not null tolerant.
	 * 
	 */
	public CompartmentFilter(CompartmentId compartmentId) {
		this.compartmentId = compartmentId;
	}

	/**
	 * Validates this compartment filter
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}</li> if
	 *             the compartment used to create the filter is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>if
	 *             the compartment used to create the filter is unknown
	 */
	@Override
	public void validate(Context context) {
		validateCompartmentId(context, compartmentId);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		if (compartmentLocationDataView == null) {
			compartmentLocationDataView = context.getDataView(CompartmentLocationDataView.class).get();
		}

		return compartmentLocationDataView.getPersonCompartment(personId).equals(compartmentId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompartmentFilter [compartmentId=");
		builder.append(compartmentId);
		builder.append("]");
		return builder.toString();
	}

	private Optional<PersonId> requiresRefresh(Context context, PersonCompartmentChangeObservationEvent event) {
		/*
		 * We know that the two compartment ids from the event are not equal. If
		 * either of them are equal to this filter's compartment id then this
		 * filter will change its evaluation about the person and thus requires
		 * a refresh of the total filter.
		 */

		if (event.getCurrentCompartmentId().equals(this.compartmentId) || event.getPreviousCompartmentId().equals(this.compartmentId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	/**
	 * Returns a single filter sensitivity for
	 * PersonCompartmentChangeObservationEvent events. This sensitivity will
	 * require refreshes for either compartment in the event corresponds to the
	 * compartment associated with this compartment filter.
	 */
	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonCompartmentChangeObservationEvent>(PersonCompartmentChangeObservationEvent.class, this::requiresRefresh));
		return result;
	}

}
