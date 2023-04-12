package lesson.plugins.model.reports;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ReportContext;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 *
 *
 */
public final class DiseaseStateReport extends PeriodicReport {

	private ReportHeader reportHeader;

	public DiseaseStateReport(final ReportLabel reportLabel, final ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);
	}

	@Override
	protected void flush(final ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportLabel(getReportLabel());
		reportItemBuilder.setReportHeader(getReportHeader());
		fillTimeFields(reportItemBuilder);
		reportItemBuilder.addValue(reportContext.getTime());
		
		
		final PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		int vaccinatedCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINATED, true);
		reportItemBuilder.addValue(vaccinatedCount);
		for (final DiseaseState diseaseState : DiseaseState.values()) {
			final int count = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.DISEASE_STATE, diseaseState);
			reportItemBuilder.addValue(count);
		}

		final ReportItem reportItem = reportItemBuilder.build();
		reportContext.releaseOutput(reportItem);

	}

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			final ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder);//
			reportHeaderBuilder.add("day");
			reportHeaderBuilder.add("vaccinated");
			for (final DiseaseState diseaseState : DiseaseState.values()) {
				reportHeaderBuilder.add(diseaseState.toString().toLowerCase());
			}
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	

}
