package plugins.personproperties.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import util.ContractException;

public final class PropertyFilter extends Filter {

	private final PersonPropertyId personPropertyId;
	private final Object personPropertyValue;
	private final Equality equality;
	private PersonPropertyDataView personPropertyDataView;

	private void validatePersonPropertyId(Context context, final PersonPropertyId personPropertyId) {
		
		if (personPropertyDataView == null) {
			personPropertyDataView = context.getDataView(PersonPropertyDataView.class).get();
		}
		
		if (personPropertyId == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}

		if (!personPropertyDataView.personPropertyIdExists(personPropertyId)) {
			context.throwContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}

	private void validateEquality(Context context, final Equality equality) {
		if (equality == null) {
			context.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validatePersonPropertyValueNotNull(Context context, final Object propertyValue) {
		if (propertyValue == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(Context context, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			context.throwContractException(PropertyError.INCOMPATIBLE_VALUE, "Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateEqualityCompatibility(Context context, final Object propertyId, final PropertyDefinition propertyDefinition, final Equality equality) {

		if (equality == Equality.EQUAL) {
			return;
		}
		if (equality == Equality.NOT_EQUAL) {
			return;
		}

		if (!Comparable.class.isAssignableFrom(propertyDefinition.getType())) {
			context.throwContractException(PartitionError.NON_COMPARABLE_ATTRIBUTE, "Property values for " + propertyId + " are not comparable via " + equality);
		}
	}

	public PropertyFilter(final PersonPropertyId personPropertyId, final Equality equality, final Object personPropertyValue) {
		this.personPropertyId = personPropertyId;
		this.personPropertyValue = personPropertyValue;
		this.equality = equality;
	}

	@Override
	public void validate(Context context) {
		if(context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personPropertyDataView == null) {
			personPropertyDataView = context.getDataView(PersonPropertyDataView.class).get();
		}
		
		validatePersonPropertyId(context, personPropertyId);
		validateEquality(context, equality);
		validatePersonPropertyValueNotNull(context, personPropertyValue);		
		final PropertyDefinition propertyDefinition = personPropertyDataView.getPersonPropertyDefinition(personPropertyId);
		validateValueCompatibility(context, personPropertyId, propertyDefinition, personPropertyValue);
		validateEqualityCompatibility(context, personPropertyId, propertyDefinition, equality);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		
		if(context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personPropertyDataView == null) {
			personPropertyDataView = context.getDataView(PersonPropertyDataView.class).get();
		}
		
		// we do not assume that the returned property value is
		// comparable unless we are forced to.
		final Object propVal = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);

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

	private Optional<PersonId> requiresRefresh(Context context, PersonPropertyChangeObservationEvent event) {
		if (event.getPersonPropertyId().equals(personPropertyId)) {
			if (evaluate(event.getPreviousPropertyValue()) != evaluate(event.getCurrentPropertyValue())) {
				return Optional.of(event.getPersonId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonPropertyChangeObservationEvent>(PersonPropertyChangeObservationEvent.class, this::requiresRefresh));
		return result;
	}

}
