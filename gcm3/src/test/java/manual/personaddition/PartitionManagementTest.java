package manual.personaddition;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.Context;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.globals.GlobalPlugin;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.groups.GroupPlugin;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.materials.MaterialsPlugin;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionSampler;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.support.PersonPropertyLabeler;
import plugins.personproperties.support.PropertyFilter;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionFilter;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionLabeler;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.SeedProvider;
import util.TimeElapser;

public class PartitionManagementTest {
	

	private static enum LocalPersonPropertyId implements PersonPropertyId {

		AGE, IMMUNE, VACCINATED, SERUM_DENSITY;

		public PropertyDefinition getPropertyDefinition(TimeTrackingPolicy timeTrackingPolicy) {
			switch (this) {
			case AGE:

				return PropertyDefinition	.builder()//
											.setDefaultValue(0)//
											.setTimeTrackingPolicy(timeTrackingPolicy)//
											.setType(Integer.class)//
											.build();
			case IMMUNE:
				return PropertyDefinition	.builder()//
											.setDefaultValue(false)//
											.setTimeTrackingPolicy(timeTrackingPolicy)//
											.setType(Boolean.class)//
											.build();
			case SERUM_DENSITY:
				return PropertyDefinition	.builder()//
											.setDefaultValue(5.5)//
											.setTimeTrackingPolicy(timeTrackingPolicy)//
											.setType(Double.class)//
											.build();
			case VACCINATED:
				return PropertyDefinition	.builder()//
											.setDefaultValue(false)//
											.setTimeTrackingPolicy(timeTrackingPolicy)//
											.setType(Boolean.class)//
											.build();
			default:
				throw new RuntimeException("unhandled case " + this);

			}

		}

	}

	private static enum Phase {
		LOAD_POPULATION, LOAD_PARTITION, UPDATE_PARTITION, SAMPLE_PARTITION, MEASURE_MEMORY;
	}

	private static enum AgeGroup {
		CHILD, ADULT, SENIOR;
	}

	private Map<Integer, Integer> serumDensityLabels = new LinkedHashMap<>();

	private double getWeight(final Context context, final LabelSet labelSet) {
		return 1;
	}

	private Object getSerumDensityLabel(Object serumDensity) {
		Double sd = (Double) serumDensity;
		Integer label = (int) (sd * 1000);
		if (!serumDensityLabels.containsKey(label)) {
			serumDensityLabels.put(label, label);
		}
		return serumDensityLabels.get(label);
	}

	private Object getAgeLabel(Object age) {
		Integer a = (Integer) age;
		if (a < 20) {
			return AgeGroup.CHILD;
		}
		if (a < 50) {
			return AgeGroup.ADULT;
		}
		return AgeGroup.SENIOR;
	}

	private Object getRegionLabel(RegionId regionId) {
		return regionId;
	}

	/**
	 * 1_000_000 people
	 * 
	 * 1 partition -- filtering on 2 props, partitioning on one of those and yet
	 * another into several hundred cells
	 * 
	 * demonstrate the use of ARRAY mapping vs NONE against pre and post
	 * population loading relative to the partition
	 * 
	 * demonstrate the execution of several million random updates to the
	 * partition's contents
	 * 
	 * compare total runtime for each combination
	 * 
	 * popsize,density, pop loading order, array vs none, filter vs no filter
	 * 
	 */

	public static void main(String[] args) {
		new PartitionManagementTest().execute();
	}

	private void execute() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(558651572605439911L);

		int populationSize = 1_000_000;
		boolean measureMemory = false;

		List<Boolean> populationFirstList = generateBooleanList();
		List<Boolean> useArrayList = generateBooleanList();
		List<Boolean> useFilterList = generateBooleanList();

		System.out.println(Report.toHeader());
		for (Boolean loadPopulationFirst : populationFirstList) {
			for (Boolean useArray : useArrayList) {
				for (Boolean useFilter : useFilterList) {
					Report report = testInternal(randomGenerator.nextLong(), populationSize, loadPopulationFirst, useArray, useFilter, measureMemory);
					System.out.println(report.toString());
				}
			}

		}

	}

	private static List<Boolean> generateBooleanList() {
		List<Boolean> result = new ArrayList<>();
		result.add(false);
		result.add(true);
		return result;
	}

	private static class Report {

		int populationSize;
		boolean loadPopulationFirst;
		boolean useFilter;
		double partitionLoadTime;
		int partitionSize;
		double populationLoadTime;
		double partitionUpdateTime;
		int partitionUpdateCount;
		double partitionSampleTime;
		int partitionSampleCount;

		public void setPopulationSize(int populationSize) {
			this.populationSize = populationSize;
		}

		public void setLoadPopulationFirst(boolean loadPopulationFirst) {
			this.loadPopulationFirst = loadPopulationFirst;
		}

		public void setUseFilter(boolean useFilter) {
			this.useFilter = useFilter;
		}

		public void setPartitionLoadTime(double partitionLoadTime) {
			this.partitionLoadTime = partitionLoadTime;
		}

		public void setPartitionSize(int partitionSize) {
			this.partitionSize = partitionSize;
		}

		public void setPopulationLoadTime(double populationLoadTime) {
			this.populationLoadTime = populationLoadTime;
		}

		public void setPartitionUpdateTime(double partitionUpdateTime) {
			this.partitionUpdateTime = partitionUpdateTime;
		}

		public void setPartitionUpdateCount(int partitionUpdateCount) {
			this.partitionUpdateCount = partitionUpdateCount;
		}

		public void setPartitionSampleTime(double partitionSampleTime) {
			this.partitionSampleTime = partitionSampleTime;
		}

		public void setPartitionSampleCount(int partitionSampleCount) {
			this.partitionSampleCount = partitionSampleCount;
		}

		public static String toHeader() {
			StringBuilder sb = new StringBuilder();
			sb.append("population");
			sb.append("\t");
			sb.append("population first");
			sb.append("\t");
			sb.append("filtered");
			sb.append("\t");
			sb.append("partition load time");
			sb.append("\t");
			sb.append("partition size");
			sb.append("\t");
			sb.append("population load time");
			sb.append("\t");
			sb.append("partition update time");
			sb.append("\t");
			sb.append("partition update count");
			sb.append("\t");
			sb.append("partition sample time");
			sb.append("\t");
			sb.append("partition sample count");
			sb.append("\t");
			sb.append("total time");

			return sb.toString();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(populationSize);
			sb.append("\t");
			sb.append(loadPopulationFirst);
			sb.append("\t");
			sb.append(useFilter);
			sb.append("\t");
			sb.append(partitionLoadTime);
			sb.append("\t");
			sb.append(partitionSize);
			sb.append("\t");
			sb.append(populationLoadTime);
			sb.append("\t");
			sb.append(partitionUpdateTime);
			sb.append("\t");
			sb.append(partitionUpdateCount);
			sb.append("\t");
			sb.append(partitionSampleTime);
			sb.append("\t");
			sb.append(partitionSampleCount);
			double totalTime = partitionLoadTime + populationLoadTime + partitionUpdateTime + partitionSampleTime;
			sb.append("\t");
			sb.append(totalTime);
			return sb.toString();
		}

	}

	private static class LocalRegionId implements RegionId {
		private final int id;

		public LocalRegionId(int id) {
			this.id = id;
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

	private Report testInternal(//
			Long seed, //
			int populationSize, //
			boolean loadPopulationFirst, //
			boolean useArray, //
			boolean useFilter, //
			boolean measureMemory) {//

		Builder builder = Simulation.builder();

		Report report = new Report();
		report.setPopulationSize(populationSize);
		report.setLoadPopulationFirst(loadPopulationFirst);
		report.setUseFilter(useFilter);

		Map<Phase, Double> phaseTimeMap = new LinkedHashMap<>();
		if (loadPopulationFirst) {
			phaseTimeMap.put(Phase.LOAD_POPULATION, 1.0);
			phaseTimeMap.put(Phase.LOAD_PARTITION, 2.0);
		} else {
			phaseTimeMap.put(Phase.LOAD_POPULATION, 2.0);
			phaseTimeMap.put(Phase.LOAD_PARTITION, 1.0);
		}
		phaseTimeMap.put(Phase.UPDATE_PARTITION, 3.0);
		phaseTimeMap.put(Phase.SAMPLE_PARTITION, 4.0);
		phaseTimeMap.put(Phase.MEASURE_MEMORY, 5.0);

		// add the compartment plugin
		CompartmentInitialData.Builder compartmentBuilder = CompartmentInitialData.builder();
		compartmentBuilder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}
		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

		// add the global plugin

		GlobalInitialData.Builder globalBuilder = GlobalInitialData.builder();

		for (final TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			globalBuilder.setGlobalComponentInitialBehaviorSupplier(testGlobalComponentId, () -> new ActionAgent(testGlobalComponentId)::init);
		}
		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(globalBuilder.build())::init);

		// add the materials plugin
		MaterialsInitialData.Builder materialsBuilder = MaterialsInitialData.builder();

		for (final TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (final TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new ActionAgent(testMaterialsProducerId)::init);
		}
		builder.addPlugin(MaterialsPlugin.PLUGIN_ID, new MaterialsPlugin(materialsBuilder.build())::init);

		// add the resources plugin
		ResourceInitialData.Builder resourceBuilder = ResourceInitialData.builder();

		for (final TestResourceId testResourceId : TestResourceId.values()) {
			resourceBuilder.addResource(testResourceId);
			resourceBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}
		builder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourceBuilder.build())::init);

		// add the groups plugin
		GroupInitialData.Builder groupsBuilder = GroupInitialData.builder();
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupsBuilder.addGroupTypeId(testGroupTypeId);
		}
		builder.addPlugin(GroupPlugin.PLUGIN_ID, new GroupPlugin(groupsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		int regionCount = populationSize / 5000;
		regionsBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		List<RegionId> regionIds = new ArrayList<>();
		for (int i = 0; i < regionCount; i++) {
			LocalRegionId localRegionId = new LocalRegionId(i);
			regionIds.add(localRegionId);
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(localRegionId, () -> new ActionAgent(localRegionId)::init);
		}

		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

		// load the person properties plugin
		PersonPropertyInitialData.Builder personPropertiesBuilder = PersonPropertyInitialData.builder();
		for (LocalPersonPropertyId localPersonPropertyId : LocalPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = localPersonPropertyId.getPropertyDefinition(TimeTrackingPolicy.DO_NOT_TRACK_TIME);
			personPropertiesBuilder.definePersonProperty(localPersonPropertyId, propertyDefinition);
		}
		builder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID, new PersonPropertiesPlugin(personPropertiesBuilder.build())::init);

		// load the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);
		
		//load the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		
		//load the people plugin
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		
		//load the properties plugin		
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		
		//load the report plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		
		//load the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		
		Object partitionId = new Object();

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// load population
		pluginBuilder.addAgentActionPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, new AgentActionPlan(phaseTimeMap.get(Phase.LOAD_POPULATION), (c) -> {
			TimeElapser timeElapser = new TimeElapser();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();

			for (int i = 0; i < populationSize; i++) {
				PersonContructionData.Builder constructionBuilder = PersonContructionData.builder();
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				CompartmentId compartmentId = TestCompartmentId.getRandomCompartmentId(randomGenerator);

				constructionBuilder.add(new PersonPropertyInitialization(LocalPersonPropertyId.AGE, randomGenerator.nextInt(60)));
				constructionBuilder.add(new PersonPropertyInitialization(LocalPersonPropertyId.IMMUNE, randomGenerator.nextBoolean()));
				constructionBuilder.add(new PersonPropertyInitialization(LocalPersonPropertyId.VACCINATED, randomGenerator.nextBoolean()));
				constructionBuilder.add(new PersonPropertyInitialization(LocalPersonPropertyId.SERUM_DENSITY, randomGenerator.nextDouble() * 0.9 + 0.1));
				constructionBuilder.add(regionId);
				constructionBuilder.add(compartmentId);
				PersonContructionData personContructionData = constructionBuilder.build();

				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}

			report.setPopulationLoadTime(timeElapser.getElapsedMilliSeconds());

		}));

		// load partition
		pluginBuilder.addAgentActionPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, new AgentActionPlan(phaseTimeMap.get(Phase.LOAD_PARTITION), (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			Set<RegionId> regionsForFilter = new LinkedHashSet<>();
			for (RegionId regionId : regionIds) {
				if (randomGenerator.nextBoolean()) {
					regionsForFilter.add(regionId);
				}
			}

			TimeElapser timeElapser = new TimeElapser();
			Filter filter;
			if (useFilter) {
				filter = new PropertyFilter(LocalPersonPropertyId.AGE, Equality.GREATER_THAN, 15)	.and(new PropertyFilter(LocalPersonPropertyId.IMMUNE, Equality.EQUAL, false))
																									.and(new PropertyFilter(LocalPersonPropertyId.VACCINATED, Equality.EQUAL, false))
																									.and(new RegionFilter(regionIds.get(randomGenerator.nextInt(regionIds.size()))));
			} else {
				filter = Filter.allPeople();
			}

			Partition partition = Partition	.builder()//
											.setFilter(filter)//
											.addLabeler(new PersonPropertyLabeler(LocalPersonPropertyId.AGE, this::getAgeLabel))//
											.addLabeler(new PersonPropertyLabeler(LocalPersonPropertyId.SERUM_DENSITY, this::getSerumDensityLabel))//
											.addLabeler(new RegionLabeler(this::getRegionLabel))//
											.build();//

			if (loadPopulationFirst && useArray && useFilter) {
				c.resolveEvent(new PartitionAdditionEvent(partition, partitionId));

			} else {
				c.resolveEvent(new PartitionAdditionEvent(partition, partitionId));

			}

			report.setPartitionLoadTime(timeElapser.getElapsedMilliSeconds());
			report.setPartitionSize(partitionDataView.getPersonCount(partitionId));

		}));

		// update the property values

		pluginBuilder.addAgentActionPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, new AgentActionPlan(phaseTimeMap.get(Phase.UPDATE_PARTITION), (c) -> {

			int count = 0;
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();

			TimeElapser timeElapser = new TimeElapser();
			List<PersonId> people = personDataView.getPeople();

			for (PersonId personId : people) {
				Boolean vaccinated = personPropertyDataView.getPersonPropertyValue(personId, LocalPersonPropertyId.VACCINATED);
				if (!vaccinated && randomGenerator.nextDouble() < 0.3) {
					c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, LocalPersonPropertyId.VACCINATED, true));
					count++;
				}

				if (randomGenerator.nextDouble() < 0.25) {

					Double serumDensity = personPropertyDataView.getPersonPropertyValue(personId, LocalPersonPropertyId.SERUM_DENSITY);
					c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, LocalPersonPropertyId.SERUM_DENSITY, serumDensity / 2));
					count++;
				}
			}

			report.setPartitionUpdateTime(timeElapser.getElapsedMilliSeconds());
			report.setPartitionUpdateCount(count);

		}));

		// take samples from the partition
		pluginBuilder.addAgentActionPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, new AgentActionPlan(phaseTimeMap.get(Phase.SAMPLE_PARTITION), (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			TimeElapser timeElapser = new TimeElapser();

			int sampleRepetitionCount = 1000;
			for (int i = 0; i < sampleRepetitionCount; i++) {
				for (AgeGroup ageGroup : AgeGroup.values()) {
					for (Integer serumDensityLabel : serumDensityLabels.keySet()) {

						RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));

						LabelSet labelSet = LabelSet.builder()//
													.setLabel(LocalPersonPropertyId.AGE, ageGroup)//
													.setLabel(LocalPersonPropertyId.SERUM_DENSITY, serumDensityLabel)//
													.setLabel(RegionId.class, regionId).build();//

						PartitionSampler partitionSampler = PartitionSampler.builder()//
																			.setLabelSet(labelSet)//
																			.setLabelSetWeightingFunction(this::getWeight)//
																			.build();//

						partitionDataView.samplePartition(partitionId, partitionSampler);
					}
				}
			}
			int numberOfSamples = sampleRepetitionCount;
			numberOfSamples *= AgeGroup.values().length;
			numberOfSamples *= serumDensityLabels.size();
			report.setPartitionSampleTime(timeElapser.getElapsedMilliSeconds());
			report.setPartitionSampleCount(numberOfSamples);

		}));

		// measure the size of the partition
		pluginBuilder.addAgentActionPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, new AgentActionPlan(phaseTimeMap.get(Phase.MEASURE_MEMORY), (c) -> {

			if (measureMemory) {
				// long memSizeOfPartition = ((EnvironmentImpl)
				// environment).getMemSizeOfPartition(partitionId);
				// System.out.println("memSizeOfPartition = " +
				// memSizeOfPartition);
			}
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();
		assertTrue(actionPlugin.allActionsExecuted());

		return report;
	}

}
