package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyValueInitialization;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;

public class PopulationLoader {

	private RandomGenerator randomGenerator;
	private RegionsDataManager regionsDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private GroupsDataManager groupsDataManager;
	private PeopleDataManager peopleDataManager;
	private double susceptibleProbability;
	private double childPopulationProportion;
	private double seniorPopulationProportion;
	private double averageHomeSize;
	private double averageSchoolSize;
	private double averageWorkSize;

	/* start code_ref= groups_plugin_population_loader_init_region_population */
	private void initializeRegionPopulation(RegionId regionId, int populationSize) {

		double n = populationSize;
		int homeCount = (int) (n / averageHomeSize) + 1;
		int childCount = (int) (n * childPopulationProportion);
		int adultCount = populationSize - childCount;
		homeCount = FastMath.min(homeCount, adultCount);
		int seniorCount = (int) (n * seniorPopulationProportion);
		seniorCount = FastMath.min(seniorCount, adultCount);
		int workingAdultCount = adultCount - seniorCount;
		int workCount = (int) ((double) workingAdultCount / averageWorkSize) + 1;
		int schoolCount = (int) ((double) childCount / averageSchoolSize) + 1;

		// create the population
		for (int i = 0; i < populationSize; i++) {
			int age;
			if (i < seniorCount) {
				age = randomGenerator.nextInt(25) + 65;
			} else if (i < adultCount) {
				age = randomGenerator.nextInt(18) + (65 - 18);
			} else {
				age = randomGenerator.nextInt(18);
			}
			PersonPropertyValueInitialization ageInitialization = new PersonPropertyValueInitialization(
					PersonProperty.AGE, age);

			DiseaseState diseaseState = DiseaseState.IMMUNE;
			if (randomGenerator.nextDouble() < susceptibleProbability) {
				diseaseState = DiseaseState.SUSCEPTIBLE;
			}

			PersonPropertyValueInitialization diseaseInitialization = new PersonPropertyValueInitialization(
					PersonProperty.DISEASE_STATE, diseaseState);
			PersonConstructionData personConstructionData = PersonConstructionData.builder()//
					.add(ageInitialization)//
					.add(diseaseInitialization)//
					.add(regionId)//
					.build();
			peopleDataManager.addPerson(personConstructionData);
		}
		/* end */
		/* start code_ref= groups_plugin_population_loader_adding_groups */
		// create the home groups
		List<GroupId> homeGroupIds = new ArrayList<>();
		for (int i = 0; i < homeCount; i++) {
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.HOME)
					.build();
			GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			homeGroupIds.add(groupId);
		}

		// create the work groups
		List<GroupId> workGroupIds = new ArrayList<>();
		for (int i = 0; i < workCount; i++) {
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.WORK)
					.build();
			GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			workGroupIds.add(groupId);
		}

		// create the school groups
		List<GroupId> schoolGroupIds = new ArrayList<>();
		for (int i = 0; i < schoolCount; i++) {
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder()
					.setGroupTypeId(GroupType.SCHOOL).build();
			GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			schoolGroupIds.add(groupId);
		}
		/* end */

		// determine the subsets of people by age
		/* start code_ref= groups_plugin_population_loader_age_subsets */
		List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
		List<PersonId> adults = new ArrayList<>();
		List<PersonId> children = new ArrayList<>();
		List<PersonId> workingAdults = new ArrayList<>();
		for (PersonId personId : peopleInRegion) {
			int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			if (age < 18) {
				children.add(personId);
			} else {
				adults.add(personId);
				if (age < 65) {
					workingAdults.add(personId);
				}
			}
		}
		/* end */
		/* start code_ref= groups_plugin_population_loader_group_assignments */
		Random random = new Random(randomGenerator.nextLong());
		/*
		 * Randomize the adults and assign them to the home groups such that there is at
		 * least one adult in each home
		 */
		Collections.shuffle(adults, random);
		// put one adult in each home
		for (int i = 0; i < homeGroupIds.size(); i++) {
			PersonId personId = adults.get(i);
			GroupId groupId = homeGroupIds.get(i);
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign the remaining adults at random to homes
		for (int i = homeGroupIds.size(); i < adults.size(); i++) {
			PersonId personId = adults.get(i);
			GroupId groupId = homeGroupIds.get(randomGenerator.nextInt(homeGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign working age adults to work groups
		for (int i = 0; i < workingAdults.size(); i++) {
			PersonId personId = workingAdults.get(i);
			GroupId groupId = workGroupIds.get(randomGenerator.nextInt(workGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign children to school groups
		for (int i = 0; i < children.size(); i++) {
			PersonId personId = children.get(i);
			GroupId groupId = schoolGroupIds.get(randomGenerator.nextInt(schoolGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign children to home groups
		for (int i = 0; i < children.size(); i++) {
			PersonId personId = children.get(i);
			GroupId groupId = homeGroupIds.get(randomGenerator.nextInt(homeGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}
	}

	/* end */
	/* start code_ref= groups_plugin_population_loader_init */
	public void init(ActorContext actorContext) {
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		susceptibleProbability = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		childPopulationProportion = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION);
		seniorPopulationProportion = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION);
		averageHomeSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE);
		averageSchoolSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE);
		averageWorkSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE);

		Set<RegionId> regionIds = regionsDataManager.getRegionIds();
		int regionSize = populationSize / regionIds.size();
		int leftoverPeople = populationSize % regionIds.size();

		for (RegionId regionId : regionIds) {
			int regionPopulation = regionSize;
			if (leftoverPeople > 0) {
				leftoverPeople--;
				regionPopulation++;
			}
			initializeRegionPopulation(regionId, regionPopulation);
		}
	}
	/* end */
}
