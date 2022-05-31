package temp;

import java.util.ArrayList;
import java.util.List;
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
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupId;
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
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.errors.ContractError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.time.StopwatchManager;
import util.time.Watch;

public class MT_Bulk {

	private static enum LocalGroupType implements GroupTypeId {
		SCHOOL, WORK, HOME;
	}

	private static class Data {
		private long seed;

		// people
		private boolean loadPeopleInPlugins;
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

		// regions
		private boolean useRegions;
		private int regionPropertyCount;
		private int initializedRegionPropertyCount;
		private int regionCount;

		// resources
		private boolean useResources;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("seed");
			builder.append("\t");
			builder.append(seed);
			builder.append("\n");

			builder.append("loadPeopleInPlugins");
			builder.append("\t");
			builder.append(loadPeopleInPlugins);
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

		NEGATIVE_PROPERTY_COUNT("Negative property count"), //
		BAD_INIT_PROPERTY_COUNT("The  number of properties to initialize exceeds the total number of  properties"), //
		UNKNOWN_PROPERTY_TYPE("Unknown property type"), //
		NEGATIVE_POPULATION_SIZE("A population size is negative"), //
		ILLEGAL_GROUP_SIZE("A group size is either negative or is zero with groups active"),//

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

		public Builder setLoadPeopleInPlugins(boolean loadPeopleInPlugins) {
			data.loadPeopleInPlugins = loadPeopleInPlugins;
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

	}

	private final Data data;
	private State state;

	private MT_Bulk(Data data) {
		this.data = data;
	}

	private void loadPeople(ActorContext actorContext) {

		if (data.loadPeopleInPlugins) {
			return;
		}
		StopwatchManager.start(Watch.PLC_PERSON_PROPERTIES);
		List<Pair<PersonPropertyId, Class<?>>> initProperties = new ArrayList<>();
		if (data.usePersonProperties) {
			PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
			for (PersonPropertyId personPropertyId : personPropertiesDataManager.getPersonPropertyIds()) {
				PropertyDefinition personPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				initProperties.add(new Pair<>(personPropertyId, personPropertyDefinition.getType()));
			}
		}
		StopwatchManager.stop(Watch.PLC_PERSON_PROPERTIES);

		List<RegionId> regionIds = null;
		if (data.useRegions) {
			StopwatchManager.start(Watch.PLC_REGIONS);
			RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
			regionIds = new ArrayList<>(regionsDataManager.getRegionIds());
			StopwatchManager.stop(Watch.PLC_REGIONS);
		}

		PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
		for (int i = 0; i < data.populationSize; i++) {

			if (data.usePersonProperties) {
				StopwatchManager.start(Watch.PLC_PERSON_PROPERTIES);
				for (int j = 0; j < data.initializedPersonPropertyCount; j++) {
					Pair<PersonPropertyId, Class<?>> pair = initProperties.get(j);
					PersonPropertyId personPropertyId = pair.getFirst();
					Class<?> c = pair.getSecond();
					Object randomPropertyValue = getRandomPropertyValue(c);
					personBuilder.add(new PersonPropertyInitialization(personPropertyId, randomPropertyValue));
				}
				StopwatchManager.stop(Watch.PLC_PERSON_PROPERTIES);
			}
			
			if (regionIds != null) {
				StopwatchManager.start(Watch.PLC_REGIONS);
				RegionId regionId = regionIds.get(state.randomGenerator.nextInt(regionIds.size()));
				personBuilder.add(regionId);
				StopwatchManager.stop(Watch.PLC_REGIONS);
			}
			state.bulkPersonBuilder.add(personBuilder.build());
		}

		if (data.useGroups) {
			StopwatchManager.start(Watch.PLC_GROUPS);
			BulkGroupMembershipData.Builder bulkGroupBuilder = BulkGroupMembershipData.builder();

			int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * data.populationSize);
			int activeWorkerCount = (int) (data.activeWorkerProportion * (data.populationSize - schoolAgedChildrenCount));

			int householdCount = (int) ((double) data.populationSize / data.householdSize) + 1;
			int schoolCount = (int) ((double) schoolAgedChildrenCount / data.schoolSize) + 1;
			int workplaceCount = (int) ((double) activeWorkerCount / data.workplaceSize) + 1;

			// create the household groups
			for (int i = 0; i < householdCount; i++) {
				bulkGroupBuilder.addGroup(LocalGroupType.HOME);
			}

			// create the work groups
			for (int i = 0; i < workplaceCount; i++) {
				bulkGroupBuilder.addGroup(LocalGroupType.WORK);
			}

			// create the school groups
			for (int i = 0; i < schoolCount; i++) {
				bulkGroupBuilder.addGroup(LocalGroupType.SCHOOL);
			}

			// put people in homes
			for (int personId = 0; personId < data.populationSize; personId++) {
				int groupId = state.randomGenerator.nextInt(householdCount);
				bulkGroupBuilder.addPersonToGroup(personId, groupId);
			}

			// put people in work places
			for (int personId = 0; personId < activeWorkerCount; personId++) {
				int groupId = state.randomGenerator.nextInt(workplaceCount) + householdCount;
				bulkGroupBuilder.addPersonToGroup(personId, groupId);
			}

			// put people in schools
			for (int i = 0; i < schoolAgedChildrenCount; i++) {
				int personId = i + activeWorkerCount;
				int groupId = state.randomGenerator.nextInt(schoolCount) + householdCount + workplaceCount;
				bulkGroupBuilder.addPersonToGroup(personId, groupId);
			}
			BulkGroupMembershipData bulkGroupMembershipData = bulkGroupBuilder.build();
			state.bulkPersonBuilder.addAuxiliaryData(bulkGroupMembershipData);
			StopwatchManager.stop(Watch.PLC_GROUPS);
		}

	}

	public void execute() {
		state = new State();

		state.randomGenerator = RandomGeneratorProvider.getRandomGenerator(data.seed);

		testConsumer((c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			StopwatchManager.start(Watch.POP_LOADER_CONSTRUCTION);

			loadPeople(c);

			BulkPersonConstructionData bulkPersonConstructionData = state.bulkPersonBuilder.build();

			StopwatchManager.stop(Watch.POP_LOADER_CONSTRUCTION);

			StopwatchManager.start(Watch.POP_LOADER_PROCESSING);
			peopleDataManager.addBulkPeople(bulkPersonConstructionData);

			StopwatchManager.stop(Watch.POP_LOADER_PROCESSING);

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
		case 1:
			type = Boolean.class;
		case 2:
			type = Float.class;
		case 3:
			type = Double.class;
		case 4:
			type = Long.class;
		}
		return PropertyDefinition.builder().setType(type).setDefaultValue(getRandomPropertyValue(type)).build();

	}

	private void testConsumers(Plugin testPlugin) {

		// create a list of people

		StopwatchManager.start(Watch.PEOPLE_PLUGIN_DATA);
		List<PersonId> people = new ArrayList<>();
		if (data.loadPeopleInPlugins) {
			for (int i = 0; i < data.populationSize; i++) {
				people.add(new PersonId(i));
			}
		}
		StopwatchManager.stop(Watch.PEOPLE_PLUGIN_DATA);

		Simulation.Builder builder = Simulation.builder();

		if (data.useGroups) {
			StopwatchManager.start(Watch.GROUPS_PLUGIN_DATA);
			// add the group plugin
			GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();

			// add group types
			for (LocalGroupType localGroupType : LocalGroupType.values()) {
				groupBuilder.addGroupTypeId(localGroupType);
			}

			if (data.loadPeopleInPlugins) {
				int schoolAgedChildrenCount = (int) (data.schoolAgeProportion * people.size());
				int activeWorkerCount = (int) (data.activeWorkerProportion * (people.size() - schoolAgedChildrenCount));

				int householdCount = (int) ((double) people.size() / data.householdSize) + 1;
				int schoolCount = (int) ((double) schoolAgedChildrenCount / data.schoolSize) + 1;
				int workplaceCount = (int) ((double) activeWorkerCount / data.workplaceSize) + 1;

				// segregate the people into school age children and workers
				List<PersonId> schoolAgedChildren = new ArrayList<>();
				List<PersonId> workers = new ArrayList<>();

				for (int i = 0; i < people.size(); i++) {
					PersonId personId = people.get(i);
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
					groupBuilder.addGroup(groupId, LocalGroupType.HOME);
				}

				// create the work groups
				List<GroupId> workGroups = new ArrayList<>();
				for (int i = 0; i < workplaceCount; i++) {
					GroupId groupId = new GroupId(masterGroupId++);
					workGroups.add(groupId);
					groupBuilder.addGroup(groupId, LocalGroupType.WORK);
				}

				// create the school groups
				List<GroupId> schoolGroups = new ArrayList<>();
				for (int i = 0; i < schoolCount; i++) {
					GroupId groupId = new GroupId(masterGroupId++);
					schoolGroups.add(groupId);
					groupBuilder.addGroup(groupId, LocalGroupType.SCHOOL);
				}

				// put people in homes
				for (PersonId personId : people) {
					GroupId groupId = houseHoldGroups.get(state.randomGenerator.nextInt(houseHoldGroups.size()));
					groupBuilder.addPersonToGroup(groupId, personId);
				}

				// put people in work places
				for (PersonId personId : workers) {
					GroupId groupId = workGroups.get(state.randomGenerator.nextInt(workGroups.size()));
					groupBuilder.addPersonToGroup(groupId, personId);
				}

				// put people in schools
				for (PersonId personId : schoolAgedChildren) {
					GroupId groupId = schoolGroups.get(state.randomGenerator.nextInt(schoolGroups.size()));
					groupBuilder.addPersonToGroup(groupId, personId);
				}
			}

			// define group properties
			// for (TestGroupPropertyId testGroupPropertyId :
			// TestGroupPropertyId.values()) {
			// groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(),
			// testGroupPropertyId,
			// testGroupPropertyId.getPropertyDefinition());
			// }

			GroupsPluginData groupsPluginData = groupBuilder.build();
			Plugin groupPlugin = GroupsPlugin.getGroupPlugin(groupsPluginData);
			StopwatchManager.stop(Watch.GROUPS_PLUGIN_DATA);
			builder.addPlugin(groupPlugin);
		}

		// add the people plugin

		StopwatchManager.start(Watch.PEOPLE_PLUGIN_DATA);
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		StopwatchManager.stop(Watch.PEOPLE_PLUGIN_DATA);

		builder.addPlugin(peoplePlugin);

		// add the people properties plugin

		if (data.usePersonProperties) {
			StopwatchManager.start(Watch.PERSON_PROPERTIES_PLUGIN_DATA);
			PersonPropertiesPluginData.Builder personPropertiesBuilder = PersonPropertiesPluginData.builder();
			for (int i = 0; i < data.personPropertyCount; i++) {
				PersonPropertyId personPropertyId = new LocalPersonPropertyId(i);
				personPropertiesBuilder.definePersonProperty(personPropertyId, getRandomPropertyDefinition());
			}
			PersonPropertiesPluginData personPropertiesPluginData = personPropertiesBuilder.build();
			Plugin personPropertiesPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
			StopwatchManager.stop(Watch.PERSON_PROPERTIES_PLUGIN_DATA);

			builder.addPlugin(personPropertiesPlugin);
		}

		// add the regions plugin
		if (data.useRegions) {
			StopwatchManager.start(Watch.REGIONS_PLUGIN_DATA);
			RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
			List<RegionId> regionIds = new ArrayList<>();
			for (int i = 0; i < data.regionCount; i++) {
				RegionId regionId = new LocalRegionId(i);
				regionsBuilder.addRegion(regionId);
				regionIds.add(regionId);
			}

			for (PersonId personId : people) {
				int regionIndex = state.randomGenerator.nextInt(regionIds.size());
				RegionId regionId = regionIds.get(regionIndex);
				regionsBuilder.setPersonRegion(personId, regionId);
			}
			RegionsPluginData regionsPluginData = regionsBuilder.build();
			Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);
			StopwatchManager.stop(Watch.REGIONS_PLUGIN_DATA);
			builder.addPlugin(regionsPlugin);
		}
		StopwatchManager.start(Watch.RESOURCES_PLUGIN_DATA);
		StopwatchManager.stop(Watch.RESOURCES_PLUGIN_DATA);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(state.randomGenerator.nextLong()).build();
		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticPlugin);

		// add the action plugin
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

}
