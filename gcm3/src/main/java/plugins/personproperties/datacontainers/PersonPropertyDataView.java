package plugins.personproperties.datacontainers;

import java.util.List;
import java.util.Set;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;

/**
 * Published data view that provides person property information.
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonPropertyDataView implements DataView {
	private PersonDataView personDataView;

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Precondition : the person property id is valid
	 */
	private void validatePersonPropertyAssignmentTimesTracked(final PersonPropertyId personPropertyId) {
		final PropertyDefinition personPropertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
		if (personPropertyDefinition.getTimeTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			context.throwContractException(PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validatePersonPropertyId(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}
		if (!personPropertyDataManager.personPropertyIdExists(personPropertyId)) {
			context.throwContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}

	private void validatePersonPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE);
		}
	}

	/*
	 * Preconditions: all arguments are non-null
	 */
	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			context.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private final PersonPropertyDataManager personPropertyDataManager;

	private final Context context;

	/**
	 * Constructs this person property data view from the given context and
	 * person property data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_DATA_MANAGER}
	 *             if the person property data manager is null</li>
	 * 
	 */
	public PersonPropertyDataView(Context context, PersonPropertyDataManager personDataManager) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personDataManager == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_DATA_MANAGER);
		}
		this.context = context;
		this.personPropertyDataManager = personDataManager;
		personDataView = context.getDataView(PersonDataView.class).get();
	}

	/**
	 * Returns the set of person property ids
	 */
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		return personPropertyDataManager.getPersonPropertyIds();
	}

	/**
	 * Returns true if and only if the person property id exists.
	 * 
	 */
	public boolean personPropertyIdExists(PersonPropertyId personPropertyId) {
		return personPropertyDataManager.personPropertyIdExists(personPropertyId);
	}

	/**
	 * Returns the property definition for the given person property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
	}

	/**
	 * Returns the current value of the person's property
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 * 
	 */
	public <T> T getPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		return personPropertyDataManager.getPersonPropertyValue(personId, personPropertyId);
	}

	/**
	 * Returns the time when the person's property was last assigned or zero if
	 * the value has never been assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if the person property does not have time tracking turned on
	 *             in the associated property definition</li>
	 * 
	 */

	public double getPersonPropertyTime(PersonId personId, PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyAssignmentTimesTracked(personPropertyId);
		return personPropertyDataManager.getPersonPropertyTime(personId, personPropertyId);
	}

	/**
	 * Returns the list(no duplicates) people who have the given person property
	 * value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */

	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		return personPropertyDataManager.getPeopleWithPropertyValue(personPropertyId, personPropertyValue);
	}

	/**
	 * Returns the number of people who have the given person property value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		return personPropertyDataManager.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
	}

}
