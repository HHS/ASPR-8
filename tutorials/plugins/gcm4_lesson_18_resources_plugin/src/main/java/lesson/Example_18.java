package lesson;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportId;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.plugins.model.Resource;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.actors.PersonResourceReport;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_18 {

	private Example_18() {
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	private Plugin getReportsPlugin() {
		ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									.addReport(() -> new PersonResourceReport(
											ModelReportId.PERSON_RESOURCE_REPORT,//
											ReportPeriod.END_OF_SIMULATION,//
											true,//
											true)//
											::init)//
									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private Plugin getResourcesPlugin() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();//
		for (ResourceId resourcId : Resource.values()) {
			builder.addResource(resourcId);
		}
		ResourcesPluginData resourcesPluginData = builder.build();

		return ResourcesPlugin.getResourcesPlugin(resourcesPluginData);

	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportId.PERSON_RESOURCE_REPORT, Paths.get("c:\\temp\\gcm\\person_resource_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 5; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//

																			.setSeed(randomGenerator.nextLong())//
																			.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Plugin getPersonPropertiesPlugin() {

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();

		PersonPropertiesPluginData personPropertiesPluginData = //
				PersonPropertiesPluginData	.builder()//
											.definePersonProperty(PersonProperty.IMMUNE, propertyDefinition)//
											.definePersonProperty(PersonProperty.HOSPITALIZED, propertyDefinition)//
											.definePersonProperty(PersonProperty.TREATED_WITH_ANTIVIRAL, propertyDefinition)//
											.definePersonProperty(PersonProperty.DEAD, propertyDefinition)//
											.build();

		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
	}

	private Plugin getGlobalPropertiesPlugin() {
		Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(0.0)//
																	.setPropertyValueMutability(false)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_COVERAGE_TIME, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_SUCCESS_RATE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_BEDS_PER_PERSON, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON, propertyDefinition);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(10000)//
												.setPropertyValueMutability(false)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);
	}

	private Dimension getGlobalPropertyDimension_Double(GlobalPropertyId globalPropertyId, String header, double[] values) {
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

	private Dimension getAntiviralDosesPerPpersonDimension() {
		//double[] values = new double[] { .10, 0.20, 0.5 };
		double[] values = new double[] { 0.5 };
		return getGlobalPropertyDimension_Double(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON, "antiviral_doses_per_person", values);
	}

	private Dimension getHospitalBedsPerPersonDimension() {
		double[] values = new double[] { .05 };
		return getGlobalPropertyDimension_Double(GlobalProperty.HOSPITAL_BEDS_PER_PERSON, "hospital_beds_per_person", values);
	}

	private Dimension getHospitalSuccessWithoutAntiviralDimension() {
		double[] values = new double[] { .50 };
		return getGlobalPropertyDimension_Double(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL, "hospital_success_without_antiviral", values);
	}

	private Dimension getHospitalSuccessWithAntiviralDimension() {
		double[] values = new double[] { 0.75 };
		return getGlobalPropertyDimension_Double(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL, "hospital_success_with_antiviral", values);
	}

	private Dimension getAntiviralSuccessRateDimension() {
		//double[] values = new double[] { .50, 0.8 };
		double[] values = new double[] {  0.8 };
		return getGlobalPropertyDimension_Double(GlobalProperty.ANTIVIRAL_SUCCESS_RATE, "antiviral_success_rate", values);
	}

	private Dimension getAntiviralCoverageTimeDimension() {
		double[] values = new double[] { 15.0 };
		return getGlobalPropertyDimension_Double(GlobalProperty.ANTIVIRAL_COVERAGE_TIME, "antiviral_coverage_time", values);
	}

	private Dimension getMaximumSymptomOnsetTimeDimension() {
		//double[] values = new double[] { 60, 120 };
		double[] values = new double[] { 60 };
		return getGlobalPropertyDimension_Double(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME, "maximum_symptom_onset_time", values);
	}

	private Dimension getSusceptiblePopulationProportionDimension() {
		//double[] values = new double[] { 0.25, 0.5, 0.75 };
		double[] values = new double[] { 0.75 };
		return getGlobalPropertyDimension_Double(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, "susceptible_population_proportion", values);
	}

	private void execute() {

		/*
		 * Create the global properties plugin
		 */
		Plugin globalPropertiesPlugin = getGlobalPropertiesPlugin();

		/*
		 * Create the reports
		 */
		Plugin reportsPlugin = getReportsPlugin();
		NIOReportItemHandler nioReportItemHandler = getNIOReportItemHandler();

		/*
		 * Create the people plugin
		 */
		Plugin peoplePlugin = getPeoplePlugin();

		// Create the person properties plugin
		Plugin personPropertiesPlugin = getPersonPropertiesPlugin();

		/*
		 * Create the region plugin 5 regions, each having a lat and lon and
		 * assign the people to random regions.
		 * 
		 */
		Plugin regionsPlugin = getRegionsPlugin();

		/*
		 * create the stochastics plugin
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Plugin resourcesPlugin = getResourcesPlugin();

		/*
		 * Assemble and execute the experiment
		 */

		Experiment	.builder()//
					.addPlugin(resourcesPlugin)//
					.addPlugin(globalPropertiesPlugin)//
					.addPlugin(personPropertiesPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(reportsPlugin)//

					.addDimension(getMaximumSymptomOnsetTimeDimension())//
					.addDimension(getSusceptiblePopulationProportionDimension())//
					.addDimension(getAntiviralCoverageTimeDimension())//
					.addDimension(getAntiviralSuccessRateDimension())//
					.addDimension(getHospitalSuccessWithAntiviralDimension())//
					.addDimension(getHospitalSuccessWithoutAntiviralDimension())//
					.addDimension(getHospitalBedsPerPersonDimension())//
					.addDimension(getAntiviralDosesPerPpersonDimension())//

					.addExperimentContextConsumer(nioReportItemHandler)//
					//.setThreadCount(8)//
					// .reportProgressToConsole(false)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) {
		new Example_18().execute();
	}

}
