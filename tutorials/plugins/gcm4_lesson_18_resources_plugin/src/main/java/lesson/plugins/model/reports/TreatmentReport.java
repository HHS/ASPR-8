package lesson.plugins.model.actors.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import util.wrappers.MultiKey;
import util.wrappers.MutableInteger;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class TreatmentReport {
	private final ReportId reportId;

	public TreatmentReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ActorContext actorContext) {
		actorContext.subscribeToSimulationClose(this::report);
	}

	private void report(ActorContext actorContext) {
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		/*
		 * Build a map from a multikey to a counter. Each person's ordered
		 * person property values will form the multikey. The counter is
		 * incremented for each person matching the unique multikeys.
		 */
		Map<MultiKey, MutableInteger> map = new LinkedHashMap<>();
		for (PersonId personId : peopleDataManager.getPeople()) {

			Boolean immune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IMMUNE);
			Boolean infected = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.INFECTED);
			Boolean treatedWithAntiviral = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.TREATED_WITH_ANTIVIRAL);
			Boolean hospitalized = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.HOSPITALIZED);
			Boolean deadInHospital = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOSPITAL);
			Boolean deadInHome = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME);

			MultiKey multiKey = new MultiKey(immune, infected, treatedWithAntiviral, hospitalized, deadInHospital, deadInHome);
			MutableInteger mutableInteger = map.get(multiKey);
			if (mutableInteger == null) {
				mutableInteger = new MutableInteger();
				map.put(multiKey, mutableInteger);
			}
			mutableInteger.increment();
		}

		/*
		 * Build the header of the report using the headers that correspond to
		 * the ordered key values in the multikeys.
		 */
		ReportHeader reportHeader = ReportHeader.builder()//
												.add("immune")//
												.add("infected")//
												.add("treated_with_antiviral")//
												.add("hospitalized")//
												.add("dead_in_hospital")//
												.add("dead_in_home")//
												.add("people")//
												.build();

		ReportItem.Builder reportItemBuilder = ReportItem.builder();

		/*
		 * Form a report item for each multikey, taking the ordered property
		 * values from the multikey and using them as inputs to the report item
		 */
		for (MultiKey multiKey : map.keySet()) {

			int personCount = map.get(multiKey).getValue();
			boolean immune = multiKey.getKey(0);
			boolean infected = multiKey.getKey(1);
			boolean treatedWithAntiviral = multiKey.getKey(2);
			boolean hospitalized = multiKey.getKey(3);
			boolean deadInHospital = multiKey.getKey(4);
			boolean deadInHome = multiKey.getKey(5);

			reportItemBuilder.setReportHeader(reportHeader);
			reportItemBuilder.setReportId(reportId);
			reportItemBuilder.addValue(immune);
			reportItemBuilder.addValue(infected);
			reportItemBuilder.addValue(treatedWithAntiviral);
			reportItemBuilder.addValue(hospitalized);
			reportItemBuilder.addValue(deadInHospital);
			reportItemBuilder.addValue(deadInHome);
			reportItemBuilder.addValue(personCount);
			ReportItem reportItem = reportItemBuilder.build();
			/*
			 * Release the report item from the simulation
			 */
			actorContext.releaseOutput(reportItem);
		}

	}
}
