package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.AgeGroup;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.Well;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;

public class InspectionVaccinator {

	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double interVaccinationTime;
	private ActorContext actorContext;
	private Well randomGenerator;
	private double personInterVaccinationDelay;
	private boolean potentialEligiblePeopleExist;

	/*start code_ref=partitions_plugin_inspection_init|code_cap=The inspection-based vaccinator establishes its working variables and begins planning the next vaccination.*/
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		establishWorkingVaribles();
		planNextVaccination();
	}
	/* end */

	private void establishWorkingVaribles() {
		int vaccinationsPerDay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINATIONS_PER_DAY);
		personInterVaccinationDelay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.INTER_VACCINATION_DELAY_TIME);
		interVaccinationTime = 1.0 / vaccinationsPerDay;
	}

	/*start code_ref=partitions_plugin_inspection_weighing|code_cap=The inspection-based vaccinator assigns a probability weight to each person based on their age and number of vaccine doses administered.*/
	private double getWeight(AgeGroup ageGroup, int vaccineCount) {

		double result = 1;

		switch (ageGroup) {
		case ADULT_18_44:
			result += 0;
			break;
		case ADULT_45_64:
			result += 3;
			break;
		case CHILD:
			result += 0;
			break;
		case SENIOR:
			result += 10;
			break;
		default:
			break;
		}

		switch (vaccineCount) {
		case 0:
			result += 4;
			break;
		case 1:
			result += 3;
			break;
		case 2:
			result += 2;
			break;
		default:
			result += 0;
			break;
		}

		return result;
	}
	/* end */

	private void planWaitTermination(PersonId personId) {
		actorContext.addPlan((c) -> this.endWaitTime(personId), personInterVaccinationDelay + actorContext.getTime());
	}

	private void endWaitTime(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.WAITING_FOR_NEXT_DOSE, false);
	}

	
	/*start code_ref=partitions_plugin_inspection_vaccination|code_cap=The inspection-based vaccinator vaccinates 100 people per day. Each vaccination attempt considers every person in the simulation.*/
	private void planNextVaccination() {
		actorContext.addPlan(this::vaccinatePerson, interVaccinationTime + actorContext.getTime());
	}
	

	private void vaccinatePerson(ActorContext actorContext) {
		List<PersonId> people = peopleDataManager.getPeople();
		Map<MultiKey, List<PersonId>> candidates = new LinkedHashMap<>();
		Map<MultiKey, Double> weights = new LinkedHashMap<>();

		List<AgeGroup> eligibleAgeGroups = new ArrayList<>();
		eligibleAgeGroups.add(AgeGroup.ADULT_18_44);
		eligibleAgeGroups.add(AgeGroup.ADULT_45_64);
		eligibleAgeGroups.add(AgeGroup.SENIOR);

		List<Integer> eligibleVaccineCounts = new ArrayList<>();
		eligibleVaccineCounts.add(0);
		eligibleVaccineCounts.add(1);
		eligibleVaccineCounts.add(2);

		for (AgeGroup ageGroup : eligibleAgeGroups) {
			for (Integer vaccineCount : eligibleVaccineCounts) {
				MultiKey multiKey = new MultiKey(ageGroup, vaccineCount);
				double weight = getWeight(ageGroup, vaccineCount);
				weights.put(multiKey, weight);
				candidates.put(multiKey, new ArrayList<>());
			}
		}

		potentialEligiblePeopleExist = false;

		for (PersonId personId : people) {
			int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			if (age < 18) {
				continue;
			}
			DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.DISEASE_STATE);
			if (diseaseState != DiseaseState.SUSCEPTIBLE) {
				continue;
			}
			int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.VACCINATION_COUNT);
			if (vaccinationCount > 2) {
				continue;
			}

			boolean waitingFromPreviousVaccination = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.WAITING_FOR_NEXT_DOSE);

			if (waitingFromPreviousVaccination) {
				potentialEligiblePeopleExist = true;
				continue;
			}

			AgeGroup ageGroup = AgeGroup.getAgeGroup(age);
			MultiKey multiKey = new MultiKey(ageGroup, vaccinationCount);
			candidates.get(multiKey).add(personId);
		}

		Map<MultiKey, Double> extendedWeights = new LinkedHashMap<>();

		double sumOfExtendedWeights = 0;
		for (MultiKey multiKey : weights.keySet()) {
			Double weight = weights.get(multiKey);
			int candidateCount = candidates.get(multiKey).size();
			Double extenedWeight = weight * candidateCount;
			extendedWeights.put(multiKey, extenedWeight);
			sumOfExtendedWeights += extenedWeight;
		}

		PersonId selectedCandidate = null;

		double selectedWeight = sumOfExtendedWeights * randomGenerator.nextDouble();
		for (MultiKey multiKey : extendedWeights.keySet()) {
			Double extendedWeight = extendedWeights.get(multiKey);
			selectedWeight -= extendedWeight;
			if (selectedWeight <= 0) {
				List<PersonId> seletedCandidates = candidates.get(multiKey);
				if (!seletedCandidates.isEmpty()) {
					int index = randomGenerator.nextInt(seletedCandidates.size());
					selectedCandidate = seletedCandidates.get(index);
				}
				break;
			}
		}

		if (selectedCandidate != null) {

			int vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(selectedCandidate,
					PersonProperty.VACCINATION_COUNT);
			vaccinationCount++;
			personPropertiesDataManager.setPersonPropertyValue(selectedCandidate, PersonProperty.VACCINATION_COUNT,
					vaccinationCount);
			if (vaccinationCount < 3) {
				personPropertiesDataManager.setPersonPropertyValue(selectedCandidate,
						PersonProperty.WAITING_FOR_NEXT_DOSE, true);
				planWaitTermination(selectedCandidate);
			}
			planNextVaccination();
		} else {
			if (potentialEligiblePeopleExist) {
				planNextVaccination();
			}
		}
	}
	/* end */
	
}
