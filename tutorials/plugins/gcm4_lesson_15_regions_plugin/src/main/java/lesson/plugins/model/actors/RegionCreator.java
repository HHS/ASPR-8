package lesson.plugins.model.actors;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.Region;
import lesson.plugins.model.RegionProperty;
import nucleus.ActorContext;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionConstructionData.Builder;
import plugins.stochastics.datamanagers.StochasticsDataManager;

public class RegionCreator {

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

	public void init(ActorContext actorContext) {
		for (int i = 0; i < 5; i++) {
			double planTime = 20 * i + 1;
			actorContext.addPlan(this::addRegion, planTime);
		}
	}

}
