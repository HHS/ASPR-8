package lesson.plugins.model.actors;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.datamanagers.StochasticsDataManager;

public class VaccineEducator {

	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double educationAttemptInterval;
	private double educationSuccessRate;
	private RandomGenerator randomGenerator;
	private ActorContext actorContext;

	private void educatePerson(PersonId personId) {
		int educationAttempts = personPropertiesDataManager
				.getPersonPropertyValue(personId, PersonProperty.EDUCATION_ATTEMPTS);
		personPropertiesDataManager
		.setPersonPropertyValue(personId, PersonProperty.EDUCATION_ATTEMPTS, educationAttempts + 1);

		if (randomGenerator.nextDouble() < educationSuccessRate) {
			personPropertiesDataManager
			.setPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE, false);
		} else {
			planEducation(personId);			
		}
	}

	private void planEducation(PersonId personId) {
		double planTime = actorContext.getTime() + randomGenerator.nextDouble() * educationAttemptInterval;		
		Consumer<ActorContext> plan = (c) -> educatePerson(personId);
		actorContext.addPlan(plan, planTime);
	}

	private void handleNewPerson(PersonId personId) {
		boolean vaccinated = personPropertiesDataManager
				.getPersonPropertyValue(personId, PersonProperty.VACCINATED);
		if (!vaccinated) {
			Boolean refusesVaccine = personPropertiesDataManager
					.getPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE);
			if (refusesVaccine) {
				planEducation(personId);
			}
		}
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		educationAttemptInterval = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL);
		educationSuccessRate = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.EDUCATION_SUCCESS_RATE);

		List<PersonId> unvaccinatedPeople = personPropertiesDataManager
				.getPeopleWithPropertyValue(PersonProperty.VACCINATED, false);
		for (PersonId personId : unvaccinatedPeople) {
			Boolean refusesVaccine = personPropertiesDataManager
					.getPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE);
			if (refusesVaccine) {
				planEducation(personId);				
			}
		}

		actorContext.subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), (c, e) -> {
			handleNewPerson(e.personId());
		});
	}

}
