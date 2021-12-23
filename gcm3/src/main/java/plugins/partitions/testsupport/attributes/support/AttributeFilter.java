package plugins.partitions.testsupport.attributes.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.people.support.PersonId;
import util.ContractException;

public final class AttributeFilter extends Filter {

	private final AttributeId attributeId;
	private final Object value;
	private final Equality equality;
	private AttributesDataView attributesDataView;

	private void validateAttributeId(Context context, final AttributeId attributeId) {
		if (attributeId == null) {
			context.throwContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (attributesDataView == null) {
			attributesDataView = context.getDataView(AttributesDataView.class).get();
		}

		if (!attributesDataView.attributeExists(attributeId)) {
			context.throwContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
	}

	private void validateEquality(Context context, final Equality equality) {
		if (equality == null) {
			context.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateValueNotNull(Context context, final Object value) {
		if (value == null) {
			context.throwContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	private void validateValueCompatibility(Context context, final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Object value) {
		if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
			context.throwContractException(AttributeError.INCOMPATIBLE_VALUE,
					"Attribute value " + value + " is not of type " + attributeDefinition.getType().getName() + " and does not match definition of " + attributeId);
		}
	}

	private void validateEqualityCompatibility(Context context, final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(attributeDefinition.getType())) {
			context.throwContractException(PartitionError.NON_COMPARABLE_ATTRIBUTE, "Values for " + attributeId + " are not comparable via " + equality);
		}
	}

	public AttributeFilter(final AttributeId attributeId, final Equality equality, final Object value) {
		this.attributeId = attributeId;
		this.value = value;
		this.equality = equality;
	}

	/**
	 * Validates this attribute filter
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}</li> if the
	 *             filter's attribute id is null
	 * 
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}</li> if
	 *             the filter's attribute id is unknown
	 * 
	 *             <li>{@linkplain PartitionError.NULL_EQUALITY_OPERATOR}</li>if
	 *             the filter's equality operator is null
	 * 
	 *             <li>{@linkplain AttributeError.NULL_ATTRIBUTE_VALUE}</li>if
	 *             the filter's value is null
	 * 
	 *             <li>{@linkplain AttributeError.INCOMPATIBLE_VALUE}</li>if the
	 *             filter's value is incompatible with the attribute definition
	 *             associated with the filter's attribute id.
	 * 
	 *             <li>{@linkplain PartitionError.NON_COMPARABLE_ATTRIBUTE}</li>if
	 *             the filter's value is not a COMPARABLE when the filter's
	 *             equality operator is not EQUALS or NOT_EQUALS.
	 * 
	 */
	@Override
	public void validate(Context context) {
		validateAttributeId(context, attributeId);
		validateEquality(context, equality);
		validateValueNotNull(context, value);
		if (attributesDataView == null) {
			attributesDataView = context.getDataView(AttributesDataView.class).get();
		}
		final AttributeDefinition attributeDefinition = attributesDataView.getAttributeDefinition(attributeId);
		validateValueCompatibility(context, attributeId, attributeDefinition, value);
		validateEqualityCompatibility(context, attributeId, attributeDefinition, equality);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		if(context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		
		if (attributesDataView == null) {
			attributesDataView = context.getDataView(AttributesDataView.class).get();
		}
		
		// we do not assume that the returned attribute value is
		// comparable unless we are forced to.
		final Object attValue = attributesDataView.getAttributeValue(personId, attributeId);

		return evaluate(attValue);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean evaluate(Object propVal) {
		if (equality.equals(Equality.EQUAL)) {
			return propVal.equals(value);
		} else if (equality.equals(Equality.NOT_EQUAL)) {
			return !propVal.equals(value);
		} else {
			Comparable comparableAttributeValue = (Comparable) propVal;
			int evaluation = comparableAttributeValue.compareTo(value);
			return equality.isCompatibleComparisonValue(evaluation);
		}
	}

	private Optional<PersonId> requiresRefresh(Context context, AttributeChangeObservationEvent event) {
		if (event.getAttributeId().equals(attributeId)) {
			if (evaluate(event.getPreviousValue()) != evaluate(event.getCurrentValue())) {
				return Optional.of(event.getPersonId());
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a single filter sensitivity for AttributeChangeObservationEvent
	 * events. This sensitivity will require refreshes for events with the same
	 * attribute id and where the event where the event has different previous
	 * and current values.
	 */
	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<AttributeChangeObservationEvent>(AttributeChangeObservationEvent.class, this::requiresRefresh));
		return result;
	}

}
