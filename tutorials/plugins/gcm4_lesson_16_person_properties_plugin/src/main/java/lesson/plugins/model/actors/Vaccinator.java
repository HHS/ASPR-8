package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import nucleus.EventFilter;
import nucleus.Plan;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.stochastics.StochasticsDataManager;

public final class Vaccinator {

	private PeopleDataManager peopleDataManager;
	private RandomGenerator randomGenerator;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double vaccineAttemptInterval;
	private ActorContext actorContext;

	private void vaccinatePerson(PersonId personId) {
		int vaccineAttempts = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINE_ATTEMPTS);
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINE_ATTEMPTS, vaccineAttempts + 1);

		boolean isImmune = false;
		if (personPropertiesDataManager.personPropertyIdExists(PersonProperty.IS_IMMUNE)) {
			isImmune = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.IS_IMMUNE);
		}

		Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE);
		if (!isImmune) {
			if (refusesVaccine) {
				double planTime = actorContext.getTime() + randomGenerator.nextDouble() * vaccineAttemptInterval;
				Object planKey = personId;
				
				Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
						.setCallbackConsumer((c) -> vaccinatePerson(personId))//
						.setKey(planKey)//
						.setTime(planTime)//
						.build();//
				
				actorContext.addPlan(plan);
			} else {
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATED, true);
			}
		}
	}

	private void handleVaccineAcceptance(ActorContext actorContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		/*
		 * We know that the person property is PersonProperty.REFUSES_VACCINE
		 * since we used an event filter when subscribing
		 */
		Boolean refusesVaccine = personPropertyUpdateEvent.getCurrentPropertyValue();
		if (!refusesVaccine) {
			PersonId personId = personPropertyUpdateEvent.personId();
			// drop the current plan
			actorContext.removePlan(personId);
			vaccinatePerson(personId);
		}
	}

	private void planVaccination(PersonId personId) {
		double planTime = actorContext.getTime() + randomGenerator.nextDouble() * vaccineAttemptInterval;
		Object planKey = personId;
		Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
										.setCallbackConsumer((c) -> vaccinatePerson(personId))//
										.setKey(planKey)//
										.setTime(planTime)//
										.build();//
		actorContext.addPlan(plan);
	}

	private void handleNewPerson(PersonId personId) {
		boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINATED);
		if (!vaccinated) {
			planVaccination(personId);
		}
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		List<PersonId> unvaccinatedPeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.VACCINATED, false);
		vaccineAttemptInterval = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.VACCINE_ATTEMPT_INTERVAL);
		for (PersonId personId : unvaccinatedPeople) {
			planVaccination(personId);
		}

		EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager//
																						.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.REFUSES_VACCINE);

		actorContext.subscribe(eventFilter, this::handleVaccineAcceptance);

		actorContext.subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), (c, e) -> {
			handleNewPerson(e.personId());
		});

	}

}
