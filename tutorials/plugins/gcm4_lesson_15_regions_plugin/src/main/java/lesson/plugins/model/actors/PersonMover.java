package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;

public class PersonMover {
	
	
	private void moveRandomPerson(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		
		//pick a random person
		List<PersonId> people = peopleDataManager.getPeople();
		if(people.isEmpty()) {
			return;
		}
		PersonId personId = people.get(randomGenerator.nextInt(people.size()));
		
		//pick a new random new region for that person
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
		RegionId personRegion = regionsDataManager.getPersonRegion(personId);
		regionIds.remove(personRegion);
		if(regionIds.isEmpty()) {
			return;
		}		
		RegionId newPersonRegion = regionIds.get(randomGenerator.nextInt(regionIds.size()));
		
		//assign the region to the person
		regionsDataManager.setPersonRegion(personId, newPersonRegion);		
	}

	public void init(ActorContext actorContext) {
		for(int i = 0;i<1000;i++) {
			double planTime = ((double)i)*0.1;
			actorContext.addPlan(this::moveRandomPerson, planTime);			
		}		
	}
	
}
