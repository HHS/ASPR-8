package plugins.personproperties.datacontainers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.BooleanPropertyManager;
import plugins.properties.support.DoublePropertyManager;
import plugins.properties.support.EnumPropertyManager;
import plugins.properties.support.FloatPropertyManager;
import plugins.properties.support.IndexedPropertyManager;
import plugins.properties.support.IntPropertyManager;
import plugins.properties.support.ObjectPropertyManager;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;


/**
 * Mutable data manager that backs the {@linkplain PersonPropertyDataView}. This
 * data manager is for internal use by the {@link PersonPropertiesPlugin} and should
 * not be published.
 * 
 * @author Shawn Hatch
 *
 */

public final class PersonPropertyDataManager {

	private final Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();

	private final Map<PersonPropertyId, IndexedPropertyManager> personPropertyManagerMap = new LinkedHashMap<>();

	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */

	private PersonDataView personDataView;

	private IndexedPropertyManager getIndexedPropertyManager(final Context context, final PropertyDefinition propertyDefinition, final int intialSize) {

		IndexedPropertyManager indexedPropertyManager;
		if (propertyDefinition.getType() == Boolean.class) {
			indexedPropertyManager = new BooleanPropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Float.class) {
			indexedPropertyManager = new FloatPropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Double.class) {
			indexedPropertyManager = new DoublePropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Byte.class) {
			indexedPropertyManager = new IntPropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Short.class) {
			indexedPropertyManager = new IntPropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Integer.class) {
			indexedPropertyManager = new IntPropertyManager(context, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Long.class) {
			indexedPropertyManager = new IntPropertyManager(context, propertyDefinition, intialSize);
		} else if (Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			indexedPropertyManager = new EnumPropertyManager(context, propertyDefinition, intialSize);
		} else {
			indexedPropertyManager = new ObjectPropertyManager(context, propertyDefinition, intialSize);
		}
		return indexedPropertyManager;
	}

	/**
	 * Returns the list people without duplications who have the given property
	 * value.
	 * 
	 * Preconditions :
	 * <li>The person property id must be valid</li>
	 *
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		// validatePersonPropertyId(personPropertyId);
		// final PropertyDefinition propertyDefinition =
		// getPersonPropertyDefinition(personPropertyId);
		// validatePersonPropertyValueNotNull(personPropertyValue);
		// validateValueCompatibility(personPropertyId, propertyDefinition,
		// personPropertyValue);

		final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */
		final int n = personDataView.getPersonIdLimit();
		int count = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personDataView.personIndexExists(personIndex)) {
				final PersonId personId = personDataView.getBoxedPersonId(personIndex);
				final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				if (propertyValue.equals(personPropertyValue)) {
					count++;
				}
			}
		}

		/*
		 * Now we fill the list.
		 */
		final List<PersonId> result = new ArrayList<>(count);

		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personDataView.personIndexExists(personIndex)) {
				final PersonId personId = personDataView.getBoxedPersonId(personIndex);
				final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				if (propertyValue.equals(personPropertyValue)) {
					result.add(personId);
				}
			}
		}

		return result;

	}

	/**
	 * Returns the number of people who have the given property value.
	 * 
	 * Preconditions :
	 * <li>The person property id must be valid</li>
	 *
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		// validatePersonPropertyId(personPropertyId);
		// final PropertyDefinition propertyDefinition =
		// getPersonPropertyDefinition(personPropertyId);
		// validatePersonPropertyValueNotNull(personPropertyValue);
		// validateValueCompatibility(personPropertyId, propertyDefinition,
		// personPropertyValue);
		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */

		final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
		final int n = personDataView.getPersonIdLimit();
		int count = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (personDataView.personIndexExists(personIndex)) {
				final PersonId personId = personDataView.getBoxedPersonId(personIndex);
				final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				if (propertyValue.equals(personPropertyValue)) {
					count++;
				}
			}
		}
		return count;

	}

	/**
	 * Returns the property definition for the given person property id. Returns
	 * null for invalid inputs.
	 * 
	 * Preconditions:
	 * <li>if person property id must be valid</li>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		// validatePersonPropertyId(personPropertyId);
		return personPropertyDefinitions.get(personPropertyId);
	}

	/**
	 * Returns the person property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {

		final Set<T> result = new LinkedHashSet<>(personPropertyDefinitions.keySet().size());
		for (final PersonPropertyId personPropertyId : personPropertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}

		return result;
	}

	/**
	 * Returns the time when the property was last assigned to the person
	 * 
	 * Preconditions :
	 * <li>The person id must be valid</li>
	 * <li>The person property id must be valid</li>
	 * <li>The person property must have tracking on</li>
	 *
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		// validatePersonExists(personId);
		// validatePersonPropertyId(personPropertyId);
		// validatePersonPropertyAssignmentTimesTracked(personPropertyId);
		return personPropertyManagerMap.get(personPropertyId).getPropertyTime(personId.getValue());
	}

	/**
	 * Returns the value last assigned to the person for the given property id
	 * 
	 * Preconditions :
	 * <li>The person id must be valid</li>
	 * <li>The person property id must be valid</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		// validatePersonExists(personId);
		// validatePersonPropertyId(personPropertyId);
		return (T) personPropertyManagerMap.get(personPropertyId).getPropertyValue(personId.getValue());
	}

	/**
	 * Removes the person from this manager for non-primitive-based properties.
	 * Some values may be retained for people who have been removed.
	 * 
	 * Preconditions :
	 * <li>The person id must be valid</li>
	 *
	 */
	public void handlePersonRemoval(final PersonId personId) {

		for (final PersonPropertyId personPropertyId : personPropertyManagerMap.keySet()) {
			final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
			indexedPropertyManager.removeId(personId.getValue());
		}
	}

	private final Context context;

	/**
	 * Constructs the person property data manager from the given context
	 * 
	 * @throws ContractException
	 * <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is null</li>
	 */
	public PersonPropertyDataManager(final Context context) {
		if(context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.context = context;
		personDataView = context.getDataView(PersonDataView.class).get();
	}

	/**
	 * Add the person property definition
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_DEFINITION}
	 *             if the property definition is null</li>
	 *             <li>{@linkplain PersonPropertyError#DUPLICATE_PERSON_PROPERTY_DEFINITION}
	 *             if the person property id was previously added</li>
	 */
	public void definePersonProperty(PersonPropertyId personPropertyId, PropertyDefinition propertyDefinition) {
		if (personPropertyId == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}
		if (propertyDefinition == null) {
			context.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_DEFINITION);
		}
		if (personPropertyDefinitions.containsKey(personPropertyId)) {
			context.throwContractException(PersonPropertyError.DUPLICATE_PERSON_PROPERTY_DEFINITION);
		}
		personPropertyDefinitions.put(personPropertyId, propertyDefinition);
		final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(context, propertyDefinition, 0);
		personPropertyManagerMap.put(personPropertyId, indexedPropertyManager);
	}

	/**
	 * Expands internal data structures (by the count) to more efficiently
	 * prepare for a large number of future person additions.
	 */
	public void expandCapacity(int count) {
		for (final PersonPropertyId personPropertyId : personPropertyManagerMap.keySet()) {
			IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
			indexedPropertyManager.incrementCapacity(count);
		}
	}

	/**
	 * Returns true if and only if the person property id is valid.
	 */
	public boolean personPropertyIdExists(final PersonPropertyId personPropertyId) {
		return personPropertyDefinitions.containsKey(personPropertyId);
	}

	/**
	 * Sets the person's property value
	 * 
	 * Preconditions :
	 * <li>The person id must be valid</li>
	 * <li>The person property id must be valid</li>
	 * <li>The value should be non-null</li>
	 * <li>The value should compatible with the associated property
	 * definition</li>
	 *
	 */
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		personPropertyManagerMap.get(personPropertyId).setPropertyValue(personId.getValue(), personPropertyValue);
	}

}
