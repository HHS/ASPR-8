package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;

public final class VaccineReport extends PeriodicReport {

	private ReportHeader reportHeader;

	public VaccineReport(final ReportLabel reportLabel, final ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);
	}

	@Override
	protected void flush(final ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportLabel(getReportLabel());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);

		final PersonPropertiesDataManager personPropertiesDataManager = reportContext
				.getDataManager(PersonPropertiesDataManager.class);
		int vaccinatedCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINATED,
				true);
		reportItemBuilder.addValue(vaccinatedCount);
		int vaccineScheduledCount = personPropertiesDataManager
				.getPersonCountForPropertyValue(PersonProperty.VACCINE_SCHEDULED, true);
		reportItemBuilder.addValue(vaccineScheduledCount);

		final ReportItem reportItem = reportItemBuilder.build();
		reportContext.releaseOutput(reportItem);
	}

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			final ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);//
			reportHeaderBuilder.add("vaccine_scheduled");
			reportHeaderBuilder.add("vaccinated");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

}
