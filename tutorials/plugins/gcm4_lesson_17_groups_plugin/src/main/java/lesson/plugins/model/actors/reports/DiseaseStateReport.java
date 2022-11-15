package lesson.plugins.model.actors.reports;

import lesson.plugins.model.DiseaseState;
import lesson.plugins.model.PersonProperty;
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
	
	public DiseaseStateReport(ReportId reportId, ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);		
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);//
			for (DiseaseState diseaseState : DiseaseState.values()) {
				reportHeaderBuilder.add(diseaseState.toString().toLowerCase());
			}
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public void init(ActorContext actorContext) {
		super.init(actorContext);
	}

	@Override
	protected void flush(ActorContext actorContext) {
		ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportId(getReportId());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);

		PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		for (DiseaseState diseaseState : DiseaseState.values()) {
			int count = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE, diseaseState);
			reportItemBuilder.addValue(count);
		}

		ReportItem reportItem = reportItemBuilder.build();
		actorContext.releaseOutput(reportItem);

	}

}
