package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum ReportError implements ContractError {
	NULL_CONSUMER("Supplier of Consumer of ActorContext supplied a null consumer"),
	NULL_SUPPLIER("Supplier of Consumer of ActorContext is null"), NULL_REPORT_LABEL("Null report label"),
	NULL_REPORT_PATH("Null report path"), PATH_COLLISION("Report path shared between multiple reports"),
	UNKNOWN_REPORT_LABEL("Unknown report label"), NULL_REPORT_INITIAL_DATA("Null report initial data"),
	DUPLICATE_REPORT_LABEL("Duplicate report label"), NULL_CONTEXT("Null context"),
	NULL_REPORT_PERIOD("Null report period"), NULL_REPORT_HEADER_STRING("Null report header string"),
	NULL_REPORT_HEADER("Null report header"), NULL_REPORT_ITEM_ENTRY("Null report item entry"),
	UNSUPPORTED_REPORT_PERIOD("Unsupported report period");

	@Override
	public String getDescription() {
		return description;
	}

	private final String description;

	private ReportError(String description) {
		this.description = description;
	}
}
