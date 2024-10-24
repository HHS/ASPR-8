package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class DiseaseStateReport extends PeriodicReport {

	public DiseaseStateReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);
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
	protected void flush(ReportContext reportContext) {
		ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportLabel(getReportLabel());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);

		PersonPropertiesDataManager personPropertiesDataManager = reportContext
				.getDataManager(PersonPropertiesDataManager.class);
		for (DiseaseState diseaseState : DiseaseState.values()) {
			int count = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE,
					diseaseState);
			reportItemBuilder.addValue(count);
		}

		ReportItem reportItem = reportItemBuilder.build();
		reportContext.releaseOutput(reportItem);

	}

	@Override
	protected void prepare(ReportContext reportContext) {
		//does nothing
		
	}

}
