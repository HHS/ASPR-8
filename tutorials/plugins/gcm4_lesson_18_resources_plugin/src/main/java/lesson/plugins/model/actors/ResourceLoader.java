package lesson.plugins.model.actors;

import java.util.Set;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.Resource;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;

public class ResourceLoader {

	public void init(ActorContext actorContext) {
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		Set<RegionId> regionIds = regionsDataManager.getRegionIds();
		
		double dosesPerPerson = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON);
		double totalDoses = dosesPerPerson*populationSize;
		int doseCount = (int)totalDoses;
		int doseCountPerRegion = doseCount/regionIds.size();
		
		double bedsPerPerson = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_BEDS_PER_PERSON);
		double totalBeds = bedsPerPerson*populationSize;
		int bedCount=  (int)totalBeds;
		int bedCountPerRegion = bedCount/regionIds.size();
		
		for (RegionId regionId : regionIds) {			
			resourcesDataManager.addResourceToRegion(Resource.ANTI_VIRAL_MED, regionId, doseCountPerRegion);
			resourcesDataManager.addResourceToRegion(Resource.HOSPITAL_BED, regionId, bedCountPerRegion);
		}
	}

}
