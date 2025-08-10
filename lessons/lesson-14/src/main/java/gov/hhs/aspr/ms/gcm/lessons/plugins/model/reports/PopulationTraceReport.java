package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.events.PersonImminentRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;

public final class PopulationTraceReport {
	private final ReportLabel reportLabel;

	private static enum Action {
		ADDITION, REMOVAL
	}

	private ReportContext reportContext;

	private ReportHeader reportHeader;

	public PopulationTraceReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;

		reportHeader = ReportHeader.builder()//
			.add("time")//
			.add("personId")//
			.add("action")//
			.setReportLabel(reportLabel)
			.build();
	}

	public void init(ReportContext reportContext) {
		this.reportContext = reportContext;
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);

		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		reportContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);

		reportContext.releaseOutput(reportHeader);

		for (PersonId personId : peopleDataManager.getPeople()) {
			generateReportItem(Action.ADDITION, personId);
		}
	}

	private void handlePersonImminentRemovalEvent(ReportContext reportContext,
			PersonImminentRemovalEvent personImminentRemovalEvent) {
		generateReportItem(Action.REMOVAL, personImminentRemovalEvent.personId());
	}

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		generateReportItem(Action.ADDITION, personAdditionEvent.personId());
	}

	private void generateReportItem(Action action, PersonId personId) {
		ReportItem reportItem = ReportItem.builder()//
				.setReportLabel(reportLabel)//
				.addValue(reportContext.getTime())//
				.addValue(personId)//
				.addValue(action)//
				.build();
		reportContext.releaseOutput(reportItem);
	}

}
