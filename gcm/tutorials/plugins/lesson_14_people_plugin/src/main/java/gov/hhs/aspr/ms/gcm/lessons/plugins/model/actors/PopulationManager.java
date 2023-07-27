package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.support.VaccineInitialization;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public final class PopulationManager {

	/* start code_ref= people_plugin_population_manager */
	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		double planTime = randomGenerator.nextDouble();
		for (int i = 0; i < 100; i++) {
			actorContext.addPlan((c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				if (randomGenerator.nextDouble() < 0.1) {
					List<PersonId> people = peopleDataManager.getPeople();
					if (!people.isEmpty()) {
						PersonId personId = people.get(randomGenerator.nextInt(people.size()));
						peopleDataManager.removePerson(personId);
					}
				} else {
					int intialVaccineCount = randomGenerator.nextInt(3);
					VaccineInitialization vaccineInitialization = new VaccineInitialization(intialVaccineCount);
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
