package lesson.plugins.model.reports;

import java.util.List;

import lesson.plugins.model.PersonProperty;
import nucleus.ReportContext;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import util.stats.MutableStat;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class QuestionnaireReport {
	private final ReportLabel reportLabel;

	public QuestionnaireReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);
	}

	private void report(ReportContext reportContext) {
		PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);

		ReportHeader reportHeader = ReportHeader.builder()//
												.add("delivery rate")//
												.add("mean delivery time")//
												.add("stdev delivery time")//
												.build();

		ReportItem.Builder reportItemBuilder = ReportItem.builder();
		List<PersonId> infectedPeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.INFECTED, true);
		

		MutableStat mutableStat = new MutableStat();

		for (PersonId personId : infectedPeople) {
			Boolean receivedQuestionnaire = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.RECEIVED_QUESTIONNAIRE);
			if (receivedQuestionnaire) {
				double questionnaireTime = personPropertiesDataManager.getPersonPropertyTime(personId, PersonProperty.RECEIVED_QUESTIONNAIRE);
				mutableStat.add(questionnaireTime);
			}
		}

		double mean = mutableStat.getMean().orElse(0.0);
		double stdev = mutableStat.getStandardDeviation().orElse(0.0);

		int completionCount = mutableStat.size();
		double deliveryRate = 0;
		if (infectedPeople.size() > 0) {
			deliveryRate = completionCount;
			deliveryRate /= infectedPeople.size();
		}

		reportItemBuilder.setReportHeader(reportHeader);
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(deliveryRate);
		reportItemBuilder.addValue(mean);
		reportItemBuilder.addValue(stdev);

		ReportItem reportItem = reportItemBuilder.build();

		reportContext.releaseOutput(reportItem);

	}
}
