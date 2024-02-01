package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import util.errors.ContractException;

public final class PersonPropertyFilter extends Filter {

	private final PersonPropertyId personPropertyId;
	private final Object personPropertyValue;
	private final Equality equality;
	private PersonPropertiesDataManager personPropertiesDataManager;

	private void validatePersonPropertyId(PartitionsContext partitionsContext,
			final PersonPropertyId personPropertyId) {

		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = partitionsContext.getDataManager(PersonPropertiesDataManager.class);
		}

		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!personPropertiesDataManager.personPropertyIdExists(personPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
		}
	}

	private void validateEquality(PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validatePersonPropertyValueNotNull(PartitionsContext partitionsContext, final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(PartitionsContext partitionsContext, final Object propertyId,
			final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
		}
	}

	private void validateEqualityCompatibility(PartitionsContext partitionsContext, final Object propertyId,
			final PropertyDefinition propertyDefinition, final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(propertyDefinition.getType())) {
			throw new ContractException(PartitionError.NON_COMPARABLE_ATTRIBUTE,
					"Property values for " + propertyId + " are not comparable via " + equality);
		}
	}

	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	public Equality getEquality() {
		return equality;
	}

	public Object getPersonPropertyValue() {
		return personPropertyValue;
	}

	public PersonPropertyFilter(final PersonPropertyId personPropertyId, final Equality equality,
			final Object personPropertyValue) {
		this.personPropertyId = personPropertyId;
		this.personPropertyValue = personPropertyValue;
		this.equality = equality;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = partitionsContext.getDataManager(PersonPropertiesDataManager.class);
		}

		validatePersonPropertyId(partitionsContext, personPropertyId);
		validateEquality(partitionsContext, equality);
		validatePersonPropertyValueNotNull(partitionsContext, personPropertyValue);
		final PropertyDefinition propertyDefinition = personPropertiesDataManager
				.getPersonPropertyDefinition(personPropertyId);
		validateValueCompatibility(partitionsContext, personPropertyId, propertyDefinition, personPropertyValue);
		validateEqualityCompatibility(partitionsContext, personPropertyId, propertyDefinition, equality);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {

		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (personPropertiesDataManager == null) {
			personPropertiesDataManager = partitionsContext.getDataManager(PersonPropertiesDataManager.class);
		}

		// we do not assume that the returned property value is
		// comparable unless we are forced to.
		final Object propVal = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);

		return evaluate(propVal);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean evaluate(Object propVal) {
		if (equality.equals(Equality.EQUAL)) {
			return propVal.equals(personPropertyValue);
		} else if (equality.equals(Equality.NOT_EQUAL)) {
			return !propVal.equals(personPropertyValue);
		} else {
			Comparable comparablePropertyValue = (Comparable) propVal;
			int evaluation = comparablePropertyValue.compareTo(personPropertyValue);
			return equality.isCompatibleComparisonValue(evaluation);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyFilter [personPropertyId=");
		builder.append(personPropertyId);
		builder.append(", personPropertyValue=");
		builder.append(personPropertyValue);
		builder.append(", equality=");
		builder.append(equality);
		builder.append("]");
		return builder.toString();
	}

	private Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, PersonPropertyUpdateEvent event) {
		if (event.personPropertyId().equals(personPropertyId)) {
			if (evaluate(event.previousPropertyValue()) != evaluate(event.currentPropertyValue())) {
				return Optional.of(event.personId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonPropertyUpdateEvent>(PersonPropertyUpdateEvent.class,
				this::requiresRefresh));
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equality == null) ? 0 : equality.hashCode());
		result = prime * result + ((personPropertyId == null) ? 0 : personPropertyId.hashCode());
		result = prime * result + ((personPropertyValue == null) ? 0 : personPropertyValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonPropertyFilter)) {
			return false;
		}
		PersonPropertyFilter other = (PersonPropertyFilter) obj;
		if (equality != other.equality) {
			return false;
		}
		if (personPropertyId == null) {
			if (other.personPropertyId != null) {
				return false;
			}
		} else if (!personPropertyId.equals(other.personPropertyId)) {
			return false;
		}
		if (personPropertyValue == null) {
			if (other.personPropertyValue != null) {
				return false;
			}
		} else if (!personPropertyValue.equals(other.personPropertyValue)) {
			return false;
		}
		return true;
	}

}
