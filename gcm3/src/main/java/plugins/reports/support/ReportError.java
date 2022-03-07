package plugins.reports.support;

import nucleus.util.ContractError;

public enum ReportError implements ContractError{
	NULL_CONSUMER("Supplier of Consumer of ActorContext supplied a null consumer"),
	NULL_SUPPLIER("Supplier of Consumer of ActorContext is null"),
	NULL_REPORT_ID("Null report id"),
	UNKNOWN_REPORT_ID("Unknown report id"),
	NULL_REPORT_INITIAL_DATA("Null report initial data"),
	DUPLICATE_REPORT("Duplicate report id"),
	NULL_CONTEXT("Null context"),
	NULL_REPORT_PERIOD("Null report period"),
	NULL_REPORT_HEADER_STRING("Null report header string"),
	NULL_REPORT_HEADER("Null report header"),
	NULL_REPORT_ITEM_ENTRY("Null report item entry"),
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
