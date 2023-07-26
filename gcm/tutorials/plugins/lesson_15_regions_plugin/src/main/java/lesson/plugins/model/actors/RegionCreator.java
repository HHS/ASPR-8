package lesson.plugins.model.actors;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionConstructionData.Builder;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import lesson.plugins.model.Region;
import lesson.plugins.model.RegionProperty;

public class RegionCreator {
	/* start code_ref= regions_plugin_region_creator_add_region */
	private void addRegion(ActorContext actorContext) {
		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		Set<Region> regions = regionsDataManager.getRegionIds();
		int maxRegionValue = -1;
		for (Region region : regions) {
			int value = region.getValue();
			maxRegionValue = FastMath.max(value, maxRegionValue);
		}
		Region newRegion = new Region(maxRegionValue + 1);
		Builder regionBuilder = RegionConstructionData.builder().setRegionId(newRegion);
		regionBuilder.setRegionPropertyValue(RegionProperty.LAT, 35 + randomGenerator.nextDouble());
		regionBuilder.setRegionPropertyValue(RegionProperty.LON, 128 + randomGenerator.nextDouble());

		if (regionsDataManager.regionPropertyIdExists(RegionProperty.VACCINE_PRIORITY)) {
			regionBuilder.setRegionPropertyValue(RegionProperty.VACCINE_PRIORITY, randomGenerator.nextBoolean());
		}
		RegionConstructionData regionConstructionData = regionBuilder.build();
		regionsDataManager.addRegion(regionConstructionData);
	}

	/* end */
	/* start code_ref= regions_plugin_region_creator_init */
	public void init(ActorContext actorContext) {
		for (int i = 0; i < 5; i++) {
			double planTime = 20 * i + 1;
			actorContext.addPlan(this::addRegion, planTime);
		}
	}
	/* end */
}
