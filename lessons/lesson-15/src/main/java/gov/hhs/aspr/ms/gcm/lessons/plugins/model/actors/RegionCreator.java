package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Region;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.RegionProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionConstructionData.Builder;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;

public class RegionCreator {
	/* start code_ref= regions_plugin_region_creator_add_region|code_cap=When the region creator actor adds a new region, it assigns a random lat-lon corrdinate and possibly assigns a vaccine priority status to the region. */
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
	/* start code_ref= regions_plugin_region_creator_init|code_cap=The region creator actor plans the addition of five new regions. */
	public void init(ActorContext actorContext) {
		for (int i = 0; i < 5; i++) {
			double planTime = 20 * i + 1;
			actorContext.addPlan(this::addRegion, planTime);
		}
	}
	/* end */
}
