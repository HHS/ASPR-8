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

	public void init(final ActorContext actorContext) {

		final RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		final ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		final int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		final Set<RegionId> regionIds = regionsDataManager.getRegionIds();

		final double dosesPerPerson = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.VACCINED_DOSES_PER_PERSON);

		final double totalDoses = dosesPerPerson * populationSize;
		final int doseCount = (int) totalDoses;
		final int doseCountPerRegion = doseCount / regionIds.size();

		for (final RegionId regionId : regionIds) {
			resourcesDataManager.addResourceToRegion(Resource.VACCINE, regionId, doseCountPerRegion);

		}
	}

}
