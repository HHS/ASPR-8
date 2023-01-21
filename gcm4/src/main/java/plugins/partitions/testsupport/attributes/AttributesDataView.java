package plugins.partitions.testsupport.attributes;

import java.util.Set;

import nucleus.DataView;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * Data view of the AttributesDataManager
 *
 */

public final class AttributesDataView implements DataView {
	
	private final AttributesDataManager attributesDataManager; 


	/**
	 * Constructs this view from the corresponding data manager
	 * 
	 */
	public AttributesDataView(AttributesDataManager attributesDataManager) {
		this.attributesDataManager = attributesDataManager;
	}
	
	
	/**
	 * Returns the attribute definition associated with the given attribute id
	 * without validation.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
	 *             attribute id is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the
	 *             attribute id unknown</li>
	 */
	public AttributeDefinition getAttributeDefinition(final AttributeId attributeId) {
		return attributesDataManager.getAttributeDefinition(attributeId);
	}

	/**
	 * Returns the attribute ids
	 */
	public <T extends AttributeId> Set<T> getAttributeIds() {
		return attributesDataManager.getAttributeIds();
	}

	/**
	 * Returns the attribute value associated with the given attribute id
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID} if the
	 *             attribute id is null</li>
	 *             <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID} if the
	 *             attribute id unknown</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id unknown</li>
	 */
	public <T> T getAttributeValue(final PersonId personId, final AttributeId attributeId) {
		return attributesDataManager.getAttributeValue(personId, attributeId);
	}

	/**
	 * Returns true if and only if the attribute is contained
	 */
	public boolean attributeExists(final AttributeId attributeId) {
		return attributesDataManager.attributeExists(attributeId);
	}

}
