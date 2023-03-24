package lesson;

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
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.reports.GroupPopulationReportPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_17 {

	private Example_17() {
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	
	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportLabel.GROUP_POPULATON, Paths.get("c:\\temp\\gcm\\group_population_report.xls"))//
									.addReport(ModelReportLabel.PERSON_PROPERTY, Paths.get("c:\\temp\\gcm\\person_property_report.xls"))//
									.addReport(ModelReportLabel.DISEASE_STATE, Paths.get("c:\\temp\\gcm\\disease_state_report.xls"))//
									.addReport(ModelReportLabel.CONTAGION, Paths.get("c:\\temp\\gcm\\contagion_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getGroupsPlugin() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (GroupType groupType : GroupType.values()) {
			builder.addGroupTypeId(groupType);
		}
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();

		builder.defineGroupProperty(GroupType.WORK, GroupProperty.TELEWORK, propertyDefinition);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(SchoolStatus.class)//
												.setDefaultValue(SchoolStatus.OPEN)//
												.build();

		builder.defineGroupProperty(GroupType.SCHOOL, GroupProperty.SCHOOL_STATUS, propertyDefinition);

		GroupsPluginData groupsPluginData = builder.build();

		GroupPopulationReportPluginData groupPopulationReportPluginData = //
				GroupPopulationReportPluginData	.builder()//
												.setReportLabel(ModelReportLabel.GROUP_POPULATON)//
												.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
												.build();//
		return GroupsPlugin	.builder()//
							.setGroupsPluginData(groupsPluginData)//
							.setGroupPopulationReportPluginData(groupPopulationReportPluginData)//
							.getGroupsPlugin();
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//

																			.setSeed(randomGenerator.nextLong())//
																			.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Plugin getPersonPropertiesPlugin() {

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.build();

		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(DiseaseState.class)//
												.setDefaultValue(DiseaseState.SUSCEPTIBLE).build();

		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(0).build();

		builder.definePersonProperty(PersonProperty.INFECTED_COUNT, propertyDefinition);//

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData	.builder()//
																										.setReportLabel(ModelReportLabel.PERSON_PROPERTY)//
																										.setReportPeriod(ReportPeriod.DAILY)//
																										.includePersonProperty(PersonProperty.DISEASE_STATE)//
																										.build();

		return PersonPropertiesPlugin	.builder()//
										.setPersonPropertiesPluginData(personPropertiesPluginData)//
										.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
										.getPersonPropertyPlugin();
	}

	private Plugin getGlobalPropertiesPlugin() {
		Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.setDefaultValue(0.0).build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.TELEWORK_INFECTION_THRESHOLD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.TELEWORK_PROBABILTY, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.SCHOOL_COHORT_INFECTION_THRESHOLD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.SCHOOL_CLOSURE_INFECTION_THRESHOLD, propertyDefinition);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setPropertyValueMutability(false)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTIONS, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MIN_INFECTIOUS_PERIOD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MAX_INFECTIOUS_PERIOD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition);

		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000);
		builder.setGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, 1.0);
		builder.setGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS, 10);
		builder.setGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD, 3);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD, 12);
		builder.setGlobalPropertyValue(GlobalProperty.R0, 2.0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();
	}

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 5; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private void execute() {

		Experiment	.builder()

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
					.setThreadCount(8)//
					.build()//
					.execute();//
	}

	public static void main(String[] args) {
		new Example_17().execute();
	}

	private Dimension getTeleworkProbabilityDimension() {
		double[] values = new double[] { 0.1, 0.3, 0.5, 0.8 };
		return getGlobalPropertyDimension(GlobalProperty.TELEWORK_PROBABILTY, "telework_probabilty", values);
	}

	private Dimension getTeleworkInfectionThresholdDimension() {
		double[] values = new double[] { 0.001, 0.01, 0.1 };
		return getGlobalPropertyDimension(GlobalProperty.TELEWORK_INFECTION_THRESHOLD, "telework_infection_threshold", values);
	}

	private Dimension getSchoolDimension() {
		double[] cohortValues = { 0.001, 0.01, 0.1 };
		double[] closureValues = { 0.01, 0.02, 0.2 };

		Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, cohortValues.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				double cohortValue = cohortValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.SCHOOL_COHORT_INFECTION_THRESHOLD, cohortValue);
				double closureValue = closureValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.SCHOOL_CLOSURE_INFECTION_THRESHOLD, closureValue);
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
