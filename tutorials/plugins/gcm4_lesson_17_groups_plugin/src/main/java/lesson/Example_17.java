package lesson;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.DiseaseState;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.GroupType;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportId;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.plugins.model.actors.reports.ContagionReport;
import lesson.plugins.model.actors.reports.DiseaseStateReport;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.actors.GroupPopulationReport;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.actors.PersonPropertyReport;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
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

	private Plugin getReportsPlugin() {

		ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									.addReport(() -> new GroupPopulationReport(ModelReportId.GROUP_POPULATON, ReportPeriod.END_OF_SIMULATION)::init)//
									.addReport(() -> PersonPropertyReport	.builder()//
																			.setReportId(ModelReportId.PERSON_PROPERTY)//
																			.setReportPeriod(ReportPeriod.DAILY)//
																			.includePersonProperty(PersonProperty.DISEASE_STATE)//
																			.build()::init)//
									.addReport(() -> new DiseaseStateReport(ModelReportId.DISEASE_STATE, ReportPeriod.DAILY)::init)//
									.addReport(() -> new ContagionReport(ModelReportId.CONTAGION)::init)//
									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportId.GROUP_POPULATON, Paths.get("c:\\temp\\gcm\\group_population_report.xls"))//
									.addReport(ModelReportId.PERSON_PROPERTY, Paths.get("c:\\temp\\gcm\\person_property_report.xls"))//
									.addReport(ModelReportId.DISEASE_STATE, Paths.get("c:\\temp\\gcm\\disease_state_report.xls"))//
									.addReport(ModelReportId.CONTAGION, Paths.get("c:\\temp\\gcm\\contagion_report.xls"))//
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
		GroupsPluginData groupsPluginData = builder.build();
		return GroupsPlugin.getGroupPlugin(groupsPluginData);
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

		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
	}

	private Plugin getGlobalPropertiesPlugin() {
		Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition);

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
		builder.setGlobalPropertyValue(GlobalProperty.R0, 5.0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);
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
					.addPlugin(getReportsPlugin())//
					.addPlugin(getRegionsPlugin())//
					.addPlugin(getPeoplePlugin())//
					.addPlugin(getGroupsPlugin())//
					.addPlugin(getStochasticsPlugin())//
					.addPlugin(ModelPlugin.getModelPlugin())//

					// .addDimension(getMaximumSymptomOnsetTimeDimension())//
					// .addDimension(getSusceptiblePopulationProportionDimension())//
					// .addDimension(getAntiviralCoverageTimeDimension())//
					// .addDimension(getAntiviralSuccessRateDimension())//
					// .addDimension(getHospitalSuccessDimension())//
					// .addDimension(getHospitalBedsPerPersonDimension())//
					// .addDimension(getAntiviralDosesPerPersonDimension())//
					// .addDimension(getHospitalStayDurationDimension())//

					.addExperimentContextConsumer(getNIOReportItemHandler())//
					// .setThreadCount(8)//
					// .reportProgressToConsole(false)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) {
		new Example_17().execute();
	}

	// private Dimension getAntiviralSuccessRateDimension() {
	// double[] values = new double[] { .50, 0.8 };
	// return getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_SUCCESS_RATE,
	// "antiviral_success_rate", values);
	// }
	//
	// private Dimension getAntiviralCoverageTimeDimension() {
	// double[] values = new double[] { 10.0, 15.0 };
	// return getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_COVERAGE_TIME,
	// "antiviral_coverage_time", values);
	// }
	//
	// private Dimension getMaximumSymptomOnsetTimeDimension() {
	// double[] values = new double[] { 60, 120 };
	// return
	// getGlobalPropertyDimension(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME,
	// "maximum_symptom_onset_time", values);
	// }
	//
	// private Dimension getSusceptiblePopulationProportionDimension() {
	// double[] values = new double[] { 0.25, 0.5, 0.75 };
	// return
	// getGlobalPropertyDimension(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION,
	// "susceptible_population_proportion", values);
	// }
	//
	// private Dimension getAntiviralDosesPerPersonDimension() {
	// double[] values = new double[] { .10, 0.20, 0.5 };
	// return
	// getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON,
	// "antiviral_doses_per_person", values);
	// }
	//
	// private Dimension getHospitalBedsPerPersonDimension() {
	// double[] values = new double[] { 0.001, 0.003, 0.005 };
	// return
	// getGlobalPropertyDimension(GlobalProperty.HOSPITAL_BEDS_PER_PERSON,
	// "hospital_beds_per_person", values);
	// }
	//
	// private Dimension getHospitalSuccessDimension() {
	// double[] minValues = { 0.30, 5.0 };
	// double[] maxValues = { 0.50, 0.75 };
	//
	// Dimension.Builder dimensionBuilder = Dimension.builder();//
	// IntStream.range(0, minValues.length).forEach((i) -> {
	// dimensionBuilder.addLevel((context) -> {
	// GlobalPropertiesPluginData.Builder builder =
	// context.get(GlobalPropertiesPluginData.Builder.class);
	// double minValue = minValues[i];
	// builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL,
	// minValue);
	// double maxValue = maxValues[i];
	// builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL,
	// maxValue);
	// ArrayList<String> result = new ArrayList<>();
	// result.add(Double.toString(minValue));
	// result.add(Double.toString(maxValue));
	// return result;
	// });//
	// });
	// dimensionBuilder.addMetaDatum("hospital_success_without_antiviral");//
	// dimensionBuilder.addMetaDatum("hospital_success_with_antiviral");//
	// return dimensionBuilder.build();
	// }

	// private Dimension getHospitalStayDurationDimension() {
	// double[] minValues = { 2.0, 5.0 };
	// double[] maxValues = { 5.0, 10.0 };
	//
	// Dimension.Builder dimensionBuilder = Dimension.builder();//
	// IntStream.range(0, minValues.length).forEach((i) -> {
	// dimensionBuilder.addLevel((context) -> {
	// GlobalPropertiesPluginData.Builder builder =
	// context.get(GlobalPropertiesPluginData.Builder.class);
	// double minValue = minValues[i];
	// builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MIN,
	// minValue);
	// double maxValue = maxValues[i];
	// builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MAX,
	// maxValue);
	// ArrayList<String> result = new ArrayList<>();
	// result.add(Double.toString(minValue));
	// result.add(Double.toString(maxValue));
	// return result;
	// });//
	// });
	// dimensionBuilder.addMetaDatum("hospital_stay_duration_min");//
	// dimensionBuilder.addMetaDatum("hospital_stay_duration_max");//
	// return dimensionBuilder.build();
	// }

}
