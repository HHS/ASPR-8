package lesson.plugins.model.reports;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ReportContext;
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
	public void init(ReportContext reportContext) {
		super.init(reportContext);
	}

	@Override
	protected void flush(ReportContext reportContext) {
		ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportId(getReportId());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);

		PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		for (DiseaseState diseaseState : DiseaseState.values()) {
			int count = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE, diseaseState);
			reportItemBuilder.addValue(count);
		}

		ReportItem reportItem = reportItemBuilder.build();
		reportContext.releaseOutput(reportItem);

	}

}