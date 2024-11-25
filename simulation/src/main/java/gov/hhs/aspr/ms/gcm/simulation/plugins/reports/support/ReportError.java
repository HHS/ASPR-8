package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum ReportError implements ContractError {
	NULL_CONTEXT("Null context"),
	NULL_REPORT_HEADER("Null report header"), 
	NULL_REPORT_HEADER_STRING("Null report header string"),
	NULL_REPORT_ITEM_ENTRY("Null report item entry"),
	NULL_REPORT_LABEL("Null report label"),
	NULL_REPORT_PATH("Null report path"), 
	NULL_REPORT_PERIOD("Null report period"), 
	PATH_COLLISION("Report path shared between multiple reports"),
	HEADER_COLLISION("Report header shared between multiple reports"),
	NO_REPORT_HEADER("Tried to write a report item to a report that did not establish a report header. Make sure the report call reportContext.releaseOutput() with the reports header"),
	;

	@Override
	public String getDescription() {
		return description;
	}

	private final String description;

	private ReportError(String description) {
		this.description = description;
	}
}
