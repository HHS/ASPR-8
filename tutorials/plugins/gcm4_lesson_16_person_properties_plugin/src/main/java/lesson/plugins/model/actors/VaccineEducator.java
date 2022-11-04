package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.PersonProperty;
import lesson.plugins.vaccine.VaccinationDataManager;
import nucleus.ActorContext;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class VaccineEducator {

	private PersonPropertiesDataManager personPropertiesDataManager;
	private RandomGenerator randomGenerator;
	private ActorContext actorContext;

	private void educatePerson(PersonId personId) {
		int educationAttempts = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.EDUCATION_ATTEMPTS);
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.EDUCATION_ATTEMPTS,educationAttempts+1);

		if (randomGenerator.nextDouble() < 0.25) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE, false);
		} else {
			actorContext.addPlan((c) -> educatePerson(personId), actorContext.getTime() + randomGenerator.nextDouble() * 60);
		}
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		VaccinationDataManager vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		
		List<PersonId> unvaccinatedPeople = vaccinationDataManager.getUnvaccinatedPeople();
		for (PersonId personId : unvaccinatedPeople) {
			Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE);
			if (refusesVaccine) {
				actorContext.addPlan((c) -> educatePerson(personId), actorContext.getTime() + randomGenerator.nextDouble() * 60);
			}
		}
	}

}
