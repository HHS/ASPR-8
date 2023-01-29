package lesson.plugins.model.actors.reports;

import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;


public final class VaccineReport extends PeriodicReport {

	private ReportHeader reportHeader;

	public VaccineReport(final ReportId reportId, final ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);
	}

	@Override
	protected void flush(final ActorContext actorContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportId(getReportId());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);				
		
		final PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		int vaccinatedCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINATED, true);
		reportItemBuilder.addValue(vaccinatedCount);
		int vaccineScheduledCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINE_SCHEDULED, true);
		reportItemBuilder.addValue(vaccineScheduledCount);

		final ReportItem reportItem = reportItemBuilder.build();
		actorContext.releaseOutput(reportItem);
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

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);
	}

}
