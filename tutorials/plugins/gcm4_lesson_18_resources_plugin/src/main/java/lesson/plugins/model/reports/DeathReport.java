package lesson.plugins.model.reports;

import java.util.List;

import lesson.plugins.model.PersonProperty;
import nucleus.ReportContext;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class DeathReport {
	private final ReportId reportId;

	public DeathReport(ReportId reportId) {
		this.reportId = reportId;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);
	}

	private void report(ReportContext reportContext) {
		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		
		ReportHeader reportHeader = ReportHeader.builder()//
												.add("region")//
												.add("pop_size")//
												.add("deaths")//
												.add("deaths_in_home")//
												.add("deaths_in_hospital")//
												.add("per_capita_deaths")//
												.add("per_capita_deaths_in_home")//
												.add("per_capita_deaths_in_hospital")//												
												.build();
		ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
			int popSize = peopleInRegion.size();
			int homeDeathCount = 0;
			int hospitalDeathCount = 0;
			
			for (PersonId personId : peopleInRegion) {
				Boolean deadInHospital = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOSPITAL);
				if (deadInHospital) {
					hospitalDeathCount++;
				}
				Boolean deadInHome = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME);
				if (deadInHome) {
					homeDeathCount++;
				}
			}
			int deathCount = homeDeathCount + hospitalDeathCount;
			double perCapitaDeaths = 0;
			double perCapitaHomeDeaths = 0;
			double perCapitaHospitalDeaths = 0;
			if (peopleInRegion.size() > 0) {
				perCapitaDeaths = ((double)deathCount)/popSize;
				perCapitaHomeDeaths = ((double)homeDeathCount)/popSize;
				perCapitaHospitalDeaths = ((double)hospitalDeathCount)/popSize;

			}
			reportItemBuilder.setReportHeader(reportHeader);
			reportItemBuilder.setReportId(reportId);
			reportItemBuilder.addValue(regionId);
			reportItemBuilder.addValue(peopleInRegion.size());
			reportItemBuilder.addValue(deathCount);
			reportItemBuilder.addValue(homeDeathCount);
			reportItemBuilder.addValue(hospitalDeathCount);
			reportItemBuilder.addValue(perCapitaDeaths);
			reportItemBuilder.addValue(perCapitaHomeDeaths);
			reportItemBuilder.addValue(perCapitaHospitalDeaths);


			ReportItem reportItem = reportItemBuilder.build();
			/*
			 * Release the report item from the simulation
			 */
			reportContext.releaseOutput(reportItem);

		}

	}
}
