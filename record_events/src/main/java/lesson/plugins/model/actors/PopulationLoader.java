package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.support.GlobalProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;

public class PopulationLoader {

		public void init(final ActorContext actorContext) {				
		final StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

		final int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		for(int i = 0;i<populationSize;i++) {
			RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(regionId).build();
			peopleDataManager.addPerson(personConstructionData);
		}		
	}

}
