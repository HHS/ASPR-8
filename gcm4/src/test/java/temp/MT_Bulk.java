package temp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.errors.ContractError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.time.StopwatchManager;
import util.time.Watch;

public class MT_Bulk {

	public static enum LoadMode {
		PLUGIN, BULK, SINGULAR
	}

	private static enum LocalGroupType implements GroupTypeId {
		SCHOOL, WORK, HOME;
	}

	private static class Data {
		private long seed;

		// people
		private LoadMode loadMode;
		private int populationSize;

		// person properties
		private boolean usePersonProperties;
		private int personPropertyCount;
		private int initializedPersonPropertyCount;

		// groups
		private boolean useGroups;
		private boolean useGroupProperties;
		private int householdSize;
		private int schoolSize;
		private int workplaceSize;
		private double schoolAgeProportion;
		private double activeWorkerProportion;
		private int housePropertyCount;
		private int schoolPropertyCount;
		private int workPropertyCount;

		// regions
		private boolean useRegions;
		private int regionPropertyCount;
		private int initializedRegionPropertyCount;
		private int regionCount;

		// resources
		private boolean useResources;
		private int resourceCount;
		private int initializedResourceCount;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("seed");
			builder.append("\t");
			builder.append(seed);
			builder.append("\n");

			builder.append("loadMode");
			builder.append("\t");
			builder.append(loadMode);
			builder.append("\n");

			builder.append("populationSize");
			builder.append("\t");
			builder.append(populationSize);
			builder.append("\n");

			builder.append("usePersonProperties");
			builder.append("\t");
			builder.append(usePersonProperties);
			builder.append("\n");

			builder.append("personPropertyCount");
			builder.append("\t");
			builder.append(personPropertyCount);
			builder.append("\n");

			builder.append("initializedPersonPropertyCount");
			builder.append("\t");
			builder.append(initializedPersonPropertyCount);
			builder.append("\n");

			builder.append("useGroups");
			builder.append("\t");
			builder.append(useGroups);
			builder.append("\n");

			builder.append("useGroupProperties");
			builder.append("\t");
			builder.append(useGroupProperties);
			builder.append("\n");

			builder.append("householdSize");
			builder.append("\t");
			builder.append(householdSize);
			builder.append("\n");

			builder.append("schoolSize");
			builder.append("\t");
			builder.append(schoolSize);
			builder.append("\n");

			builder.append("workplaceSize");
			builder.append("\t");
			builder.append(workplaceSize);
			builder.append("\n");

			builder.append("schoolAgeProportion");
			builder.append("\t");
			builder.append(schoolAgeProportion);
			builder.append("\n");

			builder.append("activeWorkerProportion");
			builder.append("\t");
			builder.append(activeWorkerProportion);
			builder.append("\n");

			builder.append("useRegions");
			builder.append("\t");
			builder.append(useRegions);
			builder.append("\n");

			builder.append("regionPropertyCount");
			builder.append("\t");
			builder.append(regionPropertyCount);
			builder.append("\n");

			builder.append("initializedRegionPropertyCount");
			builder.append("\t");
			builder.append(initializedRegionPropertyCount);
			builder.append("\n");

			builder.append("regionCount");
			builder.append("\t");
			builder.append(regionCount);
			builder.append("\n");

			builder.append("useResources");
			builder.append("\t");
			builder.append(useResources);
			builder.append("\n");

			return builder.toString();
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	private static enum BulkTestError implements ContractError {
		BAD_POPULATION_PROPORTION("Population proportion must be in interval [0,1]"),
		MISSING_LOAD_MODE("Load mode not assigned"), //
		NEGATIVE_PROPERTY_COUNT("Negative property count"), //
		BAD_INIT_PROPERTY_COUNT("The  number of properties to initialize exceeds the total number of  properties"), //
		UNKNOWN_PROPERTY_TYPE("Unknown property type"), //
		NEGATIVE_POPULATION_SIZE("A population size is negative"), //
		ILLEGAL_GROUP_SIZE("A group size is either negative or is zero with groups active"), //
		NEGATIVE_RESOURCE_COUNT("Negative resource count"), //
		BAD_INIT_RESOURCE_COUNT("The  number of resources to initialize exceeds the total number of resources"), //
		;

		private final String description;

		private BulkTestError(String description) {
			this.description = description;
		}

		@Override
		public String getDescription() {
			return description;
		}
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.loadMode == null) {
				throw new ContractException(BulkTestError.MISSING_LOAD_MODE);
			}

			if (data.initializedPersonPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}
			if (data.personPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}

			if (data.initializedPersonPropertyCount > data.personPropertyCount) {
				throw new ContractException(BulkTestError.BAD_INIT_PROPERTY_COUNT);
			}
			if (data.populationSize < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_POPULATION_SIZE);
			}

			if (data.householdSize < 0 || data.householdSize == 0 && data.useGroups) {
				throw new ContractException(BulkTestError.ILLEGAL_GROUP_SIZE, "household");
			}

			if (data.schoolSize < 0 || data.schoolSize == 0 && data.useGroups) {
				throw new ContractException(BulkTestError.ILLEGAL_GROUP_SIZE, "school");
			}

			if (data.workplaceSize < 0 || data.workplaceSize == 0 && data.useGroups) {
				throw new ContractException(BulkTestError.ILLEGAL_GROUP_SIZE, "workplace");
			}
			if (data.regionPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}
			if (data.initializedRegionPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}

			if (data.initializedRegionPropertyCount > data.regionPropertyCount) {
				throw new ContractException(BulkTestError.BAD_INIT_PROPERTY_COUNT);
			}

			if (data.schoolAgeProportion < 0) {
				throw new ContractException(BulkTestError.BAD_POPULATION_PROPORTION);
			}
			if (data.schoolAgeProportion > 1) {
				throw new ContractException(BulkTestError.BAD_POPULATION_PROPORTION);
			}
			if (data.activeWorkerProportion < 0) {
				throw new ContractException(BulkTestError.BAD_POPULATION_PROPORTION);
			}
			if (data.activeWorkerProportion > 1) {
				throw new ContractException(BulkTestError.BAD_POPULATION_PROPORTION);
			}

			if (data.housePropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}

			if (data.schoolPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}

			if (data.workPropertyCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_PROPERTY_COUNT);
			}

			if (data.resourceCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_RESOURCE_COUNT);
			}

			if (data.initializedResourceCount < 0) {
				throw new ContractException(BulkTestError.NEGATIVE_RESOURCE_COUNT);
			}

			if (data.initializedResourceCount > data.resourceCount) {
				throw new ContractException(BulkTestError.BAD_INIT_RESOURCE_COUNT);
			}

		}

		private void report() {
			System.out.println(data.toString());
		}

		public MT_Bulk build() {
			try {
				validate();
				report();
				return new MT_Bulk(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setSeed(long seed) {
			data.seed = seed;
			return this;
		}

		public Builder setPopulationSize(int populationSize) {
			data.populationSize = populationSize;
			return this;
		}

		public Builder setLoadMode(LoadMode loadMode) {
			data.loadMode = loadMode;
			return this;
		}

		public Builder setPersonPropertyCount(int personPropertyCount) {
			data.personPropertyCount = personPropertyCount;
			return this;
		}

		public Builder setInitializedPersonPropertyCount(int initializedPersonPropertyCount) {
			data.initializedPersonPropertyCount = initializedPersonPropertyCount;
			return this;
		}

		public Builder setUsePersonProperties(boolean usePersonProperties) {
			data.usePersonProperties = usePersonProperties;
			return this;
		}

		public Builder setUseGroups(boolean useGroups) {
			data.useGroups = useGroups;
			return this;
		}

		public Builder setUseGroupProperties(boolean useGroupProperties) {
			data.useGroupProperties = useGroupProperties;
			return this;
		}

		public Builder setUseRegions(boolean useRegions) {
			data.useRegions = useRegions;
			return this;
		}

		public Builder setRegionCount(int regionCount) {
			data.regionCount = regionCount;
			return this;
		}

		public Builder setUseResources(boolean useResources) {
			data.useResources = useResources;
			return this;
		}

		public Builder setHouseholdSize(int householdSize) {
			data.householdSize = householdSize;
			return this;
		}

		public Builder setSchoolSize(int schoolSize) {
			data.schoolSize = schoolSize;
			return this;
		}

		public Builder setWorkplaceSize(int workplaceSize) {
			data.workplaceSize = workplaceSize;
			return this;
		}

		public Builder setRegionPropertyCount(int regionPropertyCount) {
			data.regionPropertyCount = regionPropertyCount;
			return this;
		}

		public Builder setInitializedRegionPropertyCount(int initializedRegionPropertyCount) {
			data.initializedRegionPropertyCount = initializedRegionPropertyCount;
			return this;
		}

		public Builder setSchoolAgeProportion(double schoolAgeProportion) {
			data.schoolAgeProportion = schoolAgeProportion;
			return this;
		}

		public Builder setActiveWorkerProportion(double activeWorkerProportion) {
			data.activeWorkerProportion = activeWorkerProportion;
			return this;
		}

		public Builder setHousePropertyCount(int housePropertyCount) {
			data.housePropertyCount = housePropertyCount;
			return this;
		}

		public Builder setSchoolPropertyCount(int schoolPropertyCount) {
			data.schoolPropertyCount = schoolPropertyCount;
			return this;
		}

		public Builder setWorkPropertyCount(int workerPropertyCount) {
			data.workPropertyCount = workerPropertyCount;
			return this;
		}

		public Builder setResourceCount(int resourceCount) {
			data.resourceCount = resourceCount;
			return this;
		}

		public Builder setInitializedResourceCount(int initializedResourceCount) {
			data.initializedResourceCount = initializedResourceCount;
			return this;
		}

	}

	private static class LocalRegionPropertyId implements RegionPropertyId {
		private final int id;

		public LocalRegionPropertyId(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalRegionPropertyId)) {
				return false;
			}
			LocalRegionPropertyId other = (LocalRegionPropertyId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalRegionPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class LocalResourceId implements ResourceId {
		private final int id;

		public LocalResourceId(int id) {
			super();
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalResourceId)) {
				return false;
			}
			LocalResourceId other = (LocalResourceId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalResourceId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class LocalRegionId implements RegionId {
		private final int id;

		public LocalRegionId(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalRegionId)) {
				return false;
			}
			LocalRegionId other = (LocalRegionId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalRegionId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class LocalGroupPropertyId implements GroupPropertyId {
		private final int id;
		private LocalGroupType localGroupType;

		public LocalGroupPropertyId(LocalGroupType localGroupType, int id) {
			super();
			this.localGroupType = localGroupType;
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + ((localGroupType == null) ? 0 : localGroupType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalGroupPropertyId)) {
				return false;
			}
			LocalGroupPropertyId other = (LocalGroupPropertyId) obj;
			if (id != other.id) {
				return false;
			}
			if (localGroupType != other.localGroupType) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalGroupPropertyId [id=");
			builder.append(id);
			builder.append(", localGroupType=");
			builder.append(localGroupType);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class LocalPersonPropertyId implements PersonPropertyId {
		private final int id;

		public LocalPersonPropertyId(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalPersonPropertyId)) {
				return false;
			}
			LocalPersonPropertyId other = (LocalPersonPropertyId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalPersonPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private static class State {
		private RandomGenerator randomGenerator;
		private BulkPersonConstructionData.Builder bulkPersonBuilder = BulkPersonConstructionData.builder();
		private Simulation.Builder simulationBuilder = Simulation.builder();
		private List<PersonId> people = new ArrayList<>();
		private Map<LocalGroupType, Map<LocalGroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();
		private GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();
	}

	private final Data data;
	private State state;

	private MT_Bulk(Data data) {
		this.data = data;
	}

	private void loadPeopleForSinglesPopLoader(ActorContext actorContext) {
		if (data.loadMode != LoadMode.SINGULAR) {
			return;
		}
		StopwatchManager.start(Watch.PSLC_PERSON_PROPERTIES);
		List<Pair<PersonPropertyId, Class<?>>> personPropertyToTypeMap = new ArrayList<>();
		if (data.usePersonProperties) {
			PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
			for (PersonPropertyId personPropertyId : personPropertiesDataManager.getPersonPropertyIds()) {
				PropertyDefinition personPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				personPropertyToTypeMap.add(new Pair<>(personPropertyId, personPropertyDefinition.getType()));
			}
		}
		StopwatchManager.stop(Watch.PSLC_PERSON_PROPERTIES);

		List<RegionId> regionIds = null;
		if (data.useRegions) {
			StopwatchManager.start(Watch.PSLC_REGIONS);
			RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
			regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
			StopwatchManager.stop(Watch.PSLC_REGIONS);
		}

		List<ResourceId> resourceIds = null;
		if (data.useResources) {
			StopwatchManager.start(Watch.PSLC_RESOURCES);
			ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
			resourceIds = new ArrayList<>(resourcesDataManager.getResourceIds());
			StopwatchManager.stop(Watch.PSLC_RESOURCES);
		}

		if (data.useGroups) {
			StopwatchManager.start(Watch.PSLC_GROUPS);
			GroupsDataManager groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);

			int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * data.populationSize);
			int activeWorkerCount = (int) (data.activeWorkerProportion * (data.populationSize - schoolAgedChildrenCount));

			int householdCount = (int) ((double) data.populationSize / data.householdSize) + 1;
			int schoolCount = (int) ((double) schoolAgedChildrenCount / data.schoolSize) + 1;
			int workplaceCount = (int) ((double) activeWorkerCount / data.workplaceSize) + 1;

			// create the household groups
			Set<GroupPropertyId> groupPropertyIds = new LinkedHashSet<>();
			if (data.useGroupProperties) {
				groupPropertyIds = groupsDataManager.getGroupPropertyIds(LocalGroupType.HOME);
			}
			for (int i = 0; i < householdCount; i++) {
				GroupId groupId = groupsDataManager.addGroup(LocalGroupType.HOME);
				if (data.useGroupProperties) {
					for (GroupPropertyId groupPropertyId : groupPropertyIds) {
						Class<?> type = groupsDataManager.getGroupPropertyDefinition(LocalGroupType.HOME, groupPropertyId).getType();
						Object randomPropertyValue = getRandomPropertyValue(type);
						groupsDataManager.setGroupPropertyValue(groupId, groupPropertyId, randomPropertyValue);
					}
				}
			}

			// create the work groups
			for (int i = 0; i < workplaceCount; i++) {
				groupsDataManager.addGroup(LocalGroupType.WORK);
			}

			// create the school groups
			for (int i = 0; i < schoolCount; i++) {
				groupsDataManager.addGroup(LocalGroupType.SCHOOL);
			}
			StopwatchManager.stop(Watch.PSLC_GROUPS);
		}

		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
		for (int i = 0; i < data.populationSize; i++) {

			if (data.usePersonProperties) {
				StopwatchManager.start(Watch.PSLC_PERSON_PROPERTIES);
				for (int j = 0; j < data.initializedPersonPropertyCount; j++) {
					Pair<PersonPropertyId, Class<?>> pair = personPropertyToTypeMap.get(j);
					PersonPropertyId personPropertyId = pair.getFirst();
					Class<?> c = pair.getSecond();
					Object randomPropertyValue = getRandomPropertyValue(c);
					personBuilder.add(new PersonPropertyInitialization(personPropertyId, randomPropertyValue));
				}
				StopwatchManager.stop(Watch.PSLC_PERSON_PROPERTIES);
			}

			if (regionIds != null) {
				StopwatchManager.start(Watch.PSLC_REGIONS);
				RegionId regionId = regionIds.get(state.randomGenerator.nextInt(regionIds.size()));
				personBuilder.add(regionId);
				StopwatchManager.stop(Watch.PSLC_REGIONS);
			}

			if (resourceIds != null) {
				StopwatchManager.start(Watch.PSLC_RESOURCES);
				for (int j = 0; j < data.initializedResourceCount; j++) {
					ResourceId resourceId = resourceIds.get(j);
					long amount = state.randomGenerator.nextInt(1000) + 1;
					ResourceInitialization resourceInitialization = new ResourceInitialization(resourceId, amount);
					personBuilder.add(resourceInitialization);
				}
				StopwatchManager.stop(Watch.PSLC_RESOURCES);
			}

			PersonConstructionData personConstructionData = personBuilder.build();
			peopleDataManager.addPerson(personConstructionData);
		}

		if (data.useGroups) {
			StopwatchManager.start(Watch.PSLC_GROUPS);
			GroupsDataManager groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * data.populationSize);
			int activeWorkerCount = (int) (data.activeWorkerProportion * (data.populationSize - schoolAgedChildrenCount));

			int groupMembershipCount = 0;

			// put people in homes
			List<GroupId> groups = groupsDataManager.getGroupsForGroupType(LocalGroupType.HOME);
			if (groups.size() > 0) {
				for (int i = 0; i < people.size(); i++) {
					PersonId personId = people.get(i);
					int groupIndex = state.randomGenerator.nextInt(groups.size());
					GroupId groupId = groups.get(groupIndex);
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupMembershipCount++;
				}
			}

			// put people in work places
			groups = groupsDataManager.getGroupsForGroupType(LocalGroupType.WORK);
			if (groups.size() > 0) {
				for (int i = 0; i < activeWorkerCount; i++) {
					PersonId personId = people.get(i);
					int groupIndex = state.randomGenerator.nextInt(groups.size());
					GroupId groupId = groups.get(groupIndex);
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupMembershipCount++;
				}
			}

			// put people in schools
			groups = groupsDataManager.getGroupsForGroupType(LocalGroupType.SCHOOL);
			if (groups.size() > 0) {
				for (int i = 0; i < schoolAgedChildrenCount; i++) {
					PersonId personId = people.get(i + activeWorkerCount);
					int groupIndex = state.randomGenerator.nextInt(groups.size());
					GroupId groupId = groups.get(groupIndex);
					groupsDataManager.addPersonToGroup(personId, groupId);
					groupMembershipCount++;
				}
			}

			System.out.println("groupMembershipCount = " + groupMembershipCount);

			StopwatchManager.stop(Watch.PSLC_GROUPS);
		}

	}

	private void loadPeopleForBulkPopLoader(ActorContext actorContext) {

		if (data.loadMode != LoadMode.BULK) {
			return;
		}
		StopwatchManager.start(Watch.PBLC_PERSON_PROPERTIES);
		List<Pair<PersonPropertyId, Class<?>>> personPropertyToTypeMap = new ArrayList<>();
		if (data.usePersonProperties) {
			PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
			for (PersonPropertyId personPropertyId : personPropertiesDataManager.getPersonPropertyIds()) {
				PropertyDefinition personPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				personPropertyToTypeMap.add(new Pair<>(personPropertyId, personPropertyDefinition.getType()));
			}
		}
		StopwatchManager.stop(Watch.PBLC_PERSON_PROPERTIES);

		List<RegionId> regionIds = null;
		if (data.useRegions) {
			StopwatchManager.start(Watch.PBLC_REGIONS);
			RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
			regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
			StopwatchManager.stop(Watch.PBLC_REGIONS);
		}

		List<ResourceId> resourceIds = null;
		if (data.useResources) {
			StopwatchManager.start(Watch.PBLC_RESOURCES);
			ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
			resourceIds = new ArrayList<>(resourcesDataManager.getResourceIds());
			StopwatchManager.stop(Watch.PBLC_RESOURCES);
		}

		PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
		for (int i = 0; i < data.populationSize; i++) {

			if (data.usePersonProperties) {
				StopwatchManager.start(Watch.PBLC_PERSON_PROPERTIES);
				for (int j = 0; j < data.initializedPersonPropertyCount; j++) {
					Pair<PersonPropertyId, Class<?>> pair = personPropertyToTypeMap.get(j);
					PersonPropertyId personPropertyId = pair.getFirst();
					Class<?> c = pair.getSecond();
					Object randomPropertyValue = getRandomPropertyValue(c);
					personBuilder.add(new PersonPropertyInitialization(personPropertyId, randomPropertyValue));
				}
				StopwatchManager.stop(Watch.PBLC_PERSON_PROPERTIES);
			}

			if (regionIds != null) {
				StopwatchManager.start(Watch.PBLC_REGIONS);
				RegionId regionId = regionIds.get(state.randomGenerator.nextInt(regionIds.size()));
				personBuilder.add(regionId);
				StopwatchManager.stop(Watch.PBLC_REGIONS);
			}

			if (resourceIds != null) {
				StopwatchManager.start(Watch.PBLC_RESOURCES);
				for (int j = 0; j < data.initializedResourceCount; j++) {
					ResourceId resourceId = resourceIds.get(j);
					long amount = state.randomGenerator.nextInt(1000) + 1;
					ResourceInitialization resourceInitialization = new ResourceInitialization(resourceId, amount);
					personBuilder.add(resourceInitialization);
				}
				StopwatchManager.stop(Watch.PBLC_RESOURCES);
			}
			state.bulkPersonBuilder.add(personBuilder.build());
		}

		if (data.useGroups) {
			
			int groupMembershipCount = 0;
			StopwatchManager.start(Watch.PBLC_GROUPS);
			BulkGroupMembershipData.Builder bulkGroupMembershipBuilder = BulkGroupMembershipData.builder();

			int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * data.populationSize);
			int activeWorkerCount = (int) (data.activeWorkerProportion * (data.populationSize - schoolAgedChildrenCount));

			int householdCount = (int) ((double) data.populationSize / data.householdSize) + 1;
			int schoolCount = (int) ((double) schoolAgedChildrenCount / data.schoolSize) + 1;
			int workplaceCount = (int) ((double) activeWorkerCount / data.workplaceSize) + 1;

			// create the household groups
			for (int i = 0; i < householdCount; i++) {
				bulkGroupMembershipBuilder.addGroup(LocalGroupType.HOME);
			}

			// create the work groups
			for (int i = 0; i < workplaceCount; i++) {
				bulkGroupMembershipBuilder.addGroup(LocalGroupType.WORK);
			}

			// create the school groups
			for (int i = 0; i < schoolCount; i++) {
				bulkGroupMembershipBuilder.addGroup(LocalGroupType.SCHOOL);
			}

			// put people in homes
			for (int personId = 0; personId < data.populationSize; personId++) {
				int groupId = state.randomGenerator.nextInt(householdCount);
				bulkGroupMembershipBuilder.addPersonToGroup(personId, groupId);
				groupMembershipCount++;
			}

			// put people in work places
			for (int personId = 0; personId < activeWorkerCount; personId++) {
				int groupId = state.randomGenerator.nextInt(workplaceCount) + householdCount;
				bulkGroupMembershipBuilder.addPersonToGroup(personId, groupId);
				groupMembershipCount++;
			}

			// put people in schools
			for (int i = 0; i < schoolAgedChildrenCount; i++) {
				int personId = i + activeWorkerCount;
				int groupId = state.randomGenerator.nextInt(schoolCount) + householdCount + workplaceCount;
				bulkGroupMembershipBuilder.addPersonToGroup(personId, groupId);
				groupMembershipCount++;
			}

			if (data.useGroupProperties) {
				GroupsDataManager groupsDataManager = actorContext.getDataManager(GroupsDataManager.class);
				Set<GroupPropertyId> groupPropertyIds = groupsDataManager.getGroupPropertyIds(LocalGroupType.HOME);
				for (int i = 0; i < householdCount; i++) {
					for (GroupPropertyId groupPropertyId : groupPropertyIds) {
						Class<?> type = groupsDataManager.getGroupPropertyDefinition(LocalGroupType.HOME, groupPropertyId).getType();
						Object randomPropertyValue = getRandomPropertyValue(type);
						bulkGroupMembershipBuilder.setGroupPropertyValue(i, groupPropertyId, randomPropertyValue);
					}
				}
			}

			BulkGroupMembershipData bulkGroupMembershipData = bulkGroupMembershipBuilder.build();
			state.bulkPersonBuilder.addAuxiliaryData(bulkGroupMembershipData);
			StopwatchManager.stop(Watch.PBLC_GROUPS);
			
			System.out.println("groupMembershipCount = "+groupMembershipCount);
		}

	}

	public void execute() {
		state = new State();

		state.randomGenerator = RandomGeneratorProvider.getRandomGenerator(data.seed);

		testConsumer((c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			switch (data.loadMode) {
			case BULK:

				StopwatchManager.start(Watch.PBLC_TOTAL);

				loadPeopleForBulkPopLoader(c);

				BulkPersonConstructionData bulkPersonConstructionData = state.bulkPersonBuilder.build();

				StopwatchManager.stop(Watch.PBLC_TOTAL);

				StopwatchManager.start(Watch.PBL_PROCESSING);
				peopleDataManager.addBulkPeople(bulkPersonConstructionData);

				StopwatchManager.stop(Watch.PBL_PROCESSING);
				break;
			case SINGULAR:
				StopwatchManager.start(Watch.PSLC_TOTAL);
				loadPeopleForSinglesPopLoader(c);
				StopwatchManager.stop(Watch.PSLC_TOTAL);
				break;
			default:
				// PLUGIN case handled by testConsumer() methods
				break;

			}
		});
	}

	private void testConsumer(Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(testPlugin);
	}

	private Object getRandomPropertyValue(Class<?> type) {
		if (type == Integer.class) {
			return state.randomGenerator.nextInt();
		}
		if (type == Boolean.class) {
			return state.randomGenerator.nextBoolean();
		}
		if (type == Float.class) {
			return state.randomGenerator.nextFloat();
		}
		if (type == Double.class) {
			return state.randomGenerator.nextDouble();
		}
		if (type == Long.class) {
			return state.randomGenerator.nextLong();
		}
		throw new ContractException(BulkTestError.UNKNOWN_PROPERTY_TYPE);

	}

	private PropertyDefinition getRandomPropertyDefinition() {
		int value = state.randomGenerator.nextInt(5);
		Class<?> type = null;
		switch (value) {
		case 0:
			type = Integer.class;
			break;
		case 1:
			type = Boolean.class;
			break;
		case 2:
			type = Float.class;
			break;
		case 3:
			type = Double.class;
			break;
		case 4:
			type = Long.class;
			break;
		}
		return PropertyDefinition.builder().setType(type).setDefaultValue(getRandomPropertyValue(type)).build();

	}

	private void loadPeoplePlugin() {
		StopwatchManager.start(Watch.PEOPLE_PLUGIN_DATA);

		if (data.loadMode == LoadMode.PLUGIN) {
			for (int i = 0; i < data.populationSize; i++) {
				state.people.add(new PersonId(i));
			}
		}

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : state.people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		StopwatchManager.stop(Watch.PEOPLE_PLUGIN_DATA);

		state.simulationBuilder.addPlugin(peoplePlugin);
	}

	private void defineGroupProperties() {
		if (!data.useGroupProperties) {
			return;
		}

		Map<LocalGroupPropertyId, PropertyDefinition> propMap = new LinkedHashMap<>();
		state.groupPropertyDefinitions.put(LocalGroupType.HOME, propMap);
		for (int i = 0; i < data.housePropertyCount; i++) {
			PropertyDefinition propertyDefinition = getRandomPropertyDefinition();
			LocalGroupPropertyId localGroupPropertyId = new LocalGroupPropertyId(LocalGroupType.HOME, i);
			state.groupBuilder.defineGroupProperty(LocalGroupType.HOME, localGroupPropertyId, propertyDefinition);
			propMap.put(localGroupPropertyId, propertyDefinition);
		}
		propMap = new LinkedHashMap<>();
		state.groupPropertyDefinitions.put(LocalGroupType.SCHOOL, propMap);
		for (int i = 0; i < data.schoolPropertyCount; i++) {
			PropertyDefinition propertyDefinition = getRandomPropertyDefinition();
			LocalGroupPropertyId localGroupPropertyId = new LocalGroupPropertyId(LocalGroupType.SCHOOL, i);
			state.groupBuilder.defineGroupProperty(LocalGroupType.SCHOOL, localGroupPropertyId, propertyDefinition);
			propMap.put(localGroupPropertyId, propertyDefinition);
		}

		propMap = new LinkedHashMap<>();
		state.groupPropertyDefinitions.put(LocalGroupType.WORK, propMap);
		for (int i = 0; i < data.workPropertyCount; i++) {
			PropertyDefinition propertyDefinition = getRandomPropertyDefinition();
			LocalGroupPropertyId localGroupPropertyId = new LocalGroupPropertyId(LocalGroupType.WORK, i);
			state.groupBuilder.defineGroupProperty(LocalGroupType.WORK, localGroupPropertyId, propertyDefinition);
			propMap.put(localGroupPropertyId, propertyDefinition);
		}

	}

	private void addGroupPluginMemberships() {
		if (data.loadMode != LoadMode.PLUGIN) {
			return;
		}
		int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * state.people.size());
		int activeWorkerCount = (int) (data.activeWorkerProportion * (state.people.size() - schoolAgedChildrenCount));

		int householdCount = (int) ((double) state.people.size() / data.householdSize) + 1;
		int schoolCount = (int) ((double) schoolAgedChildrenCount / data.schoolSize) + 1;
		int workplaceCount = (int) ((double) activeWorkerCount / data.workplaceSize) + 1;

		// segregate the people into school age children and workers
		List<PersonId> schoolAgedChildren = new ArrayList<>();
		List<PersonId> workers = new ArrayList<>();

		for (int i = 0; i < state.people.size(); i++) {
			PersonId personId = state.people.get(i);
			if (i < schoolAgedChildrenCount) {
				schoolAgedChildren.add(personId);
			} else if (i < schoolAgedChildrenCount + activeWorkerCount) {
				workers.add(personId);
			}
		}

		int masterGroupId = 0;

		// create the household groups
		List<GroupId> houseHoldGroups = new ArrayList<>();
		for (int i = 0; i < householdCount; i++) {
			GroupId groupId = new GroupId(masterGroupId++);
			houseHoldGroups.add(groupId);
			state.groupBuilder.addGroup(groupId, LocalGroupType.HOME);
		}

		// create the work groups
		List<GroupId> workGroups = new ArrayList<>();
		for (int i = 0; i < workplaceCount; i++) {
			GroupId groupId = new GroupId(masterGroupId++);
			workGroups.add(groupId);
			state.groupBuilder.addGroup(groupId, LocalGroupType.WORK);
		}

		// create the school groups
		List<GroupId> schoolGroups = new ArrayList<>();
		for (int i = 0; i < schoolCount; i++) {
			GroupId groupId = new GroupId(masterGroupId++);
			schoolGroups.add(groupId);
			state.groupBuilder.addGroup(groupId, LocalGroupType.SCHOOL);
		}

		// System.out.println("total people "+state.people.size());
		// System.out.println("householdCount "+householdCount);
		// System.out.println("worker count "+workers.size());
		// System.out.println("workplaceCount "+workplaceCount);
		// System.out.println("school aged children
		// "+schoolAgedChildren.size());
		// System.out.println("schoolCount "+schoolCount);

		// put people in homes
		for (PersonId personId : state.people) {
			GroupId groupId = houseHoldGroups.get(state.randomGenerator.nextInt(houseHoldGroups.size()));
			state.groupBuilder.addPersonToGroup(groupId, personId);
		}

		// put people in work places
		for (PersonId personId : workers) {
			GroupId groupId = workGroups.get(state.randomGenerator.nextInt(workGroups.size()));
			state.groupBuilder.addPersonToGroup(groupId, personId);
		}

		// put people in schools
		for (PersonId personId : schoolAgedChildren) {
			GroupId groupId = schoolGroups.get(state.randomGenerator.nextInt(schoolGroups.size()));
			state.groupBuilder.addPersonToGroup(groupId, personId);
		}
		if (data.useGroupProperties) {

			Map<LocalGroupPropertyId, PropertyDefinition> propMap = state.groupPropertyDefinitions.get(LocalGroupType.HOME);
			for (GroupId groupId : houseHoldGroups) {
				for (LocalGroupPropertyId localGroupPropertyId : propMap.keySet()) {
					PropertyDefinition propertyDefinition = propMap.get(localGroupPropertyId);
					Object randomPropertyValue = getRandomPropertyValue(propertyDefinition.getType());
					state.groupBuilder.setGroupPropertyValue(groupId, localGroupPropertyId, randomPropertyValue);
				}
			}
			propMap = state.groupPropertyDefinitions.get(LocalGroupType.WORK);
			for (GroupId groupId : workGroups) {
				for (LocalGroupPropertyId localGroupPropertyId : propMap.keySet()) {
					PropertyDefinition propertyDefinition = propMap.get(localGroupPropertyId);
					Object randomPropertyValue = getRandomPropertyValue(propertyDefinition.getType());
					state.groupBuilder.setGroupPropertyValue(groupId, localGroupPropertyId, randomPropertyValue);
				}
			}
			propMap = state.groupPropertyDefinitions.get(LocalGroupType.SCHOOL);
			for (GroupId groupId : schoolGroups) {
				for (LocalGroupPropertyId localGroupPropertyId : propMap.keySet()) {
					PropertyDefinition propertyDefinition = propMap.get(localGroupPropertyId);
					Object randomPropertyValue = getRandomPropertyValue(propertyDefinition.getType());
					state.groupBuilder.setGroupPropertyValue(groupId, localGroupPropertyId, randomPropertyValue);
				}
			}
		}

	}

	private void addGroupPluginTypes() {
		// add group types
		for (LocalGroupType localGroupType : LocalGroupType.values()) {
			state.groupBuilder.addGroupTypeId(localGroupType);
		}
	}

	private void loadGroupsPlugin() {
		if (!data.useGroups) {
			return;
		}

		StopwatchManager.start(Watch.GROUPS_PLUGIN_DATA);
		// add the group plugin
		addGroupPluginTypes();
		defineGroupProperties();
		addGroupPluginMemberships();

		GroupsPluginData groupsPluginData = state.groupBuilder.build();
		Plugin groupPlugin = GroupsPlugin.getGroupPlugin(groupsPluginData);
		StopwatchManager.stop(Watch.GROUPS_PLUGIN_DATA);
		state.simulationBuilder.addPlugin(groupPlugin);

	}

	private void loadPersonPropertiesPlugin() {
		if (!data.usePersonProperties) {
			return;
		}
		StopwatchManager.start(Watch.PERSON_PROPERTIES_PLUGIN_DATA);
		PersonPropertiesPluginData.Builder personPropertiesBuilder = PersonPropertiesPluginData.builder();
		List<PersonPropertyId> personPropertyIds = new ArrayList<>();
		List<Class<?>> types = new ArrayList<>();
		for (int i = 0; i < data.personPropertyCount; i++) {
			PersonPropertyId personPropertyId = new LocalPersonPropertyId(i);
			personPropertyIds.add(personPropertyId);
			PropertyDefinition propertyDefinition = getRandomPropertyDefinition();
			types.add(propertyDefinition.getType());
			personPropertiesBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		}

		if (data.loadMode == LoadMode.PLUGIN) {
			for (int i = 0; i < data.initializedPersonPropertyCount; i++) {
				PersonPropertyId personPropertyId = personPropertyIds.get(i);
				Class<?> type = types.get(i);
				for (PersonId personId : state.people) {
					Object randomPropertyValue = getRandomPropertyValue(type);
					personPropertiesBuilder.setPersonPropertyValue(personId, personPropertyId, randomPropertyValue);
				}
			}
		}

		PersonPropertiesPluginData personPropertiesPluginData = personPropertiesBuilder.build();
		Plugin personPropertiesPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
		StopwatchManager.stop(Watch.PERSON_PROPERTIES_PLUGIN_DATA);

		state.simulationBuilder.addPlugin(personPropertiesPlugin);

	}

	private void loadRegionsPlugin() {
		if (!data.useRegions) {
			return;
		}
		StopwatchManager.start(Watch.REGIONS_PLUGIN_DATA);
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		List<RegionId> regionIds = new ArrayList<>();
		for (int i = 0; i < data.regionCount; i++) {
			RegionId regionId = new LocalRegionId(i);
			regionsBuilder.addRegion(regionId);
			regionIds.add(regionId);
		}

		List<RegionPropertyId> regionPropertyIds = new ArrayList<>();
		List<Class<?>> types = new ArrayList<>();
		for (int i = 0; i < data.regionPropertyCount; i++) {
			RegionPropertyId regionPropertyId = new LocalRegionPropertyId(i);
			regionPropertyIds.add(regionPropertyId);
			PropertyDefinition propertyDefinition = getRandomPropertyDefinition();
			types.add(propertyDefinition.getType());
			regionsBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		}
		for (RegionId regionId : regionIds) {
			for (int i = 0; i < data.initializedRegionPropertyCount; i++) {
				RegionPropertyId regionPropertyId = regionPropertyIds.get(i);
				Class<?> type = types.get(i);
				Object randomPropertyValue = getRandomPropertyValue(type);
				regionsBuilder.setRegionPropertyValue(regionId, regionPropertyId, randomPropertyValue);
			}
		}
		for (PersonId personId : state.people) {
			int regionIndex = state.randomGenerator.nextInt(regionIds.size());
			RegionId regionId = regionIds.get(regionIndex);
			regionsBuilder.setPersonRegion(personId, regionId);
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);
		StopwatchManager.stop(Watch.REGIONS_PLUGIN_DATA);
		state.simulationBuilder.addPlugin(regionsPlugin);

	}

	private void loadResourcesPlugin() {
		if (!data.useResources) {
			return;
		}

		StopwatchManager.start(Watch.RESOURCES_PLUGIN_DATA);

		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

		List<ResourceId> resourceIds = new ArrayList<>();
		for (int i = 0; i < data.resourceCount; i++) {
			ResourceId resourceId = new LocalResourceId(i);
			resourceIds.add(resourceId);
			builder.addResource(resourceId);
		}

		if (data.loadMode == LoadMode.PLUGIN) {
			for (PersonId personId : state.people) {
				for (int i = 0; i < data.initializedResourceCount; i++) {
					ResourceId resourceId = resourceIds.get(i);
					long amount = state.randomGenerator.nextInt(1000) + 1;
					builder.setPersonResourceLevel(personId, resourceId, amount);
				}
			}
		}

		ResourcesPluginData resourcesPluginData = builder.build();
		Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(resourcesPluginData);
		state.simulationBuilder.addPlugin(resourcesPlugin);

		StopwatchManager.stop(Watch.RESOURCES_PLUGIN_DATA);
	}

	private void loadStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(state.randomGenerator.nextLong()).build();
		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		state.simulationBuilder.addPlugin(stochasticPlugin);
	}

	private void testConsumers(Plugin testPlugin) {
		loadPeoplePlugin();
		loadGroupsPlugin();
		loadPersonPropertiesPlugin();
		loadRegionsPlugin();
		loadResourcesPlugin();
		loadStochasticsPlugin();

		// add the action plugin
		state.simulationBuilder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		state.simulationBuilder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

}
