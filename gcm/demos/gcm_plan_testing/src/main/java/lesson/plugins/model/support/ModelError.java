package lesson.plugins.model.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 *
 *
 */
public enum ModelError implements ContractError {

	NULL_ANTIGEN_PRODUCER_PLUGIN_DATA("Null antigen producer plugin data"),
	NULL_VACCINE_PRODUCER_PLUGIN_DATA("Null vaccine producer plugin data"),
	NULL_VACCINATOR_PLUGIN_DATA("Null vaccinator plugin data"),
	NULL_CONTACT_MANAGER_PLUGIN_DATA("Null contact manager plugin data"),
	ANTIGEN_PRODUCER_LAST_BATCH_ASSEMBLY_END_TIME("Improper last batch assembly end time"),
	MATERIALS_MISMATCH("A set of materials related assignments are mismatched"),
	NEGATIVE_REGION_ID("Negative region id");

	private final String description;

	private ModelError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
