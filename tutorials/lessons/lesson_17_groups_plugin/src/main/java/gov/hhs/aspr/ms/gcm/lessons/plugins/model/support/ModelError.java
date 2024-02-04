package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum ModelError implements ContractError {

	NEGATIVE_REGION_ID("Negative region id"),;

	private final String description;

	private ModelError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
