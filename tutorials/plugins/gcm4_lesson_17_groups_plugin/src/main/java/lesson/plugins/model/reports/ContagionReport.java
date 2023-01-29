package lesson.plugins.model.reports;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ReportContext;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import util.wrappers.MutableInteger;

public final class ContagionReport {
	private final ReportHeader reportHeader = ReportHeader.builder().add("infected").add("count").build();
	private final ReportId reportId;

	public ContagionReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);
	}

	private void report(ReportContext reportContext) {
		PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		List<PersonId> people = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.RECOVERED);

		Map<Integer, MutableInteger> countMap = new TreeMap<>();

		int maxInfectedCount = 0;
		for (PersonId personId : people) {
			int infectedCount = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.INFECTED_COUNT);
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
		ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (Integer i : countMap.keySet()) {
			MutableInteger mutableInteger = countMap.get(i);
			reportItemBuilder.setReportId(reportId);
			reportItemBuilder.setReportHeader(reportHeader);
			reportItemBuilder.addValue(i);
			reportItemBuilder.addValue(mutableInteger.getValue());
			ReportItem reportItem = reportItemBuilder.build();
			reportContext.releaseOutput(reportItem);
		}

	}

}
