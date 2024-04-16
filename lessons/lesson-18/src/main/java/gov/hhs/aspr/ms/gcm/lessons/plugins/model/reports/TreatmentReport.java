package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class TreatmentReport {
	private final ReportLabel reportLabel;

	public TreatmentReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);
	}

	private void report(ReportContext reportContext) {
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = reportContext
				.getDataManager(PersonPropertiesDataManager.class);
		/*
		 * Build a map from a multikey to a counter. Each person's ordered person
		 * property values will form the multikey. The counter is incremented for each
		 * person matching the unique multikeys.
		 */
		Map<MultiKey, MutableInteger> map = new LinkedHashMap<>();
		for (PersonId personId : peopleDataManager.getPeople()) {

			Boolean immune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IMMUNE);
			Boolean infected = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.INFECTED);
			Boolean treatedWithAntiviral = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.TREATED_WITH_ANTIVIRAL);
			Boolean hospitalized = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.HOSPITALIZED);
			Boolean deadInHospital = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.DEAD_IN_HOSPITAL);
			Boolean deadInHome = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.DEAD_IN_HOME);

			MultiKey multiKey = new MultiKey(immune, infected, treatedWithAntiviral, hospitalized, deadInHospital,
					deadInHome);
			MutableInteger mutableInteger = map.get(multiKey);
			if (mutableInteger == null) {
				mutableInteger = new MutableInteger();
				map.put(multiKey, mutableInteger);
			}
			mutableInteger.increment();
		}

		/*
		 * Build the header of the report using the headers that correspond to the
		 * ordered key values in the multikeys.
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

		/*
		 * Form a report item for each multikey, taking the ordered property values from
		 * the multikey and using them as inputs to the report item
		 */
		for (MultiKey multiKey : map.keySet()) {
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			int personCount = map.get(multiKey).getValue();
			boolean immune = multiKey.getKey(0);
			boolean infected = multiKey.getKey(1);
			boolean treatedWithAntiviral = multiKey.getKey(2);
			boolean hospitalized = multiKey.getKey(3);
			boolean deadInHospital = multiKey.getKey(4);
			boolean deadInHome = multiKey.getKey(5);

			reportItemBuilder.setReportHeader(reportHeader);
			reportItemBuilder.setReportLabel(reportLabel);
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
			reportContext.releaseOutput(reportItem);
		}

	}
}
