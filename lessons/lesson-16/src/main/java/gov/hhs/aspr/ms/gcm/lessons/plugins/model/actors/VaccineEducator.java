package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;

public class VaccineEducator {

	private PersonPropertiesDataManager personPropertiesDataManager;
	private GlobalPropertiesDataManager globalPropertiesDataManager;
	private double educationAttemptInterval;
	private double educationSuccessRate;
	private RandomGenerator randomGenerator;
	private ActorContext actorContext;

	/* start code_ref= person_properties_vaccine_educator_educate_person|code_cap= After updating the number of educational attempts for a person, the vaccine educator succeeds in educating the person to stop refusing vaccination based on the global property, EDUCATION_SUCCESS_RATE.  */
	private void educatePerson(PersonId personId) {
		int educationAttempts = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.EDUCATION_ATTEMPTS);
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.EDUCATION_ATTEMPTS,
				educationAttempts + 1);

		if (randomGenerator.nextDouble() < educationSuccessRate) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE, false);
		} else {
			planEducation(personId);
		}
	}
	/* end */

	/* start code_ref= person_properties_vaccine_educator_handle_new_person|code_cap= Attempts to educate a person on vaccination are scheduled at random times in the future based on the global property, EDUCATION_ATTEMPT_INTERVAL. */
	private void planEducation(PersonId personId) {
		double planTime = actorContext.getTime() + randomGenerator.nextDouble() * educationAttemptInterval;
		Consumer<ActorContext> plan = (c) -> educatePerson(personId);
		actorContext.addPlan(plan, planTime);
	}

	private void handleNewPerson(PersonId personId) {
		boolean vaccinated = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINATED);
		if (!vaccinated) {
			Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.REFUSES_VACCINE);
			if (refusesVaccine) {
				planEducation(personId);
			}
		}
	}
	/* end */

	/* start code_ref= person_properties_vaccine_educator_init|code_cap=The vaccine educator initializes by planning the education for each person who refuses vaccination.*/
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
			Boolean refusesVaccine = personPropertiesDataManager.getPersonPropertyValue(personId,
					PersonProperty.REFUSES_VACCINE);
			if (refusesVaccine) {
				planEducation(personId);
			}
		}

		actorContext.subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), (c, e) -> {
			handleNewPerson(e.personId());
		});
	}
	/* end */
}
