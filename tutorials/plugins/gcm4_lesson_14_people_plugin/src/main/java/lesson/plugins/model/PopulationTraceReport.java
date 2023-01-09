package lesson.plugins.model;

import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.PersonId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

public final class PopulationTraceReport {
	private final ReportId reportId;

	private static enum Action {
		ADDITION, REMOVAL
	}

	private ActorContext actorContext;

	private ReportHeader reportHeader = ReportHeader.builder()//
													.add("time")//
													.add("personId")//
													.add("action")//
													.build();

	public PopulationTraceReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		actorContext.subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), this::handlePersonAdditionEvent);
		actorContext.subscribe(peopleDataManager.getEventFilterForPersonImminentRemovalEvent(), this::handlePersonImminentRemovalEvent);

		for(PersonId personId : peopleDataManager.getPeople()) {
			generateReportItem(Action.ADDITION, personId);
		}
		
	}

	private void handlePersonImminentRemovalEvent(ActorContext actorContext, PersonImminentRemovalEvent personImminentRemovalEvent) {
		generateReportItem(Action.REMOVAL, personImminentRemovalEvent.personId());
	}

	private void handlePersonAdditionEvent(ActorContext actorContext, PersonAdditionEvent personAdditionEvent) {
		generateReportItem(Action.ADDITION, personAdditionEvent.personId());
	}

	private void generateReportItem(Action action, PersonId personId) {
		ReportItem reportItem = ReportItem	.builder()//
											.setReportId(reportId)//
											.setReportHeader(reportHeader)//
											.addValue(actorContext.getTime())//
											.addValue(personId)//
											.addValue(action)//
											.build();
		actorContext.releaseOutput(reportItem);
	}

}
