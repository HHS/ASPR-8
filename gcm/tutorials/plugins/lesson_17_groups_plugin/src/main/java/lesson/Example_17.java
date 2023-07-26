package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.Region;
import lesson.plugins.model.support.SchoolStatus;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentParameterData;
import nucleus.FunctionalDimension;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.GroupsPlugin;
import plugins.groups.datamanagers.GroupsPluginData;
import plugins.groups.reports.GroupPopulationReportPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_17 {

	private final Path outputDirectory;

	private Example_17(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.GROUP_POPULATON, outputDirectory.resolve("group_population_report.xls"))//
				.addReport(ModelReportLabel.PERSON_PROPERTY, outputDirectory.resolve("person_property_report.xls"))//
				.addReport(ModelReportLabel.DISEASE_STATE, outputDirectory.resolve("disease_state_report.xls"))//
				.addReport(ModelReportLabel.CONTAGION, outputDirectory.resolve("contagion_report.xls"))//
				.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	/* start code_ref= groups_plugin_example_17_groups_plugin */
	private Plugin getGroupsPlugin() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (GroupType groupType : GroupType.values()) {
			builder.addGroupTypeId(groupType);
		}
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();

		builder.defineGroupProperty(GroupType.WORK, GroupProperty.TELEWORK, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(SchoolStatus.class)//
				.setDefaultValue(SchoolStatus.OPEN)//
				.build();

		builder.defineGroupProperty(GroupType.SCHOOL, GroupProperty.SCHOOL_STATUS, propertyDefinition);

		GroupsPluginData groupsPluginData = builder.build();

		GroupPopulationReportPluginData groupPopulationReportPluginData = //
				GroupPopulationReportPluginData.builder()//
						.setReportLabel(ModelReportLabel.GROUP_POPULATON)//
						.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
						.build();//
		return GroupsPlugin.builder()//
				.setGroupsPluginData(groupsPluginData)//
				.setGroupPopulationReportPluginData(groupPopulationReportPluginData)//
				.getGroupsPlugin();
	}

	/* end */
	private Plugin getStochasticsPlugin() {

		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState)//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	/* start code_ref= groups_plugin_example_17_person_properties */
	private Plugin getPersonPropertiesPlugin() {

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.build();

		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition, 0, false);//

		propertyDefinition = PropertyDefinition.builder()//
				.setType(DiseaseState.class)//
				.setDefaultValue(DiseaseState.SUSCEPTIBLE).build();

		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition, 0, false);//

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(0).build();

		builder.definePersonProperty(PersonProperty.INFECTED_COUNT, propertyDefinition, 0, false);//

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData.builder()//
				.setReportLabel(ModelReportLabel.PERSON_PROPERTY)//
				.setReportPeriod(ReportPeriod.DAILY)//
				.includePersonProperty(PersonProperty.DISEASE_STATE)//
				.build();

		return PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
				.getPersonPropertyPlugin();
	}
	/* end */

	/* start code_ref= groups_plugin_example_17_global_properties */
	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setPropertyValueMutability(false)//
				.setDefaultValue(0.0).build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.TELEWORK_INFECTION_THRESHOLD, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.TELEWORK_PROBABILTY, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.SCHOOL_COHORT_INFECTION_THRESHOLD, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.SCHOOL_CLOSURE_INFECTION_THRESHOLD, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setPropertyValueMutability(false)//
				.build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTIONS, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.MIN_INFECTIOUS_PERIOD, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.MAX_INFECTIOUS_PERIOD, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition, 0);

		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000, 0);
		builder.setGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, 1.0, 0);
		builder.setGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS, 10, 0);
		builder.setGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD, 3, 0);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD, 12, 0);
		builder.setGlobalPropertyValue(GlobalProperty.R0, 2.0, 0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235, 0);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169, 0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5, 0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0, 0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0, 0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.getGlobalPropertiesPlugin();
	}

	/* end */
	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 5; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context
						.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value, 0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	/* start code_ref= groups_plugin_example_17_execute */
	private void execute() {

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(8)//
				.build();

		Experiment.builder()

				.addPlugin(getGlobalPropertiesPlugin())//
				.addPlugin(getPersonPropertiesPlugin())//
				.addPlugin(getRegionsPlugin())//
				.addPlugin(getPeoplePlugin())//
				.addPlugin(getGroupsPlugin())//
				.addPlugin(getStochasticsPlugin())//
				.addPlugin(ModelPlugin.getModelPlugin())//

				.addDimension(getTeleworkProbabilityDimension())//
				.addDimension(getTeleworkInfectionThresholdDimension())//
				.addDimension(getSchoolDimension())//

				.addExperimentContextConsumer(getNIOReportItemHandler())//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();//
	}
	/* end */

	/* start code_ref= groups_plugin_example_17_init */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("One output directory argument is required");
		}
		Path outputDirectory = Paths.get(args[0]);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		} else {
			if (!Files.isDirectory(outputDirectory)) {
				throw new IOException("Provided path is not a directory");
			}
		}

		new Example_17(outputDirectory).execute();
	}
	/* end */

	private Dimension getTeleworkProbabilityDimension() {
		double[] values = new double[] { 0.1, 0.3, 0.5, 0.8 };
		return getGlobalPropertyDimension(GlobalProperty.TELEWORK_PROBABILTY, "telework_probabilty", values);
	}

	private Dimension getTeleworkInfectionThresholdDimension() {
		double[] values = new double[] { 0.001, 0.01, 0.1 };
		return getGlobalPropertyDimension(GlobalProperty.TELEWORK_INFECTION_THRESHOLD, "telework_infection_threshold",
				values);
	}

	private Dimension getSchoolDimension() {
		double[] cohortValues = { 0.001, 0.01, 0.1 };
		double[] closureValues = { 0.01, 0.02, 0.2 };

		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, cohortValues.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context
						.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double cohortValue = cohortValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.SCHOOL_COHORT_INFECTION_THRESHOLD, cohortValue, 0);
				double closureValue = closureValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.SCHOOL_CLOSURE_INFECTION_THRESHOLD, closureValue, 0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(cohortValue));
				result.add(Double.toString(closureValue));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum("school_cohort_infection_threshold");//
		dimensionBuilder.addMetaDatum("school_closure_infection_threshold");//
		return dimensionBuilder.build();
	}

}