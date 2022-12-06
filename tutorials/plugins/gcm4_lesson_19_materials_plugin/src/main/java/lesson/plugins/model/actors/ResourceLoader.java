package lesson.plugins.model.actors;

import java.util.Set;

import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.Resource;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;

public class ResourceLoader {

	/*
	 * Allocate antiviral doses and hospital beds uniformly to all regions.
	 */
	
	public void init(ActorContext actorContext) {
		
		RegionsDataManager regionsDataManager = 
				actorContext.getDataManager(RegionsDataManager.class);
		ResourcesDataManager resourcesDataManager = 
				actorContext.getDataManager(ResourcesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = 
				actorContext.getDataManager(GlobalPropertiesDataManager.class);
		int populationSize = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		Set<RegionId> regionIds = regionsDataManager.getRegionIds();

		double dosesPerPerson = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.VACCINED_DOSES_PER_PERSON);
		
		double totalDoses = dosesPerPerson * populationSize;		
		int doseCount = (int) totalDoses;
		int doseCountPerRegion = doseCount / regionIds.size();

		for (RegionId regionId : regionIds) {
			resourcesDataManager
				.addResourceToRegion(Resource.VACCINE, regionId, doseCountPerRegion);
			
		}
	}

}
