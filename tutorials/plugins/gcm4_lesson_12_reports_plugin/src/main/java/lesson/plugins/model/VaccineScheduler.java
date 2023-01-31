
package lesson.plugins.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.person.datamanagers.PersonDataManager;
import lesson.plugins.person.support.PersonId;
import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;
import nucleus.ActorContext;
import plugins.stochastics.StochasticsDataManager;

public final class VaccineScheduler {

	public void init(ActorContext actorContext) {
		actorContext.addPlan(this::scheduleVaccinations, 1);
	}

	private void scheduleVaccinations(ActorContext actorContext) {

		// get the data managers that will be needed to add people and families
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		// plan out randomized vaccinations, one person per day
		List<PersonId> people = new ArrayList<>(personDataManager.getPeople());

		// randomize the people
		Collections.shuffle(people);

		// schedule the vaccinations
		double planTime = 1;
		for (PersonId personId : people) {
			actorContext.addPlan((context) -> vaccinatePerson(context, personId), planTime);
			planTime += randomGenerator.nextDouble() * 0.1;
		}
	}

	private void vaccinatePerson(ActorContext actorContext, PersonId personId) {
		// The person may have already been removed from the simulation, so we
		// check that before trying to vaccinate them.
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		if (personDataManager.personExists(personId)) {
			VaccinationDataManager vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
			vaccinationDataManager.vaccinatePerson(personId);
		}
	}
}
