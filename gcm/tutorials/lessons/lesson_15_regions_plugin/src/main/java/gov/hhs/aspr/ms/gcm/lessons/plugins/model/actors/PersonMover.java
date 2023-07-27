package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public class PersonMover {

	/* start code_ref= regions_plugin_person_mover_move_person */
	private void moveRandomPerson(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		// pick a random person
		List<PersonId> people = peopleDataManager.getPeople();
		if (people.isEmpty()) {
			return;
		}
		PersonId personId = people.get(randomGenerator.nextInt(people.size()));

		// pick a new random new region for that person
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
		RegionId personRegion = regionsDataManager.getPersonRegion(personId);
		regionIds.remove(personRegion);
		if (regionIds.isEmpty()) {
			return;
		}
		RegionId newPersonRegion = regionIds.get(randomGenerator.nextInt(regionIds.size()));

		// assign the region to the person
		regionsDataManager.setPersonRegion(personId, newPersonRegion);
	}
	/* end */

	/* start code_ref= regions_plugin_person_mover_init */
	public void init(ActorContext actorContext) {
		for (int i = 0; i < 1000; i++) {
			double planTime = ((double) i) * 0.1;
			actorContext.addPlan(this::moveRandomPerson, planTime);
		}
	}
	/* end */

}
