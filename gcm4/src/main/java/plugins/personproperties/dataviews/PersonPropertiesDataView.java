package plugins.personproperties.dataviews;

import java.util.List;
import java.util.Set;

import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Data view of the PersonPropertiesDataManager
 *
 */

public final class PersonPropertiesDataView implements DataView {
	
	private final PersonPropertiesDataManager personPropertiesDataManager;
	
	/**
	 * Constructs this view from the corresponding data manager 
	 * 
	 */
	public PersonPropertiesDataView(PersonPropertiesDataManager personPropertiesDataManager) {
		this.personPropertiesDataManager  = personPropertiesDataManager;
	}
	

	

	/**
	 * Returns the list(no duplicates) people who have the given person property
	 * value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertiesDataManager.getPeopleWithPropertyValue(personPropertyId, personPropertyValue);
	}

	/**
	 * Returns the number of people who have the given person property value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertiesDataManager.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
	}

	/**
	 * Returns the property definition for the given person property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		return personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
	}

	/**
	 * Returns the person property ids
	 */
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		return personPropertiesDataManager.getPersonPropertyIds();
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if the person property does not have time tracking turned on
	 *             in the associated property definition</li>
	 * 
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		return personPropertiesDataManager.getPersonPropertyTime(personId, personPropertyId);
	}

	/**
	 * Returns the current value of the person's property
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 * 
	 */
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		return personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
	}

	

	/**
	 * Returns true if and only if the person property id is valid.
	 */
	public boolean personPropertyIdExists(final PersonPropertyId personPropertyId) {
		return personPropertiesDataManager.personPropertyIdExists(personPropertyId);
	}

}
