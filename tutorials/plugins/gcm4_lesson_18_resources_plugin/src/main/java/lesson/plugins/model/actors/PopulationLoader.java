package lesson.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;
import util.wrappers.MutableDouble;

public class PopulationLoader {
	private RegionId defaultRegionId;
	private Map<RegionId, MutableDouble> regionMap = new LinkedHashMap<>();
	private RandomGenerator randomGenerator;
	private RegionsDataManager regionsDataManager;

	private void buildUnbalanceRegions() {

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			double value = randomGenerator.nextDouble();
			value = value * 0.9 + .1;
			regionMap.put(regionId, new MutableDouble(value));
			defaultRegionId = regionId;
		}
		double sum = 0;
		for (RegionId regionId : regionMap.keySet()) {
			sum += regionMap.get(regionId).getValue();
		}
		for (RegionId regionId : regionMap.keySet()) {
			MutableDouble mutableDouble = regionMap.get(regionId);
			double value = mutableDouble.getValue();
			value /= sum;
			mutableDouble.setValue(value);
		}
		sum = 0;
		for (RegionId regionId : regionMap.keySet()) {
			MutableDouble mutableDouble = regionMap.get(regionId);
			double value = mutableDouble.getValue();
			sum += value;
			mutableDouble.setValue(sum);
		}
	}

	private RegionId getRandomRegionId() {
		double value = randomGenerator.nextDouble();
		for (RegionId regionId : regionMap.keySet()) {
			MutableDouble mutableDouble = regionMap.get(regionId);
			if (mutableDouble.getValue() >= value) {
				return regionId;
			}
			value -= mutableDouble.getValue();
		}
		return defaultRegionId;
	}

	public void init(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		double susceptibleProbability = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		double immuneProbabilty = 1 - susceptibleProbability;

		buildUnbalanceRegions();

		for (int i = 0; i < populationSize; i++) {
			RegionId regionId = getRandomRegionId();
			boolean immune = randomGenerator.nextDouble() < immuneProbabilty;
			PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(PersonProperty.IMMUNE, immune);
			PersonConstructionData personConstructionData = PersonConstructionData	.builder()//
																					.add(personPropertyInitialization)//
																					.add(regionId)//
																					.build();

			peopleDataManager.addPerson(personConstructionData);
		}
	}
}
