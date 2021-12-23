package plugins.reports.support;

import util.ContractError;

public enum ReportError implements ContractError{
	NULL_CONSUMER("Supplier of Consumer of ReportContext supplied a null consumer"),
	NULL_SUPPLIER("Supplier of Consumer of ReportContext is null"),
	NULL_REPORT_ID("Null report id"),
	UNKNOWN_REPORT_ID("Unknown report id"),
	NULL_REPORT_DATA_MANAGER("Null report data manager"),
	NULL_REPORT_INITIAL_DATA("Null report initial data"),
	DUPLICATE_REPORT("Duplicate report id"),
	NULL_REPORT_CONTEXT("Null report context"),
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
