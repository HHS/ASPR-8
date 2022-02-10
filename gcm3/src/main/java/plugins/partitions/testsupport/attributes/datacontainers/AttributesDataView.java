package plugins.partitions.testsupport.attributes.datacontainers;

import java.util.Set;

import nucleus.SimulationContext;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * Published data view that provides attribute information.
 * 
 * @author Shawn Hatch
 *
 */

public final class AttributesDataView implements DataView {

	private final SimulationContext simulationContext;

	private final AttributesDataManager attributesDataManager;

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
		validateAttributeId(attributeId);
		return attributesDataManager.getAttributeDefinition(attributeId);
	}

	private void validateAttributeId(AttributeId attributeId) {
		if (attributeId == null) {
			simulationContext.throwContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}

		if (!attributeExists(attributeId)) {
			simulationContext.throwContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}

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
		validateAttributeId(attributeId);
		validatePersonId(personId);
		return attributesDataManager.getAttributeValue(personId, attributeId);
	}

	private void validatePersonId(PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private PersonDataView personDataView;

	/**
	 * Constructs this data manager from the given context
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_DATA_MANAGER} if the context is
	 *             null</li>
	 */
	public AttributesDataView(final SimulationContext simulationContext, AttributesDataManager attributesDataManager) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if(attributesDataManager == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_DATA_MANAGER);
		}
		this.attributesDataManager = attributesDataManager;
		this.simulationContext = simulationContext;
		personDataView = simulationContext.getDataView(PersonDataView.class).get();
	}

	/**
	 * Returns true if and only if the attribute is contained
	 */
	public boolean attributeExists(final AttributeId attributeId) {
		return attributesDataManager.attributeExists(attributeId);
	}

}
