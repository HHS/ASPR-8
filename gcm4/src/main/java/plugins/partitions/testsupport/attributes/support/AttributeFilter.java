package plugins.partitions.testsupport.attributes.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Equality;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class AttributeFilter extends Filter {

	private final AttributeId attributeId;
	private final Object value;
	private final Equality equality;
	private AttributesDataManager attributesDataManager;

	private void validateAttributeId(SimulationContext simulationContext, final AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (attributesDataManager == null) {
			attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class);
		}

		if (!attributesDataManager.attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
	}

	private void validateEquality(SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateValueNotNull(SimulationContext simulationContext, final Object value) {
		if (value == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	private void validateValueCompatibility(SimulationContext simulationContext, final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Object value) {
		if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
			throw new ContractException(AttributeError.INCOMPATIBLE_VALUE,
					"Attribute value " + value + " is not of type " + attributeDefinition.getType().getName() + " and does not match definition of " + attributeId);
		}
	}

	private void validateEqualityCompatibility(SimulationContext simulationContext, final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(attributeDefinition.getType())) {
			throw new ContractException(PartitionError.NON_COMPARABLE_ATTRIBUTE, "Values for " + attributeId + " are not comparable via " + equality);
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
	public void validate(SimulationContext simulationContext) {
		validateAttributeId(simulationContext, attributeId);
		validateEquality(simulationContext, equality);
		validateValueNotNull(simulationContext, value);
		if (attributesDataManager == null) {
			attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class);
		}
		final AttributeDefinition attributeDefinition = attributesDataManager.getAttributeDefinition(attributeId);
		validateValueCompatibility(simulationContext, attributeId, attributeDefinition, value);
		validateEqualityCompatibility(simulationContext, attributeId, attributeDefinition, equality);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		
		if (attributesDataManager == null) {
			attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class);
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

	private Optional<PersonId> requiresRefresh(SimulationContext simulationContext, AttributeUpdateEvent event) {
		if (event.attributeId().equals(attributeId)) {
			if (evaluate(event.previousValue()) != evaluate(event.currentValue())) {
				return Optional.of(event.personId());
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a single filter sensitivity for AttributeUpdateEvent
	 * events. This sensitivity will require refreshes for events with the same
	 * attribute id and where the event where the event has different previous
	 * and current values.
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
	
	

}
