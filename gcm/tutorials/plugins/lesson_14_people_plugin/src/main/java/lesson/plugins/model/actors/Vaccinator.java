package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;

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
			planTime += randomGenerator.nextDouble() / 3;
		}
	}
}
