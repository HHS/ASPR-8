package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.errors.ContractException;

public final class AttributeFilter extends Filter {

	private final AttributeId attributeId;
	private final Object value;
	private final Equality equality;
	private AttributesDataManager attributesDataManager;

	private void validateAttributeId(PartitionsContext partitionsContext, final AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (attributesDataManager == null) {
			attributesDataManager = partitionsContext.getDataManager(AttributesDataManager.class);
		}

		if (!attributesDataManager.attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
	}

	private void validateEquality(PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateValueNotNull(PartitionsContext partitionsContext, final Object value) {
		if (value == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	private void validateValueCompatibility(PartitionsContext partitionsContext, final AttributeId attributeId,
			final AttributeDefinition attributeDefinition, final Object value) {
		if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
			throw new ContractException(AttributeError.INCOMPATIBLE_VALUE,
					"Attribute value " + value + " is not of type " + attributeDefinition.getType().getName()
							+ " and does not match definition of " + attributeId);
		}
	}

	private void validateEqualityCompatibility(PartitionsContext partitionsContext, final AttributeId attributeId,
			final AttributeDefinition attributeDefinition, final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(attributeDefinition.getType())) {
			throw new ContractException(PartitionError.NON_COMPARABLE_ATTRIBUTE,
					"Values for " + attributeId + " are not comparable via " + equality);
		}
	}

	public AttributeId getAttributeId() {
		return attributeId;
	}

	public Equality getEquality() {
		return equality;
	}

	public Object getValue() {
		return value;
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
	 *                           <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}</li>
	 *                           if the filter's attribute id is null
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}</li>
	 *                           if the filter's attribute id is unknown
	 *                           <li>{@linkplain PartitionError#NULL_EQUALITY_OPERATOR} if
	 *                           the filter's equality operator is null
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_VALUE} if
	 *                           the filter's value is null
	 *                           <li>{@linkplain AttributeError#INCOMPATIBLE_VALUE} if
	 *                           the filter's value is incompatible with the
	 *                           attribute definition associated with the filter's
	 *                           attribute id.
	 *                           <li>{@linkplain PartitionError#NON_COMPARABLE_ATTRIBUTE} if
	 *                           the filter's value is not a COMPARABLE when the
	 *                           filter's equality operator is not EQUALS or
	 *                           NOT_EQUALS.
	 */
	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateAttributeId(partitionsContext, attributeId);
		validateEquality(partitionsContext, equality);
		validateValueNotNull(partitionsContext, value);
		if (attributesDataManager == null) {
			attributesDataManager = partitionsContext.getDataManager(AttributesDataManager.class);
		}
		final AttributeDefinition attributeDefinition = attributesDataManager.getAttributeDefinition(attributeId);
		validateValueCompatibility(partitionsContext, attributeId, attributeDefinition, value);
		validateEqualityCompatibility(partitionsContext, attributeId, attributeDefinition, equality);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {

		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (attributesDataManager == null) {
			attributesDataManager = partitionsContext.getDataManager(AttributesDataManager.class);
		}

		// we do not assume that the returned attribute value is
		// comparable unless we are forced to.
		final Object attValue = attributesDataManager.getAttributeValue(personId, attributeId);

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

	private Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, AttributeUpdateEvent event) {
		if (event.attributeId().equals(attributeId)) {
			if (evaluate(event.previousValue()) != evaluate(event.currentValue())) {
				return Optional.of(event.personId());
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a single filter sensitivity for AttributeUpdateEvent events. This
	 * sensitivity will require refreshes for events with the same attribute id and
	 * where the event where the event has different previous and current values.
	 */
	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<AttributeUpdateEvent>(AttributeUpdateEvent.class, this::requiresRefresh));
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeId == null) ? 0 : attributeId.hashCode());
		result = prime * result + ((equality == null) ? 0 : equality.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AttributeFilter)) {
			return false;
		}
		AttributeFilter other = (AttributeFilter) obj;
		if (attributeId == null) {
			if (other.attributeId != null) {
				return false;
			}
		} else if (!attributeId.equals(other.attributeId)) {
			return false;
		}
		if (equality != other.equality) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AttributeFilter [attributeId=");
		builder.append(attributeId);
		builder.append(", value=");
		builder.append(value);
		builder.append(", equality=");
		builder.append(equality);
		builder.append(", attributesDataManager=");
		builder.append(attributesDataManager);
		builder.append("]");
		return builder.toString();
	}

}
