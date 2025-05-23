package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

public final class ContagionReport {
	private final ReportHeader reportHeader = ReportHeader.builder().add("infected").add("count").build();
	private final ReportLabel reportLabel;

	public ContagionReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);

		reportContext.releaseOutput(reportHeader);
	}

	private void report(ReportContext reportContext) {
		PersonPropertiesDataManager personPropertiesDataManager = reportContext
				.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> people = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE,
				DiseaseState.RECOVERED);

		Map<Integer, MutableInteger> countMap = new TreeMap<>();

		int maxInfectedCount = 0;
		for (PersonId personId : people) {
			int infectedCount = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.INFECTED_COUNT);
			maxInfectedCount = FastMath.max(maxInfectedCount, infectedCount);
			MutableInteger mutableInteger = countMap.get(infectedCount);
			if (mutableInteger == null) {
				mutableInteger = new MutableInteger();
				countMap.put(infectedCount, mutableInteger);
			}
			mutableInteger.increment();
		}

		for (int i = 0; i < maxInfectedCount; i++) {
			if (!countMap.containsKey(i)) {
				countMap.put(i, new MutableInteger());
			}
		}

		for (Integer i : countMap.keySet()) {
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			MutableInteger mutableInteger = countMap.get(i);
			reportItemBuilder.setReportLabel(reportLabel);
			reportItemBuilder.addValue(i);
			reportItemBuilder.addValue(mutableInteger.getValue());
			ReportItem reportItem = reportItemBuilder.build();
			reportContext.releaseOutput(reportItem);
		}

	}

}
