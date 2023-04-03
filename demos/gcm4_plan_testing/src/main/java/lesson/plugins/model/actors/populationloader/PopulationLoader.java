package lesson.plugins.model.actors.populationloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;

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
	private double averageWorkSize;
	private double averageSchoolSize;

	public void init(final ActorContext actorContext) {
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		if(peopleDataManager.getPopulationCount()>0) {
			return;
		}
		
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
		final StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		
		
		
		final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		final int populationSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.POPULATION_SIZE);
		susceptibleProbability = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		childPopulationProportion = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION);
		seniorPopulationProportion = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION);
		averageHomeSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE);
		averageSchoolSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE);
		averageWorkSize = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE);

		final Set<RegionId> regionIds = regionsDataManager.getRegionIds();
		final int regionSize = populationSize / regionIds.size();
		int leftoverPeople = populationSize % regionIds.size();

		for (final RegionId regionId : regionIds) {
			int regionPopulation = regionSize;
			if (leftoverPeople > 0) {
				leftoverPeople--;
				regionPopulation++;
			}
			initializeRegionPopulation(regionId, regionPopulation);
		}
	}

	private void initializeRegionPopulation(final RegionId regionId, final int populationSize) {

		final double n = populationSize;
		int homeCount = (int) (n / averageHomeSize) + 1;
		final int childCount = (int) (n * childPopulationProportion);
		final int adultCount = populationSize - childCount;
		homeCount = FastMath.min(homeCount, adultCount);
		int seniorCount = (int) (n * seniorPopulationProportion);
		seniorCount = FastMath.min(seniorCount, adultCount);
		final int workingAdultCount = adultCount - seniorCount;
		final int workCount = (int) (workingAdultCount / averageWorkSize) + 1;
		final int schoolCount = (int) (childCount / averageSchoolSize) + 1;

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
			final PersonPropertyInitialization ageInitialization = new PersonPropertyInitialization(PersonProperty.AGE, age);

			DiseaseState diseaseState = DiseaseState.IMMUNE;
			if (randomGenerator.nextDouble() < susceptibleProbability) {
				diseaseState = DiseaseState.SUSCEPTIBLE;
			}

			final PersonPropertyInitialization diseaseInitialization = new PersonPropertyInitialization(PersonProperty.DISEASE_STATE, diseaseState);
			final PersonConstructionData personConstructionData = PersonConstructionData.builder()//
																						.add(ageInitialization)//
																						.add(diseaseInitialization)//
																						.add(regionId)//
																						.build();
			peopleDataManager.addPerson(personConstructionData);
		}

		// create the home groups
		final List<GroupId> homeGroupIds = new ArrayList<>();
		for (int i = 0; i < homeCount; i++) {
			final GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.HOME).build();
			final GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			homeGroupIds.add(groupId);
		}

		// create the work groups
		final List<GroupId> workGroupIds = new ArrayList<>();
		for (int i = 0; i < workCount; i++) {
			final GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.WORK).build();
			final GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			workGroupIds.add(groupId);
		}

		// create the school groups
		final List<GroupId> schoolGroupIds = new ArrayList<>();
		for (int i = 0; i < schoolCount; i++) {
			final GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(GroupType.SCHOOL).build();
			final GroupId groupId = groupsDataManager.addGroup(groupConstructionInfo);
			schoolGroupIds.add(groupId);
		}

		// determine the subsets of people by age
		final List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
		final List<PersonId> adults = new ArrayList<>();
		final List<PersonId> children = new ArrayList<>();
		final List<PersonId> workingAdults = new ArrayList<>();
		for (final PersonId personId : peopleInRegion) {
			final int age = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.AGE);
			if (age < 18) {
				children.add(personId);
			} else {
				adults.add(personId);
				if (age < 65) {
					workingAdults.add(personId);
				}
			}
		}

		final Random random = new Random(randomGenerator.nextLong());
		/*
		 * Randomize the adults and assign them to the home groups such that
		 * there is at least one adult in each home
		 */
		Collections.shuffle(adults, random);
		// put one adult in each home
		for (int i = 0; i < homeGroupIds.size(); i++) {
			final PersonId personId = adults.get(i);
			final GroupId groupId = homeGroupIds.get(i);
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign the remaining adults at random to homes
		for (int i = homeGroupIds.size(); i < adults.size(); i++) {
			final PersonId personId = adults.get(i);
			final GroupId groupId = homeGroupIds.get(randomGenerator.nextInt(homeGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign working age adults to work groups
		for (final PersonId personId : workingAdults) {
			final GroupId groupId = workGroupIds.get(randomGenerator.nextInt(workGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign children to school groups
		for (final PersonId personId : children) {
			final GroupId groupId = schoolGroupIds.get(randomGenerator.nextInt(schoolGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}

		// assign children to home groups
		for (final PersonId personId : children) {
			final GroupId groupId = homeGroupIds.get(randomGenerator.nextInt(homeGroupIds.size()));
			groupsDataManager.addPersonToGroup(personId, groupId);
		}
	}
}
