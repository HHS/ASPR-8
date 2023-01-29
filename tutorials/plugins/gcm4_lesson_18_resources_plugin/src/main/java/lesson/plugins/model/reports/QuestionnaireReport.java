package lesson.plugins.model.actors.reports;

import java.util.List;

import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import util.stats.MutableStat;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class QuestionnaireReport {
	private final ReportId reportId;

	public QuestionnaireReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ActorContext actorContext) {
		actorContext.subscribeToSimulationClose(this::report);
	}

	private void report(ActorContext actorContext) {
		PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);

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
		reportItemBuilder.setReportId(reportId);
		reportItemBuilder.addValue(deliveryRate);
		reportItemBuilder.addValue(mean);
		reportItemBuilder.addValue(stdev);

		ReportItem reportItem = reportItemBuilder.build();

		actorContext.releaseOutput(reportItem);

	}
}
