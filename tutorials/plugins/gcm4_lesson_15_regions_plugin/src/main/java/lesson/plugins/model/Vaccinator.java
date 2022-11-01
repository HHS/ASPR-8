package lesson.plugins.model;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.vaccine.VaccinationDataManager;
import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;

public final class Vaccinator {

	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		double planTime = randomGenerator.nextDouble();
		for (int i = 0; i < 300; i++) {
			actorContext.addPlan((c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				VaccinationDataManager vaccinationDataManager = c.getDataManager(VaccinationDataManager.class);
				List<PersonId> people = peopleDataManager.getPeople();
				if (!people.isEmpty()) {
					PersonId personId = people.get(randomGenerator.nextInt(people.size()));
					vaccinationDataManager.vaccinatePerson(personId);
				}
			}, planTime);
			planTime += randomGenerator.nextDouble()/3;
		}
	}
}
