package lesson.plugins.model.actors.reports;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 *
 * @author Shawn Hatch
 *
 */
public final class DiseaseStateReport extends PeriodicReport {

	private ReportHeader reportHeader;

	public DiseaseStateReport(final ReportId reportId, final ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);
	}

	@Override
	protected void flush(final ActorContext actorContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportId(getReportId());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);
		reportItemBuilder.addValue(actorContext.getTime());

		final PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		for (final DiseaseState diseaseState : DiseaseState.values()) {
			final int count = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE, diseaseState);
			reportItemBuilder.addValue(count);
		}

		final ReportItem reportItem = reportItemBuilder.build();
		actorContext.releaseOutput(reportItem);

	}

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			final ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);//
			reportHeaderBuilder.add("day");
			for (final DiseaseState diseaseState : DiseaseState.values()) {
				reportHeaderBuilder.add(diseaseState.toString().toLowerCase());
			}
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);
	}

}
