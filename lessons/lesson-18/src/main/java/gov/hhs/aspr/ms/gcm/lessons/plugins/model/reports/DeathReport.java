package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import java.util.List;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;

/**
 * A report that groups people at the end of the simulation by their shared
 * person property values.
 * 
 *
 */
public final class DeathReport {
	private final ReportLabel reportLabel;

	public DeathReport(ReportLabel reportLabel) {
		this.reportLabel = reportLabel;
	}

	public void init(ReportContext reportContext) {
		reportContext.subscribeToSimulationClose(this::report);

		ReportHeader reportHeader = ReportHeader.builder()//
				.setReportLabel(reportLabel)//
				.add("region")//
				.add("pop_size")//
				.add("deaths")//
				.add("deaths_in_home")//
				.add("deaths_in_hospital")//
				.add("per_capita_deaths")//
				.add("per_capita_deaths_in_home")//
				.add("per_capita_deaths_in_hospital")//
				.build();

		reportContext.releaseOutput(reportHeader);
	}

	private void report(ReportContext reportContext) {
		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		PersonPropertiesDataManager personPropertiesDataManager = reportContext
				.getDataManager(PersonPropertiesDataManager.class);

		

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
			int popSize = peopleInRegion.size();
			int homeDeathCount = 0;
			int hospitalDeathCount = 0;

			for (PersonId personId : peopleInRegion) {
				Boolean deadInHospital = personPropertiesDataManager.getPersonPropertyValue(personId,
						PersonProperty.DEAD_IN_HOSPITAL);
				if (deadInHospital) {
					hospitalDeathCount++;
				}
				Boolean deadInHome = personPropertiesDataManager.getPersonPropertyValue(personId,
						PersonProperty.DEAD_IN_HOME);
				if (deadInHome) {
					homeDeathCount++;
				}
			}
			int deathCount = homeDeathCount + hospitalDeathCount;
			double perCapitaDeaths = 0;
			double perCapitaHomeDeaths = 0;
			double perCapitaHospitalDeaths = 0;
			if (peopleInRegion.size() > 0) {
				perCapitaDeaths = ((double) deathCount) / popSize;
				perCapitaHomeDeaths = ((double) homeDeathCount) / popSize;
				perCapitaHospitalDeaths = ((double) hospitalDeathCount) / popSize;

			}
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			reportItemBuilder.setReportLabel(reportLabel);
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
