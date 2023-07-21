package lesson.plugins.vaccine.support;

import util.errors.ContractError;
import util.errors.ContractException;

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
