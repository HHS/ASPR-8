package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum VaccineError implements ContractError {

	NEGATIVE_VACCINE_COUNT("Negative vaccine count for person initialization"),

	;

	private final String description;

	private VaccineError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
