package manual.demo.components;

import java.util.LinkedHashMap;
import java.util.Map;

import manual.demo.datatypes.PopulationDescription;
import manual.demo.identifiers.Compartment;
import manual.demo.identifiers.GlobalProperty;
import manual.demo.identifiers.GroupType;
import manual.demo.identifiers.PersonProperty;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupsForPersonAndGroupTypeFilter;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.Partition;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionFilter;
import plugins.regions.support.RegionId;
import util.TimeElapser;

public class PopulationLoader extends AbstractComponent {

	@Override
	public void executePlan(final Environment environment, final Plan plan) {

		TimeElapser timeElapser = new TimeElapser();

		PopulationDescription populationDescription = environment.getGlobalPropertyValue(GlobalProperty.POPULATION_DESCRIPTION);
		Map<String, RegionId> regionMap = new LinkedHashMap<>();
		for (RegionId regionId : environment.getRegionIds()) {
			regionMap.put(regionId.toString(), regionId);
		}

		// System.out.println("region map preparation " +
		// timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();

		Map<String, GroupId> homeIds = new LinkedHashMap<>();
		Map<String, GroupId> schoolIds = new LinkedHashMap<>();
		Map<String, GroupId> workPlaceIds = new LinkedHashMap<>();

		populationDescription.getPopulationElements().forEach(populationElement -> {

			String regionIdString = populationElement.getHomeId().substring(0, 11);

			// determine the region and create the person
			RegionId regionId = regionMap.get(regionIdString);
			PersonId personId;
			if (environment.getRandomGenerator().nextDouble() < 0.001) {
				personId = environment.addPerson(regionId, Compartment.DEAD);
			} else {
				personId = environment.addPerson(regionId, Compartment.SUSCEPTIBLE);
			}
			environment.setPersonPropertyValue(personId, PersonProperty.AGE, populationElement.getAge());
			boolean immune = environment.getRandomGenerator().nextDouble() < 0.05;
			environment.setPersonPropertyValue(personId, PersonProperty.IMMUNE, immune);

			// place the person in a home group
			GroupId groupId = homeIds.get(populationElement.getHomeId());
			if (groupId == null) {
				groupId = environment.addGroup(GroupType.HOME);
				homeIds.put(populationElement.getHomeId(), groupId);
			}
			environment.addPersonToGroup(personId, groupId);

			// place the person in a school group

			if (!populationElement.getSchoolId().isEmpty()) {
				groupId = schoolIds.get(populationElement.getSchoolId());
				if (groupId == null) {
					groupId = environment.addGroup(GroupType.SCHOOL);
					schoolIds.put(populationElement.getSchoolId(), groupId);
				}
				environment.addPersonToGroup(personId, groupId);
			}

			// place the person in a work place group
			if (!populationElement.getWorkPlaceId().isEmpty()) {
				groupId = workPlaceIds.get(populationElement.getWorkPlaceId());
				if (groupId == null) {
					groupId = environment.addGroup(GroupType.WORK);
					workPlaceIds.put(populationElement.getWorkPlaceId(), groupId);
				}
				// environment.setPersonPropertyValue(personId,
				// PersonProperty.IS_WORKING, true);
				environment.addPersonToGroup(personId, groupId);
			}

		});

		 System.out.println("population and group loading " +
		 timeElapser.getElapsedMilliSeconds());
		timeElapser.reset();

		environment.getRegionIds().stream().forEach(regionId -> {
			Filter filter = new RegionFilter(regionId).and(new GroupsForPersonAndGroupTypeFilter(GroupType.WORK, Equality.GREATER_THAN, 0));
			Partition partition = Partition.builder().setFilter(filter).build();
			environment.addPartition(partition, regionId);
		});

		double indexLoadingTime = timeElapser.getElapsedMilliSeconds();

		@SuppressWarnings("unused")
		double averageTimeToLoadIndex = indexLoadingTime;
		averageTimeToLoadIndex /= environment.getRegionIds().size();

		timeElapser.reset();

		// Some more stats of interest
		// System.out.println("total population = " +
		// environment.getPopulationCount());
		// System.out.println("total homes = " +
		// environment.getGroupCountForGroupType(GroupType.HOME));
		// System.out.println("total schools = " +
		// environment.getGroupCountForGroupType(GroupType.SCHOOL));
		int workPlaceCount = environment.getGroupCountForGroupType(GroupType.WORK);
		// System.out.println("total work places = " + workPlaceCount);
		// System.out.println("total regions = " +
		// environment.getRegionIds().size());

		long workingPeople = environment.getPeople()//
										.stream()//
										.filter(personId -> environment.getGroupCountForGroupTypeAndPerson(GroupType.WORK, personId) > 0)//
										.count();//

		// System.out.println("number of people working = " + workingPeople);
		@SuppressWarnings("unused")
		double averageNumberofWorkersPerWorkPlace = workingPeople;
		averageNumberofWorkersPerWorkPlace /= workPlaceCount;
		// System.out.println("average number of workers per workplace = " +
		// averageNumberofWorkersPerWorkPlace);

	}

	@Override
	public void init(Environment environment) {
		environment.addPlan(new Plan() {
		}, 0);
	}

}
