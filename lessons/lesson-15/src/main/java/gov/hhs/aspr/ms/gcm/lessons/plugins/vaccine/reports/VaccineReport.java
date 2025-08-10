package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public final class VaccineReport extends PeriodicReport {

	private final int maxVaccinatedCount;

	public VaccineReport(ReportLabel reportLabel, ReportPeriod reportPeriod, int maxVaccineCount) {
		super(reportLabel, reportPeriod);

		this.maxVaccinatedCount = FastMath.max(0, maxVaccineCount);

		ReportHeader.Builder builder = ReportHeader.builder();
		addTimeFieldHeaders(builder);
		for (int i = 0; i < maxVaccineCount; i++) {
			builder.add("count_" + i);
		}
		builder.add("count_" + maxVaccineCount + "+");
		builder.setReportLabel(reportLabel);
		reportHeader = builder.build();
	}

	private VaccinationDataManager vaccinationDataManager;

	private PeopleDataManager peopleDataManager;

	protected void prepare(ReportContext reportContext) {
		vaccinationDataManager = reportContext.getDataManager(VaccinationDataManager.class);
		peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);

		reportContext.releaseOutput(reportHeader);
	}

	private ReportHeader reportHeader;

	@Override
	protected void flush(ReportContext reportContext) {
		Map<Integer, MutableInteger> peopleByVaccineCount = new LinkedHashMap<>();
		for (int i = 0; i <= maxVaccinatedCount; i++) {
			peopleByVaccineCount.put(i, new MutableInteger());
		}
		for (PersonId personId : peopleDataManager.getPeople()) {
			int vaccinationCount = vaccinationDataManager.getPersonVaccinationCount(personId);
			MutableInteger mutableInteger = peopleByVaccineCount.get(vaccinationCount);
			if (mutableInteger == null) {
				mutableInteger = peopleByVaccineCount.get(maxVaccinatedCount);
			}
			mutableInteger.increment();
		}
		ReportItem.Builder builder = ReportItem.builder()//
				.setReportLabel(getReportLabel());
		fillTimeFields(builder);

		for (int i = 0; i <= maxVaccinatedCount; i++) {
			builder.addValue(peopleByVaccineCount.get(i).getValue());
		}

		ReportItem reportItem = builder.build();
		reportContext.releaseOutput(reportItem);
	}

}
