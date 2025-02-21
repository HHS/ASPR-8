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
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ConsumerActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.Well;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;

public class EventVaccinator {

	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double interVaccinationTime;
	private ActorContext actorContext;
	private Well randomGenerator;
	private double personInterVaccinationDelay;
	private ActorPlan futurePlan;
	private Map<MultiKey, Double> weights = new LinkedHashMap<>();
	private Map<MultiKey, List<PersonId>> candidates = new LinkedHashMap<>();
	private Map<PersonId, MultiKey> groupMap = new LinkedHashMap<>();

	/*
	 * start code_ref=partitions_plugin_event_init|code_cap=The event-based
	 * vaccinator improves on the inspection-based vaccinator by maintaining the
	 * eligible sub-populations.
	 */
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		establishWorkingVariables();
		subscribeToPersonPropertyUpdateEvents();
		initializeCandidatesAndWeights();
		planNextVaccination();
	}

	/* end */
	private void establishWorkingVariables() {
		int vaccinationsPerDay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINATIONS_PER_DAY);
		interVaccinationTime = 1.0 / vaccinationsPerDay;

		personInterVaccinationDelay = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.INTER_VACCINATION_DELAY_TIME);
	}

	private void subscribeToPersonPropertyUpdateEvents() {
		EventFilter<PersonPropertyUpdateEvent> diseaseEventFilter = personPropertiesDataManager
				.getEventFilterForPersonPropertyUpdateEvent();
		actorContext.subscribe(diseaseEventFilter, this::handlePersonPropertyChange);
	}

	/*
	 * start code_ref=partitions_plugin_event_handle_property_update|code_cap=The
	 * event-based vaccinator processes each person property update event by first
	 * removing the person from the sub-populations and then adding them back in if
	 * required.
	 */
	private void handlePersonPropertyChange(ActorContext actorContext,
			PersonPropertyUpdateEvent personPropertyUpdateEvent) {

		PersonId personId = personPropertyUpdateEvent.personId();

		// remove the person if they are being tracked
		MultiKey multiKey = groupMap.remove(personId);
		List<PersonId> list = candidates.get(multiKey);
		if (list != null) {
			list.remove(personId);
		}

		DiseaseState diseaseState = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.DISEASE_STATE);

		// the person must be susceptible
		if (diseaseState != DiseaseState.SUSCEPTIBLE) {
			return;
		}

		Integer vaccinationCount = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.VACCINATION_COUNT);

		if (vaccinationCount > 2) {
			return;
		}

		int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
		AgeGroup ageGroup = AgeGroup.getAgeGroup(age);
		if (ageGroup == AgeGroup.CHILD) {
			return;
		}
		boolean waitingForNextDose = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.WAITING_FOR_NEXT_DOSE);
		if (waitingForNextDose) {
			return;
		}

		multiKey = new MultiKey(ageGroup, vaccinationCount);

		list = candidates.get(multiKey);
		if (list != null) {
			list.add(personId);
			groupMap.put(personId, multiKey);
		}

	}
	/* end */

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

	private void initializeCandidatesAndWeights() {

		List<PersonId> people = peopleDataManager.getPeople();

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
			AgeGroup ageGroup = AgeGroup.getAgeGroup(age);
			MultiKey multiKey = new MultiKey(ageGroup, vaccinationCount);
			groupMap.put(personId, multiKey);
			candidates.get(multiKey).add(personId);
		}
	}

	/*
	 * start code_ref=partitions_plugin_event_vaccinate|code_cap= The event-based
	 * vaccinator selects from maintained sub-populations.
	 */
	private void planNextVaccination() {
		futurePlan = new ConsumerActorPlan(interVaccinationTime + actorContext.getTime(), this::vaccinatePerson);
		actorContext.addPlan(futurePlan);
	}

	private void vaccinatePerson(ActorContext actorContext) {

		Map<MultiKey, Double> extendedWeights = new LinkedHashMap<>();

		double sumOfExtendedWeights = 0;
		for (MultiKey multiKey : weights.keySet()) {
			Double weight = weights.get(multiKey);
			int candidateCount = candidates.get(multiKey).size();
			Double extendedWeight = weight * candidateCount;
			extendedWeights.put(multiKey, extendedWeight);
			sumOfExtendedWeights += extendedWeight;
		}

		PersonId selectedCandidate = null;

		double selectedWeight = sumOfExtendedWeights * randomGenerator.nextDouble();
		for (MultiKey multiKey : extendedWeights.keySet()) {
			Double extendedWeight = extendedWeights.get(multiKey);
			selectedWeight -= extendedWeight;
			if (selectedWeight <= 0) {
				List<PersonId> selectedCandidates = candidates.get(multiKey);
				if (!selectedCandidates.isEmpty()) {
					int index = randomGenerator.nextInt(selectedCandidates.size());
					selectedCandidate = selectedCandidates.get(index);
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
		}

	}
	/* end */

	private void planWaitTermination(PersonId personId) {
		actorContext.addPlan((c) -> this.endWaitTime(personId), personInterVaccinationDelay + actorContext.getTime());
	}

	/*
	 * start code_ref=partitions_plugin_event_end_wait|code_cap= The
	 * event-vaccinator can restart the vaccination process when a person becomes
	 * eligible after the post-vaccination waiting period is over.
	 */
	private void endWaitTime(PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.WAITING_FOR_NEXT_DOSE, false);
		if (futurePlan == null) {
			vaccinatePerson(actorContext);
		}
	}
	/* end */
}
