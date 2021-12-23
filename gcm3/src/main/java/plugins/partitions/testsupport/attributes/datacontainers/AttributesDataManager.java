package plugins.partitions.testsupport.attributes.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Mutable data manager that backs the {@linkplain AttributeDataView}. This data
 * manager is for internal use by the {@link AttributesPlugin} and should not be
 * published.
 * 
 * @author Shawn Hatch
 *
 */

public final class AttributesDataManager {

	private final Context context;

	private final Map<AttributeId, AttributeDefinition> attributeDefinitions = new LinkedHashMap<>();

	private final Map<AttributeId, Map<PersonId, Object>> attributeValues = new LinkedHashMap<>();

	/**
	 * Returns the attribute definition associated with the given attribute id
	 * without validation. Returns null if the attributeId is not contained.
	 */
	public AttributeDefinition getAttributeDefinition(final AttributeId attributeId) {
		return attributeDefinitions.get(attributeId);
	}

	/**
	 * Returns the attribute ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributeId> Set<T> getAttributeIds() {
		final Set<T> result = new LinkedHashSet<>(attributeDefinitions.keySet().size());
		for (final AttributeId attributeId : attributeDefinitions.keySet()) {
			result.add((T) attributeId);
		}
		return result;
	}

	/**
	 * Returns the person's attribute value without validation. Returns the
	 * default value of the corresponding attribute definition if the person is
	 * null, does not exist or does not have a recorded value for the attribute
	 * id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the attribute id is unknown</li>
	 *             <li>if the attribute id is null</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttributeValue(final PersonId personId, final AttributeId attributeId) {
		Object value = attributeValues.get(attributeId).get(personId);
		if (value == null) {
			value = attributeDefinitions.get(attributeId).getDefaultValue();
		}
		return (T) value;
	}

	/**
	 * Remove attribute values for the person with no validation. Attribute
	 * value for a removed person will return to the associated default values
	 * for the corresponding attribute definitions.Tolerates null values.
	 */
	public void handlePersonRemoval(final PersonId personId) {
		for (AttributeId attributeId : attributeValues.keySet()) {
			attributeValues.get(attributeId).remove(personId);
		}

	}

	/**
	 * Constructs this data manager from the given context
	 * 
	 * @throws ContractException
	 *             <li>if the context is null</li>
	 */
	public AttributesDataManager(final Context context) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.context = context;
	}

	/**
	 * Returns true if and only if the attribute is contained
	 */
	public boolean attributeExists(final AttributeId attributeId) {
		return attributeDefinitions.containsKey(attributeId);
	}

	/**
	 * Sets the attribute value with no validation
	 * 
	 * Preconditions : the person id should not be null and the value should not be null
	 * 
	 * @throws RuntimeException
	 * <li>if the attribute id is null</li>
	 * <li>if the attribute id is unknown</li>
	 */
	public void setAttributeValue(final PersonId personId, final AttributeId attributeId, final Object value) {
		attributeValues.get(attributeId).put(personId, value);
	}

	/**
	 * Adds an attribute definition
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
	 *             attribute id is null</li>
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_DEFINITION} if
	 *             the attribute definition is null</li>
	 *             <li>{@linkplain AttributeError#DUPLICATE_ATTRIBUTE_DEFINITION}
	 *             if the attribute definition was previously added</li>
	 */
	public void addAttribute(AttributeId attributeId, AttributeDefinition attributeDefinition) {
		if (attributeId == null) {
			context.throwContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}

		if (attributeDefinition == null) {
			context.throwContractException(AttributeError.NULL_ATTRIBUTE_DEFINITION);
		}

		if (attributeDefinitions.containsKey(attributeId)) {
			context.throwContractException(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION);
		}

		attributeDefinitions.put(attributeId, attributeDefinition);
		attributeValues.put(attributeId, new LinkedHashMap<>());
	}

}
