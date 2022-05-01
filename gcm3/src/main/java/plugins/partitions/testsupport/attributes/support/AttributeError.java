package plugins.partitions.testsupport.attributes.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum AttributeError implements ContractError {

	
	NULL_ATTRIBUTE_INITIAL_DATA("Null attribute initial data"),//
	NULL_ATTRIBUTE_DATA_MANAGER("Null attributes data manager"),//
	NULL_ATTRIBUTE_ID("Null attribute id"),//	
	NULL_ATTRIBUTE_DEFINITION("Null attribute definition"),//
	NULL_ATTRIBUTE_VALUE("Null attribute value"),//	
	NULL_ATTRIBUTE_TYPE("Null attribute type"),//
	INCOMPATIBLE_DEFAULT_VALUE("Incompatible default value"),
	NULL_DEFAULT_VALUE("Null default value"),
	UNKNOWN_ATTRIBUTE_ID("Unknown attribute id"),//
	INCOMPATIBLE_VALUE("Atrribute value is incompatible with the attribute definition"),//
	DUPLICATE_ATTRIBUTE_DEFINITION("Duplicate attribute definition");//

	private final String description;

	private AttributeError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
