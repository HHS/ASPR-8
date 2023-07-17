package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.vaccine.support.VaccineInitialization;
import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.stochastics.datamanagers.StochasticsDataManager;

public final class PopulationManager {

	/* start code_ref= people_plugin_population_manager*/
	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = 
				actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		double planTime = randomGenerator.nextDouble();
		for (int i = 0; i < 100; i++) {
			actorContext.addPlan((c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				if (randomGenerator.nextDouble()<0.1) {
					List<PersonId> people = peopleDataManager.getPeople();
					if (!people.isEmpty()) {
						PersonId personId = people.get(randomGenerator.nextInt(people.size()));
						peopleDataManager.removePerson(personId);
					}
				} else {
					int intialVaccineCount = randomGenerator.nextInt(3);
					VaccineInitialization vaccineInitialization = 
							new VaccineInitialization(intialVaccineCount);
					PersonConstructionData personConstructionData = PersonConstructionData.builder()//
							.add(vaccineInitialization)//
							.build();
					peopleDataManager.addPerson(personConstructionData);
				}
			}, planTime);
			planTime += randomGenerator.nextDouble();
		}
	}
	/* end */
}
