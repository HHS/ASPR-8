package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.RegionProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;

public final class Vaccinator {

	private RegionsDataManager regionsDataManager;
	private VaccinationDataManager vaccinationDataManager;
	private RandomGenerator randomGenerator;

	/* start code_ref= regions_plugin_vaccinator_alter_priority_property|code_cap= Toggling the vaccine priority for a randomly selected region.*/
	private void alterVaccinePriorityPropertyOnRandomRegion(ActorContext actorContext) {
		List<RegionId> regionids = new ArrayList<>(regionsDataManager.getRegionIds());
		if (regionids.isEmpty()) {
			return;
		}
		RegionId regionId = regionids.get(randomGenerator.nextInt(regionids.size()));
		Boolean vaccinePriority = regionsDataManager.getRegionPropertyValue(regionId, RegionProperty.VACCINE_PRIORITY);
		regionsDataManager.setRegionPropertyValue(regionId, RegionProperty.VACCINE_PRIORITY, !vaccinePriority);
	}
	/* end */

	/* start code_ref= regions_plugin_vaccinator_priority_property|code_cap= On day 50, the vaccinator defines the Boolean VACCINE PRIORITY property and assigns randomized values to the existing regions. It then plans for updates to 50 regional vaccine priority property values over 50 days.*/
	private void addVaccinePriorityPropertyToRegions(ActorContext actorContext) {

		PropertyDefinition propertyDefinition = //
				PropertyDefinition.builder()//
						.setType(Boolean.class)//
						.setDefaultValue(false)//
						.build();

		RegionPropertyDefinitionInitialization.Builder defBuilder = RegionPropertyDefinitionInitialization.builder()//
				.setPropertyDefinition(propertyDefinition)//
				.setRegionPropertyId(RegionProperty.VACCINE_PRIORITY);

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			defBuilder.addPropertyValue(regionId, randomGenerator.nextBoolean());
		}

		RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = defBuilder.build();
		regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

		for (int i = 0; i < 50; i++) {
			double planTime = actorContext.getTime() + i;
			actorContext.addPlan(this::alterVaccinePriorityPropertyOnRandomRegion, planTime);
		}
	}

	/* end */
	
	/* start code_ref= regions_plugin_vaccinator_vaccinate_random_person|code_cap= The vaccinator selects a person at random from the population to vaccinate. If the region is using the VACCINE_PRIORITY policy, then those with the least number of vaccinations have preference.*/
	private void vaccinateRandomPerson(ActorContext actorContext) {

		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
		if (regionIds.isEmpty()) {
			return;
		}
		RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
		List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);

		Boolean prioritizePeople = false;
		boolean vaccinePriorityPropertyExists = regionsDataManager
				.regionPropertyIdExists(RegionProperty.VACCINE_PRIORITY);
		if (vaccinePriorityPropertyExists) {
			prioritizePeople = regionsDataManager.getRegionPropertyValue(regionId, RegionProperty.VACCINE_PRIORITY);
		}

		PersonId selectedPersonId = null;
		if (prioritizePeople) {
			int minVaccinationCount = Integer.MAX_VALUE;
			for (PersonId personId : peopleInRegion) {
				int personVaccinationCount = vaccinationDataManager.getPersonVaccinationCount(personId);
				if (personVaccinationCount < minVaccinationCount) {
					minVaccinationCount = personVaccinationCount;
				}
			}
			List<PersonId> eligiblePeople = new ArrayList<>();
			for (PersonId personId : peopleInRegion) {
				int personVaccinationCount = vaccinationDataManager.getPersonVaccinationCount(personId);
				if (personVaccinationCount == minVaccinationCount) {
					eligiblePeople.add(personId);
				}
			}
			if (!eligiblePeople.isEmpty()) {
				selectedPersonId = eligiblePeople.get(randomGenerator.nextInt(eligiblePeople.size()));
			}
		} else {
			if (!peopleInRegion.isEmpty()) {
				selectedPersonId = peopleInRegion.get(randomGenerator.nextInt(peopleInRegion.size()));
			}
		}

		if (selectedPersonId != null) {
			vaccinationDataManager.vaccinatePerson(selectedPersonId);
		}
	}
	/* end */

	/* start code_ref= regions_plugin_vaccinator_init|code_cap=The vaccinator initializes by planning the vaccination of 5000 people carried out over approximately 50 days. */
	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);

		double planTime = randomGenerator.nextDouble();
		for (int i = 0; i < 5000; i++) {
			actorContext.addPlan(this::vaccinateRandomPerson, planTime);
			planTime += randomGenerator.nextDouble() * 0.02;
		}

		actorContext.addPlan(this::addVaccinePriorityPropertyToRegions, 50);
	}
	/* end */
}
