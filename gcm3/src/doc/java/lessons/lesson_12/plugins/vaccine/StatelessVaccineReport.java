package lessons.lesson_12.plugins.vaccine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lessons.lesson_12.plugins.family.FamilyDataManager;
import lessons.lesson_12.plugins.family.FamilyId;
import lessons.lesson_12.plugins.person.PersonDataManager;
import lessons.lesson_12.plugins.person.PersonId;
import nucleus.ActorContext;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.wrappers.MutableInteger;

public class StatelessVaccineReport extends PeriodicReport {

	private static enum VaccineStatus {
		FAMILY_NONE("unvacinated_families"),
		FAMILY_PARTIAL("partially_vaccinated_families"),
		FAMILY_FULL("fully_vaccinated_families"),
		INDIVIDUAL_NONE("unvaccinated_individuals"),
		INDIVIDUAL_FULL("vaccinated_individuals");

		private final String description;

		private VaccineStatus(String description) {
			this.description = description;
		}
	}
	
	public StatelessVaccineReport(ReportId reportId, ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);
	}

	@Override
	protected void flush(ActorContext actorContext) {

		FamilyDataManager familyDataManager = actorContext.getDataManager(FamilyDataManager.class);
		VaccinationDataManager vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);

		Map<VaccineStatus, MutableInteger> statusMap = new LinkedHashMap<>();
		for (VaccineStatus vaccineStatus : VaccineStatus.values()) {
			statusMap.put(vaccineStatus, new MutableInteger());
		}

		// determine the family vaccine status for every family
		for (FamilyId familyId : familyDataManager.getFamilyIds()) {
			VaccineStatus vaccineStatus = getFamilyStatus(familyId, vaccinationDataManager, familyDataManager);
			statusMap.get(vaccineStatus).increment();
		}

		// ensure that any person not assigned to a family is still counted
		for (PersonId personId : personDataManager.getPeople()) {
			if (familyDataManager.getFamilyId(personId).isEmpty()) {
				VaccineStatus vaccineStatus = getIndividualStatus(personId, vaccinationDataManager);
				statusMap.get(vaccineStatus).increment();
			}
		}
		ReportHeader.Builder headerBuilder = ReportHeader.builder();
		addTimeFieldHeaders(headerBuilder);
		for (VaccineStatus vaccineStatus : VaccineStatus.values()) {
			headerBuilder.add(vaccineStatus.description);
		}
		ReportHeader reportHeader = headerBuilder.build();
		
		ReportItem.Builder builder = ReportItem	.builder()//
												.setReportId(getReportId())//
												.setReportHeader(reportHeader);
		fillTimeFields(builder);
		for (VaccineStatus vaccineStatus : VaccineStatus.values()) {
			int value = statusMap.get(vaccineStatus).getValue();
			builder.addValue(value);
		}

		ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);

	}

	

	private VaccineStatus getFamilyStatus(FamilyId familyId, VaccinationDataManager vaccinationDataManager, FamilyDataManager familyDataManager) {

		int familySize = familyDataManager.getFamilySize(familyId);
		List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
		int vaccinatedCount = 0;
		for (PersonId personId : familyMembers) {
			if (vaccinationDataManager.isPersonVaccinated(personId)) {
				vaccinatedCount++;
			}
		}
		VaccineStatus result;

		if (vaccinatedCount == 0) {
			result = VaccineStatus.FAMILY_NONE;
		} else if (vaccinatedCount == familySize) {
			result = VaccineStatus.FAMILY_FULL;
		} else {
			result = VaccineStatus.FAMILY_PARTIAL;
		}
		return result;
	}

	private VaccineStatus getIndividualStatus(PersonId personId, VaccinationDataManager vaccinationDataManager) {
		VaccineStatus result;
		if (vaccinationDataManager.isPersonVaccinated(personId)) {
			result = VaccineStatus.INDIVIDUAL_FULL;
		} else {
			result = VaccineStatus.INDIVIDUAL_NONE;
		}
		return result;
	}
}
