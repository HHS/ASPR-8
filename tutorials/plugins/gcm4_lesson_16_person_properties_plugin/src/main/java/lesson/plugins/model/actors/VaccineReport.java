package lesson.plugins.vaccine;

import nucleus.ActorContext;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

public class VaccineReport extends PeriodicReport {

	public VaccineReport(ReportId reportId, ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);

		ReportHeader.Builder builder = ReportHeader.builder();

		reportHeader = addTimeFieldHeaders(builder)//
													.add("vaccinated")//
													.add("unvaccinated")//
													.build();

	}

	private VaccinationDataManager vaccinationDataManager;

	public void init(ActorContext actorContext) {
		super.init(actorContext);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
	}

	private ReportHeader reportHeader;

	@Override
	protected void flush(ActorContext actorContext) {
		ReportItem.Builder builder = ReportItem	.builder()//
												.setReportId(getReportId())//
												.setReportHeader(reportHeader);
		fillTimeFields(builder);
		builder.addValue(vaccinationDataManager.getVaccinatedPeople().size());
		builder.addValue(vaccinationDataManager.getUnvaccinatedPeople().size());
		ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);
	}

}
