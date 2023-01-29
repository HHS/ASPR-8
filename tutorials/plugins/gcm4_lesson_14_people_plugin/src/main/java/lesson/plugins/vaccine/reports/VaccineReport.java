package lesson.plugins.vaccine;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.wrappers.MutableInteger;

public final class VaccineReport extends PeriodicReport {

	private final int maxVaccinedCount;

	public VaccineReport(ReportId reportId, ReportPeriod reportPeriod, int maxVaccineCount) {
		super(reportId, reportPeriod);

		this.maxVaccinedCount = FastMath.max(0, maxVaccineCount);

		ReportHeader.Builder builder = ReportHeader.builder();
		addTimeFieldHeaders(builder);
		for (int i = 0; i < maxVaccineCount; i++) {
			builder.add("count_" + i);
		}
		builder.add("count_" + maxVaccineCount + "+");

		reportHeader = builder.build();
	}

	private VaccinationDataManager vaccinationDataManager;

	private PeopleDataManager peopleDataManager;

	public void init(ActorContext actorContext) {
		super.init(actorContext);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
	}

	private ReportHeader reportHeader;

	@Override
	protected void flush(ActorContext actorContext) {
		Map<Integer, MutableInteger> peopleByVaccineCount = new LinkedHashMap<>();
		for (int i = 0; i <= maxVaccinedCount; i++) {
			peopleByVaccineCount.put(i, new MutableInteger());
		}
		for (PersonId personId : peopleDataManager.getPeople()) {
			int vaccinationCount = vaccinationDataManager.getPersonVaccinationCount(personId);
			MutableInteger mutableInteger = peopleByVaccineCount.get(vaccinationCount);
			if (mutableInteger == null) {
				mutableInteger = peopleByVaccineCount.get(maxVaccinedCount);				
			}
			mutableInteger.increment();
		}
		ReportItem.Builder builder = ReportItem	.builder()//
												.setReportId(getReportId())//
												.setReportHeader(reportHeader);
		fillTimeFields(builder);
		
		for (int i = 0; i <= maxVaccinedCount; i++) {
			builder.addValue(peopleByVaccineCount.get(i).getValue());
		}

		ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);
	}

}
