package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum AttributeError implements ContractError {

	DUPLICATE_ATTRIBUTE_DEFINITION("Duplicate attribute definition"),// 
	INCOMPATIBLE_DEFAULT_VALUE("Incompatible default value"),// 
	NULL_DEFAULT_VALUE("Null default value"),//
	INCOMPATIBLE_VALUE("Atrribute value is incompatible with the attribute definition"), //
	NULL_ATTRIBUTE_DEFINITION("Null attribute definition"), //
	NULL_ATTRIBUTE_ID("Null attribute id"), //
	NULL_ATTRIBUTE_INITIAL_DATA("Null attribute initial data"), //
	NULL_ATTRIBUTE_TYPE("Null attribute type"), //
	NULL_ATTRIBUTE_VALUE("Null attribute value"), //
	NULL_ATTRIBUTES_PLUGIN_DATA("null attributes plugin data"),
	UNKNOWN_ATTRIBUTE_ID("Unknown attribute id"), //
	UNKNOWN_PERSON_HAS_ATTRIBUTE_VALUE_ASSIGNMENT("Unknown person has attribute value assignment"), //
	;//

	private final String description;

	private AttributeError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
