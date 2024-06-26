package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyValueInitialization;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.wrappers.MutableDouble;

public class PopulationLoader {
	private RegionId defaultRegionId;
	private Map<RegionId, MutableDouble> regionMap = new LinkedHashMap<>();
	private RandomGenerator randomGenerator;
	private RegionsDataManager regionsDataManager;

	private void buildUnbalancedRegions() {

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
		}
		return defaultRegionId;
	}

	/* start code_ref=resources_population_loader_init|code_cap= The population loader initializes the population by assigning to each person a randomly selected region id and immunity status. */
	public void init(ActorContext actorContext) {

		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		double susceptibleProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		double immuneProbabilty = 1 - susceptibleProbability;

		/*
		 * Derive mapping from region to probability that a person will be assigned to
		 * that region that will likely not put the same number of people in each
		 * region.
		 */
		buildUnbalancedRegions();

		/*
		 * Add each person to the simulation. Determine their region id and the immune
		 * state. The other person properties will have default values.
		 */
		for (int i = 0; i < populationSize; i++) {
			RegionId regionId = getRandomRegionId();
			boolean immune = randomGenerator.nextDouble() < immuneProbabilty;
			PersonPropertyValueInitialization personPropertyInitialization = new PersonPropertyValueInitialization(
					PersonProperty.IMMUNE, immune);
			PersonConstructionData personConstructionData = PersonConstructionData.builder()//
					.add(personPropertyInitialization)//
					.add(regionId)//
					.build();
			peopleDataManager.addPerson(personConstructionData);
		}
	}
	/* end */
}
