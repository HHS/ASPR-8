package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.AgeGroup;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import util.wrappers.MultiKey;
import util.wrappers.MutableInteger;

public class PersonStatusReport {
	private final ReportLabel reportLabel;
	private ReportHeader reportHeader;

	public PersonStatusReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;

	public void init(ReportContext reportContext) {
		personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		reportContext.subscribeToSimulationClose(this::reportState);

		final ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		reportHeaderBuilder.add("age_group");
		reportHeaderBuilder.add("disease_state");
		reportHeaderBuilder.add("vaccinations");
		reportHeaderBuilder.add("people");

		reportHeader = reportHeaderBuilder.build();

	}

	private void reportState(ReportContext reportContext) {
		List<PersonId> people = peopleDataManager.getPeople();

		Map<MultiKey, MutableInteger> map = new LinkedHashMap<>();

		for (PersonId personId : people) {
			int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			AgeGroup ageGroup = AgeGroup.getAgeGroup(age);
			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.DISEASE_STATE);
			int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.VACCINATION_COUNT);

			MultiKey multiKey = new MultiKey(ageGroup, diseaseState, vaccinationCount);
			MutableInteger counter = map.get(multiKey);
			if (counter == null) {
				counter = new MutableInteger();
				map.put(multiKey, counter);
			}
			counter.increment();
		}

		for (MultiKey multiKey : map.keySet()) {
			int personCount = map.get(multiKey).getValue();
			AgeGroup ageGroup = multiKey.getKey(0);
			DiseaseState diseaseState = multiKey.getKey(1);
			int vaccinationCount = multiKey.getKey(2);
			ReportItem reportItem = ReportItem.builder().setReportHeader(reportHeader).setReportLabel(reportLabel)
					
					.addValue(ageGroup)//
					.addValue(diseaseState)//
					.addValue(vaccinationCount)//
					.addValue(personCount)//
					.build();//

			reportContext.releaseOutput(reportItem);
		}

	}
}
