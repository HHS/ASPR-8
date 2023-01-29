package lesson.plugins.model.reports;

import java.util.List;

import lesson.plugins.model.PersonProperty;
import nucleus.ReportContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

public final class VaccineReport {

	private final ReportId reportId;

	public VaccineReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);
	}

	private ReportHeader reportHeader = ReportHeader.builder()//
													.add("vaccinated_immune")//
													.add("vaccinated_susceptible")//
													.add("unvaccinated_immune")//
													.add("unvaccinated_susceptible")//
													.build();

	private void report(ReportContext reportContext) {
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);

		int vaccinated_immune = 0;
		int vaccinated_susceptible = 0;
		int unvaccinated_immune = 0;
		int unvaccinated_susceptible = 0;

		List<PersonId> people = peopleDataManager.getPeople();
		for (PersonId personId : people) {
			boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINATED);
			boolean immune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IS_IMMUNE);
			if (vaccinated) {
				if (immune) {
					vaccinated_immune++;
				} else {
					vaccinated_susceptible++;
				}
			} else {
				if (immune) {
					unvaccinated_immune++;
				} else {
					unvaccinated_susceptible++;
				}
			}
		}

		ReportItem.Builder builder = ReportItem	.builder()//
												.setReportId(reportId)//
												.setReportHeader(reportHeader);

		builder.addValue(vaccinated_immune);
		builder.addValue(vaccinated_susceptible);
		builder.addValue(unvaccinated_immune);
		builder.addValue(unvaccinated_susceptible);

		ReportItem reportItem = builder.build();
		reportContext.releaseOutput(reportItem);
	}

}
